# 07 — Integrate: compile, datagen, the game loads

**What to build:** The `26.1` branch builds cleanly and the game runs. Datagen output is regenerated. A dedicated server and a client both start with Researchd. In a dev world with the draw at 0, a player can build a Research Lab, stock it with Research Packs, and watch the team's current research progress and complete. This is where the wide refactor turns green.

**Blocked by:** 03, 04, 05, 06

**Status:** ready-for-agent

- [ ] Full compile with no errors
- [ ] Datagen regenerated, including the 26.1 item model definitions; generated resources committed
- [ ] The dedicated server starts with no Researchd errors in the log
- [ ] The client starts, a world with the default Researchd datapack loads, and the research screen opens
- [ ] Manual: the Research Lab forms, accepts Research Packs through a Lab Part, and completes a research
- [ ] Manual: team create, join and leave work with native teams
- [ ] Anything noted by 03–06 is either fixed or filed as a follow-up ticket
