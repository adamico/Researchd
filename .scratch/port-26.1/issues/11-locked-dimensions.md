# 11 — Locked dimensions

**What to build:** A player whose team hasn't unlocked a dimension can't travel into it (creative mode is exempt), as on 1.21.1. The dimension-change hook targets the renamed 26.1 entity method.

**Blocked by:** 08 — Lab Energy Draw on 26.1 (GameTest wiring)

**Status:** ready-for-agent

- [ ] GameTest: a non-creative player in a team with the dimension Locked can't change into it
- [ ] GameTest: the same player can once the dimension-unlock research completes
- [ ] GameTest: a creative player bypasses the lock
- [ ] The Locked-dimension mixin is back in the mixin config
