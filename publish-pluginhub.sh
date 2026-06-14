#!/usr/bin/env bash
set -euo pipefail

if ! command -v gh >/dev/null 2>&1; then
  echo "GitHub CLI (gh) is required. Install it first."
  exit 1
fi

if ! gh auth status >/dev/null 2>&1; then
  echo "You are not authenticated in gh. Run: gh auth login"
  exit 1
fi

PLUGIN_REPO="${1:-}"
if [[ -z "$PLUGIN_REPO" ]]; then
  echo "Usage: $0 <owner/repo>"
  echo "Example: $0 yourname/play-time-limit"
  exit 1
fi

if [[ ! "$PLUGIN_REPO" =~ ^[^/]+/[^/]+$ ]]; then
  echo "Invalid repository format. Expected owner/repo"
  exit 1
fi

WORKDIR="$(pwd)"
COMMIT_HASH="$(git rev-parse HEAD)"
PLUGIN_SLUG="play-time-limit"

OWNER="${PLUGIN_REPO%%/*}"
REPO_NAME="${PLUGIN_REPO##*/}"
REPO_URL="https://github.com/${OWNER}/${REPO_NAME}.git"

# Ensure main branch naming is consistent for GitHub.
git branch -M main

if git remote get-url origin >/dev/null 2>&1; then
  git remote set-url origin "$REPO_URL"
else
  git remote add origin "$REPO_URL"
fi

echo "Pushing plugin repository to ${REPO_URL}"
git push -u origin main

TMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TMP_DIR"' EXIT

echo "Cloning plugin-hub"
git clone https://github.com/runelite/plugin-hub.git "$TMP_DIR/plugin-hub"
cd "$TMP_DIR/plugin-hub"

git checkout -b "submit-${PLUGIN_SLUG}"

cat > "plugins/${PLUGIN_SLUG}" <<EOF
repository=${REPO_URL}
commit=${COMMIT_HASH}
EOF

git add "plugins/${PLUGIN_SLUG}"
git commit -m "add ${PLUGIN_SLUG}"

# Push to user's fork via GitHub CLI; creates fork if needed.
if ! gh repo view "$OWNER/plugin-hub" >/dev/null 2>&1; then
  echo "Forking runelite/plugin-hub into ${OWNER}/plugin-hub"
  gh repo fork runelite/plugin-hub --clone=false --remote=false
fi

git remote add fork "https://github.com/${OWNER}/plugin-hub.git"
git push -u fork "submit-${PLUGIN_SLUG}"

echo "Creating Pull Request"
PR_URL="$(gh pr create \
  --repo runelite/plugin-hub \
  --head "${OWNER}:submit-${PLUGIN_SLUG}" \
  --base master \
  --title "Add ${PLUGIN_SLUG}" \
  --body "Adds plugin marker for ${PLUGIN_SLUG}.\n\nRepository: ${REPO_URL}\nCommit: ${COMMIT_HASH}")"

echo "PR created: ${PR_URL}"

echo "Done."
