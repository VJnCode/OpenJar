# OpenJar DevOps

## Pipeline (GitHub Actions)

```
 pull request / push
   ├─ build-test   (matrix, 7 services)  mvn verify + unit tests
   ├─ secret-scan  gitleaks
   └─ e2e          kind cluster in the runner → kustomize deploy → smoke tests
                   (real OAuth2 sign-in → create recipe → comment → like → delete)
 push to main only
   └─ publish      (matrix) build → Trivy scan → push ghcr.io/<owner>/openjar-<service>:{sha,latest}
```

The frontend repository has its own pipeline: lint → build → npm audit → secret scan →
container build + smoke test (deep links, security headers, non-root) → Trivy →
publish `ghcr.io/<owner>/openjar-frontend` on main.

Dependabot opens weekly PRs for Maven, npm, Docker base images and Actions; every one of
them runs the full pipeline including the Kubernetes e2e test.

### One-time GitHub settings

1. **Settings → Actions → General → Workflow permissions**: "Read and write permissions"
   is *not* needed (jobs request `packages: write` themselves), but Actions must be enabled.
2. **Settings → Branches → Add rule for `main`**: require the status checks
   `build & test (*)`, `secret-scan` and `e2e on Kubernetes (kind)` before merging.
3. After the first publish, images appear under your profile → **Packages**. They are
   private by default; make them public or add an `imagePullSecret` if a cluster outside
   GitHub needs to pull them.
4. If `VJnCode` is a GitHub *organisation* (not a personal account), gitleaks needs a free
   licence key stored as the `GITLEAKS_LICENSE` secret.

## Kubernetes layout

```
k8s/
  kind-config.yaml        local/CI cluster, ingress on http://localhost (127.0.0.1:80)
  base/                   all workloads, hardened (non-root, read-only FS, dropped caps,
                          probes, limits, NetworkPolicies, per-service secrets only)
  overlays/local/         images built on your PC, secrets from ../.env
  overlays/ci/            throw-away secrets, backend only (used by the e2e job)
  deploy-local.sh         one command: cluster + ingress + build + deploy + wait
  smoke-test.sh           end-to-end checks (used by CI, runnable locally)
```

Everything is served from one origin through the Ingress:

| Path | Goes to |
|---|---|
| `/api/**`, `/recipe/**`, `/logout`, `/oauth2/authorization/**`, `/login/oauth2/code/openjar-client` | api-gateway |
| `/login`, `/oauth2/**`, `/connect/**`, `/.well-known/**`, `/userinfo`, `/auth-assets/**`, `/login/oauth2/code/google` | user-service |
| everything else | frontend |

## Run it on your PC

Requirements: Docker Desktop (give it at least 6 GB RAM under Settings → Resources),
[kind](https://kind.sigs.k8s.io/) (`winget install Kubernetes.kind`) and kubectl.
Port 80 on 127.0.0.1 must be free.

```bash
cd OpenJar_Backend
./k8s/deploy-local.sh              # first run ~10 min (builds 7 images)
open http://localhost
./k8s/smoke-test.sh                # optional: verify the deployment
./k8s/deploy-local.sh down         # remove the cluster
```

For "Continue with Google" on the cluster, add this second Authorized redirect URI in
Google Cloud Console: `http://localhost/login/oauth2/code/google`.

## Going to production later

Add an `overlays/prod` that points images at `ghcr.io/<owner>/openjar-*:<sha>`, sets
`AUTH_ISSUER`, `FRONTEND_URL` and `OAUTH_REDIRECT_URI` to your HTTPS domain, adds TLS (cert-manager),
uses a managed MySQL instead of the in-cluster StatefulSet, and stores secrets in your
cloud's secret manager (e.g. External Secrets Operator). The deploy step then becomes a
job that runs `kubectl apply -k k8s/overlays/prod` with cloud OIDC credentials.
