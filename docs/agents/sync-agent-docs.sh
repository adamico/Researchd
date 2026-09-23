#!/bin/sh
# Snapshot local agent files onto orphan branch `agent-docs` (no checkout), push to fork.
# Restore on fresh clone: git fetch fork agent-docs && git checkout fork/agent-docs -- . && git reset -q
set -e
cd "$(git rev-parse --show-toplevel)"
export GIT_INDEX_FILE="$(git rev-parse --git-dir)/agent-docs.index"
rm -f "$GIT_INDEX_FILE"
for p in CLAUDE.md CONTEXT.md CONTEXT-MAP.md docs/agents docs/adr .scratch; do
  [ -e "$p" ] && git add -f "$p"
done
tree=$(git write-tree)
parent=$(git rev-parse -q --verify refs/heads/agent-docs || true)
if [ -n "$parent" ] && [ "$(git rev-parse "$parent^{tree}")" = "$tree" ]; then echo "no changes"; exit 0; fi
commit=$(git commit-tree "$tree" ${parent:+-p "$parent"} -m "agent docs $(date +%F)")
git update-ref refs/heads/agent-docs "$commit"
rm -f "$GIT_INDEX_FILE"
git push fork agent-docs
