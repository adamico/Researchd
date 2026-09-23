# 08 — Lab Energy Draw on 26.1, proven by GameTests

**What to build:** The GameTest server on 26.1 runs the same Lab Energy Draw scenarios as ticket 01, rewritten for 26.1's data-driven test instances, and they pass. This sets up the 26.1 GameTest wiring (registration, structures, and helpers for a Research Lab and a team) that later tickets reuse.

**Blocked by:** 01 — Lab Energy Draw GameTests on 1.21.1; 07 — Integrate

**Status:** ready-for-agent

- [ ] The 26.1 GameTest wiring is in place and documented in the ticket's closing note, so 09–11 can reuse it
- [ ] All five scenarios from 01 have identical setup and assertions, and pass
- [ ] Energy supplied through a Lab Part via the 26.1 energy capability; extracting through a Lab Part gets nothing
- [ ] Manual: the GUI energy bar shows when the draw is above 0 and is hidden at 0; config edits take effect after a reload
