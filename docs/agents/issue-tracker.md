# Issue tracker: GitHub Issues on the fork

Issues live in **`adamico/Researchd`** (the fork). Pass `--repo adamico/Researchd` to every `gh issue` command. Without it, `gh` may pick `origin`, which is upstream. Never open or comment on issues in upstream `Porting-Dead-Mods/Researchd`.

Reference material that isn't a ticket, such as research notes, scripts and dev datapacks, stays in `.scratch/<track>/`. That folder is local-only and listed in `.git/info/exclude`.

## Tracks

A **track** is a label:

- `port-26.1`: the 26.1 port (branch `26.1`). Issues #1–#21 keep their old ticket numbers, so `TODO(26.1 port, NN)` markers in the code point to #NN.
- `main-fixes`: 1.21.1 bug fixes (branch `main`). Old tickets 01–03 are now #22–#24.
- `unofficial-port`: publishing the fork's `26.1` line as "Researchd (Unofficial 26.1 Port)" on Modrinth and CurseForge. Spec #39.

A track's spec (PRD) is an issue labelled `spec` plus the track label. It stays open and pinned. The spec for `port-26.1` is #25.

Start a new track by creating a new label when the work doesn't belong to an existing track, for example a different branch or initiative.

## Issue format

Title: the ticket's title in plain words, with no number. Body:

```markdown
**What to build:** what should be true when the ticket is done, and why.

**Blocked by:** None — can start immediately   |   #N — Title of blocker (reason), #N, …

- [ ] Acceptance criterion
- [ ] …
```

Optional sections after the checklist: `## Shape`, `## Out of scope`. Notes from implementation and open questions go in comments.

**Status is a label** on open issues:

- `ready-for-agent`: can be picked up once every blocker is closed
- `in-progress`: claimed by a session
- `blocked`: waiting on something the `Blocked by` line doesn't cover
- `needs-human`: needs a manual step or a maintainer decision

**Done** means the issue is closed as completed. The closing comment names the commits and branch, plus anything left to the maintainer (for example "push left to maintainer"). If the agent's work is finished but manual checks remain, keep the issue open, switch the label to `needs-human`, and say what's left in a comment.

## Operations

- **Create**: `gh issue create --repo adamico/Researchd --title "…" --label <track> --label ready-for-agent --body-file <file>`
- **Read**: `gh issue view N --repo adamico/Researchd --comments`. For context, also read the track's spec issue and the issues it's blocked by.
- **List**: `gh issue list --repo adamico/Researchd --label <track> --state open --json number,title,labels,body`
- **Comment**: `gh issue comment N --repo adamico/Researchd --body "…"`
- **Change status**: `gh issue edit N --repo adamico/Researchd --remove-label ready-for-agent --add-label in-progress`
- **Close**: tick the checklist in the body (`gh issue edit N --body-file …`), then `gh issue close N --repo adamico/Researchd --reason completed --comment "Done in <commits> on <branch>; <what's left to the maintainer>"`

## "Next ticket"

Use the current branch's track (`26.1` → `port-26.1`, `main` → `main-fixes`). Pick the lowest-numbered open issue labelled `ready-for-agent` whose `Blocked by` issues are all closed. If nothing in the track is ready, say so and list what blocks the lowest-numbered open issues.

## Pull requests as a triage surface

**PRs as a request surface: no.** The maintainer opens upstream PRs, and they aren't triaged here.

## When a skill says "publish to the issue tracker"

Create an issue in the right track, as above.

## When a skill says "fetch the relevant ticket"

Run `gh issue view N --repo adamico/Researchd --comments`, and also read the track's spec issue.

## Wayfinding operations

Used by `/wayfinder`. The **map** is the track's spec issue (or an issue labelled `map` for a track with no spec), holding Notes / Decisions-so-far / Fog. The **child tickets** are the track's other issues.

- **Blocking**: the issue's `**Blocked by:**` line. An issue is unblocked when every blocker is closed.
- **Frontier query**: the same as "Next ticket", but return every unblocked `ready-for-agent` issue in number order.
- **Claim**: swap `ready-for-agent` for `in-progress`. That's the session's first write.
- **Resolve**: close the issue (above), then add a one-line pointer (gist + `#N`) to the map's Decisions-so-far by editing the map issue's body.
