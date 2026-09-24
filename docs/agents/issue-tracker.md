# Issue tracker: local markdown in `.scratch/`

Tickets and specs are markdown files under `.scratch/`. **GitHub Issues are disabled on the fork `adamico/Researchd`**, so don't use `gh issue` (it fails), and never open issues on upstream `Porting-Dead-Mods/Researchd`. `.scratch/` is local-only (listed in `.git/info/exclude`), so never commit it to a code branch.

## Layout

Each **track** is a folder with an optional spec and a numbered `issues/` directory:

```
.scratch/
├── port-26.1/            ← the 26.1 port (branch 26.1)
│   ├── spec.md           ← the track's PRD (frontmatter: title, status, created)
│   └── issues/NN-slug.md
└── main-fixes/           ← 1.21.1 bug fixes (branch main)
    └── issues/NN-slug.md
```

Start a new track folder when the work doesn't belong to an existing one (e.g. a different branch or initiative). Numbers are per track, two digits, and never reused.

## Ticket format

```markdown
# NN — Title in plain words

**What to build:** what should be true when the ticket is done, and why.

**Blocked by:** None — can start immediately   |   NN — Title of blocker (reason), NN, …

**Status:** ready-for-agent

- [ ] Acceptance criterion
- [ ] …
```

Optional sections after the checklist: `## Shape`, `## Out of scope`, `**Notes from implementation:**`, `**Open:**`.

**Status values:**
- `ready-for-agent`: can be picked up once every blocker is done
- `blocked`: waiting on something the `Blocked by` line doesn't cover
- `needs-human`: needs a manual step or a maintainer decision
- `done (<commits and branch>; <what's left to the maintainer>)`
- `agent part done (…)`: the agent's work is finished, but manual checks remain

## Operations

- **Create a ticket**: write `.scratch/<track>/issues/NN-slug.md`, with the next free number in that track.
- **Read a ticket**: read its file. For context, also read the track's `spec.md` and the tickets it's blocked by.
- **List tickets**: `grep -H '^\*\*Status:\*\*' .scratch/<track>/issues/*.md` (and `^\*\*Blocked by:\*\*`).
- **Comment**: append to `**Notes from implementation:**` or `**Open:**` in the ticket.
- **Close**: set `**Status:** done (…)`, tick the checklist, and add notes on what was found along the way.
- **After any edit** under `.scratch/`, run `docs/agents/sync-agent-docs.sh` to back up to the fork's `agent-docs` branch.

## "Next ticket"

In the current branch's track (`26.1` → `port-26.1`, `main` → `main-fixes`), pick the lowest-numbered ticket whose status is `ready-for-agent` and whose every `Blocked by` ticket is `done`. If the track has nothing ready, say so and list what blocks the lowest-numbered open tickets.

## Pull requests as a triage surface

**PRs as a request surface: no.** Upstream PRs are opened by the maintainer and aren't triaged here.

## When a skill says "publish to the issue tracker"

Create a ticket file in the right track, as above.

## When a skill says "fetch the relevant ticket"

Read the ticket file, plus the track's `spec.md` if it has one.

## Wayfinding operations

Used by `/wayfinder`. The **map** is the track's `spec.md` (or a `map.md` for a track with no spec), holding Notes / Decisions-so-far / Fog. The **child tickets** are that track's `issues/` files.

- **Blocking**: the ticket's `**Blocked by:**` line. A ticket is unblocked when every blocker's status is `done`.
- **Frontier query**: the same as "Next ticket", but return every unblocked `ready-for-agent` ticket in number order.
- **Claim**: set the status to `in-progress (<session/date>)`. That's the session's first write.
- **Resolve**: close the ticket (above), then add a one-line pointer (gist + ticket number) to the map's Decisions-so-far.
