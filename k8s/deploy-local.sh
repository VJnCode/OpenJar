#!/usr/bin/env bash
# Deploy the whole OpenJar stack to a local kind cluster, served at http://localhost
#
#   ./k8s/deploy-local.sh            create cluster (if needed), build images, deploy
#   ./k8s/deploy-local.sh --no-build redeploy without rebuilding images
#   ./k8s/deploy-local.sh status     show pods
#   ./k8s/deploy-local.sh logs <svc> follow one service's logs
#   ./k8s/deploy-local.sh down       delete the cluster (data included)
#
# Needs: Docker Desktop (give it >= 6 GB RAM), kind, kubectl.
# Frontend source is expected next to this repo: ../OpenJar-Frontend
set -euo pipefail

HERE="$(cd "$(dirname "$0")" && pwd)"
ROOT="$(cd "$HERE/.." && pwd)"
FRONTEND="${FRONTEND_DIR:-$ROOT/../OpenJar-Frontend}"
CLUSTER=openjar
NS=openjar
INGRESS_NGINX_VERSION=controller-v1.11.3
SERVICES=(eureka user-service recipe-service social-service notification-service api-gateway)
SECRET_KEYS=(MYSQL_ROOT_PASSWORD RABBITMQ_USER RABBITMQ_PASS OAUTH_CLIENT_SECRET INTERNAL_API_TOKEN
             MAIL_USER MAIL_PASS GOOGLE_CLIENT_ID GOOGLE_CLIENT_SECRET)

say()  { printf '\n\033[1;33m==> %s\033[0m\n' "$*"; }
fail() { printf '\033[1;31mERROR: %s\033[0m\n' "$*" >&2; exit 1; }
need() { command -v "$1" >/dev/null || fail "'$1' is not installed. $2"; }

case "${1:-}" in
  down)   kind delete cluster --name "$CLUSTER"; exit 0 ;;
  status) kubectl -n "$NS" get pods,svc,ingress; exit 0 ;;
  logs)   kubectl -n "$NS" logs -f "deploy/${2:?usage: logs <service>}"; exit 0 ;;
esac

need docker  "Install Docker Desktop."
need kind    "Install: winget install Kubernetes.kind   (or: choco install kind)"
need kubectl "Install: winget install Kubernetes.kubectl"
docker info >/dev/null 2>&1 || fail "Docker is not running. Start Docker Desktop first."
[[ -f "$ROOT/.env" ]] || fail "Missing $ROOT/.env (copy .env.example and fill it in)."

# ---- 1. Cluster + ingress controller -------------------------------------------------
if ! kind get clusters | grep -qx "$CLUSTER"; then
  say "Creating kind cluster '$CLUSTER' (port 80 on 127.0.0.1)"
  kind create cluster --config "$HERE/kind-config.yaml"
fi
kubectl config use-context "kind-$CLUSTER" >/dev/null

if ! kubectl get ns ingress-nginx >/dev/null 2>&1; then
  say "Installing ingress-nginx ($INGRESS_NGINX_VERSION)"
  kubectl apply -f "https://raw.githubusercontent.com/kubernetes/ingress-nginx/$INGRESS_NGINX_VERSION/deploy/static/provider/kind/deploy.yaml"
fi
kubectl -n ingress-nginx wait --for=condition=ready pod -l app.kubernetes.io/component=controller --timeout=180s

# ---- 2. Images ------------------------------------------------------------------------
if [[ "${1:-}" != "--no-build" ]]; then
  for s in "${SERVICES[@]}"; do
    say "Building openjar/$s:local"
    docker build -q -t "openjar/$s:local" "$ROOT/$s" >/dev/null
  done
  if [[ -d "$FRONTEND" ]]; then
    say "Building openjar/frontend:local"
    docker build -q -t openjar/frontend:local "$FRONTEND" >/dev/null
  else
    echo "WARNING: $FRONTEND not found - the frontend will not start."
  fi
  say "Loading images into the cluster"
  imgs=(); for s in "${SERVICES[@]}"; do imgs+=("openjar/$s:local"); done
  [[ -d "$FRONTEND" ]] && imgs+=(openjar/frontend:local)
  kind load docker-image --name "$CLUSTER" "${imgs[@]}"
fi

# ---- 3. Secrets (from ../.env, only the keys the cluster needs) -----------------------
SECRETS_FILE="$HERE/overlays/local/secrets.env"
: > "$SECRETS_FILE"
chmod 600 "$SECRETS_FILE" 2>/dev/null || true
for k in "${SECRET_KEYS[@]}"; do
  v="$(grep -E "^$k=" "$ROOT/.env" | tail -1 | cut -d= -f2- | tr -d '\r' || true)"
  printf '%s=%s\n' "$k" "$v" >> "$SECRETS_FILE"
done

# ---- 4. Deploy ------------------------------------------------------------------------
say "Applying manifests"
kubectl apply -k "$HERE/overlays/local"
# Pods only read env at start-up, so restart them to pick up new images/secrets.
kubectl -n "$NS" rollout restart deployment >/dev/null

say "Waiting for everything to become ready (first start can take a few minutes)"
kubectl -n "$NS" rollout status statefulset/mysql --timeout=300s
for d in rabbitmq "${SERVICES[@]}" frontend; do
  kubectl -n "$NS" rollout status "deployment/$d" --timeout=420s
done

cat <<EOF

  OpenJar is running at  http://localhost

  For "Continue with Google" add this Authorized redirect URI in Google Cloud Console:
      http://localhost/login/oauth2/code/google

  Useful:  ./k8s/deploy-local.sh status | logs <service> | down
EOF
