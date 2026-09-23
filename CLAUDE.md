## Agent skills

### Issue tracker

GitHub Issues on the fork `adamico/Researchd` (always `gh --repo adamico/Researchd`). See `docs/agents/issue-tracker.md`.

### Domain docs

Single-context: `CONTEXT.md` + `docs/adr/` at repo root. See `docs/agents/domain.md`.

### Local-only files

CLAUDE.md, CONTEXT.md, docs/agents/, docs/adr/ and .scratch/ are listed in `.git/info/exclude`. Never commit them or force-add them; they must not reach upstream PRs.

After editing any of these files, run `docs/agents/sync-agent-docs.sh` to back them up to orphan branch `agent-docs` on the fork. Never merge `agent-docs` into other branches.
