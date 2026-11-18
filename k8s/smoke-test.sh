#!/usr/bin/env bash
# End-to-end smoke test against a running OpenJar deployment (kind in CI, or your local cluster).
#
#   ./k8s/smoke-test.sh                 checks public endpoints + OAuth wiring
#   SEED_USER=1 ./k8s/smoke-test.sh     also seeds a verified test user and runs a full
#                                       sign-in -> create recipe -> comment -> like journey
#                                       (needs kubectl access + python3 with "bcrypt")
set -uo pipefail

BASE="${BASE_URL:-http://localhost}"
NS="${NAMESPACE:-openjar}"
JAR="$(mktemp)"; trap 'rm -f "$JAR"' EXIT
pass=0; fail=0

check() {  # check <description> <expected> <actual>
  if [[ "$2" == "$3" ]]; then printf '  \033[32mPASS\033[0m %s\n' "$1"; pass=$((pass+1))
  else printf '  \033[31mFAIL\033[0m %s (expected %s, got %s)\n' "$1" "$2" "$3"; fail=$((fail+1)); fi
}
code()  { curl -s -o /dev/null -w '%{http_code}' -b "$JAR" -c "$JAR" "$@"; }
xsrf()  { awk '$6=="XSRF-TOKEN"{print $7}' "$JAR" | tail -1; }

echo "Smoke testing $BASE"

echo "-- public surface"
# EXPECT_ROOT=503 when the frontend isn't deployed (CI runs the backend repo only).
check "SPA answers on /"                              "${EXPECT_ROOT:-200}" "$(code "$BASE/")"
check "recipes API is public"                         200 "$(code -H 'Accept: application/json' "$BASE/recipe")"
check "CSRF endpoint"                                 204 "$(code "$BASE/api/csrf")"
check "/api/users/me needs a session"                 401 "$(code "$BASE/api/users/me")"
check "auth options endpoint"                         200 "$(code "$BASE/api/users/auth-options")"
check "writes without CSRF token are rejected"        403 "$(code -X POST -H 'Content-Type: application/json' -d '{}' "$BASE/recipe")"

echo "-- OAuth2 / OIDC wiring"
issuer="$(curl -s "$BASE/.well-known/openid-configuration" | sed -n 's/.*"issuer":"\([^"]*\)".*/\1/p')"
check "issuer is the public URL"                      "$BASE" "$issuer"
loc="$(curl -s -o /dev/null -w '%{redirect_url}' -b "$JAR" -c "$JAR" "$BASE/oauth2/authorization/openjar-client")"
check "login starts at the authorization server"     "$BASE/oauth2/authorize" "${loc%%\?*}"
[[ "$loc" == *code_challenge=* ]] && pkce=yes || pkce=no
check "PKCE challenge is sent"                        yes "$pkce"
check "branded login page is served"                  yes "$(curl -s "$BASE/login" | grep -q 'Sign in to your kitchen' && echo yes || echo no)"
# /internal/** must never reach user-service (it would answer 403 for a missing shared secret).
check "internal endpoints are not routed publicly"    no  "$([[ "$(code "$BASE/internal/users/x")" == 403 ]] && echo yes || echo no)"

if [[ "${SEED_USER:-0}" == "1" ]]; then
  echo "-- full user journey"
  EMAIL="smoke@example.com"; PASSWORD="Smoke-Passw0rd!"
  HASH="$(python3 -c 'import bcrypt,sys; print(bcrypt.hashpw(sys.argv[1].encode(), bcrypt.gensalt(10)).decode())' "$PASSWORD")"
  kubectl -n "$NS" exec -i statefulset/mysql -- sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" OpenJar 2>/dev/null' <<SQL
INSERT INTO users (user_id, user_name, user_email, password, is_verified, created_at, otp_attempts)
VALUES ('smoke-user', 'Smoke Chef', '$EMAIL', '$HASH', 1, NOW(), 0)
ON DUPLICATE KEY UPDATE password = VALUES(password), is_verified = 1;
SQL
  : > "$JAR"
  loc="$(curl -s -o /dev/null -w '%{redirect_url}' -b "$JAR" -c "$JAR" "$BASE/oauth2/authorization/openjar-client")"
  curl -s -o /dev/null -L -b "$JAR" -c "$JAR" "$loc"
  csrf="$(curl -s -b "$JAR" -c "$JAR" "$BASE/login" | grep -o 'name="_csrf" value="[^"]*"' | sed 's/.*value="//;s/"$//')"
  landed="$(curl -s -o /dev/null -w '%{url_effective}' -L --max-redirs 10 -b "$JAR" -c "$JAR" \
    --data-urlencode "username=$EMAIL" --data-urlencode "password=$PASSWORD" --data-urlencode "_csrf=$csrf" "$BASE/login")"
  check "sign-in lands back on the SPA"               "$BASE/callback" "$landed"
  check "session works"                               200 "$(code "$BASE/api/users/me")"
  code "$BASE/api/csrf" >/dev/null
  body='{"recipeName":"Smoke Soup","category":"Dinner","prepTime":5,"ingredients":"water\nsalt","recipeInstructions":"Boil\nServe","recipeImageUrl":""}'
  created="$(curl -s -b "$JAR" -c "$JAR" -X POST -H 'Content-Type: application/json' -H "X-XSRF-TOKEN: $(xsrf)" -d "$body" "$BASE/recipe")"
  rid="$(sed -n 's/.*"recipeId":\([0-9]*\).*/\1/p' <<<"$created")"
  check "recipe created, owned by the signed-in user" yes "$([[ -n "$rid" && "$created" == *'"userId":"smoke-user"'* ]] && echo yes || echo no)"
  check "comment posted"                              201 "$(code -X POST -H 'Content-Type: application/json' -H "X-XSRF-TOKEN: $(xsrf)" -d "{\"recipeId\":\"$rid\",\"content\":\"tasty\"}" "$BASE/api/social/comments/add")"
  check "recipe liked"                                200 "$(code -X POST -H "X-XSRF-TOKEN: $(xsrf)" "$BASE/api/social/likes/$rid")"
  check "owner can delete the recipe"                 200 "$(code -X DELETE -H "X-XSRF-TOKEN: $(xsrf)" "$BASE/recipe/$rid")"
fi

echo
echo "$pass passed, $fail failed"
[[ $fail -eq 0 ]]
