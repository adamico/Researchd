# 02 — `/researchd` complete and remove commands work from the console (1.21.1)

**What to build:** On `main` (1.21.1), the research unlock and remove commands (`ResearchCommands.java:58, 81, 96, 112` on `main`) build their player lookup as `id -> id.equals(source.getPlayer().getUUID()) ? source.getPlayer() : null`. This causes two problems:
- Run from the console, a command block or an RCON source, `source.getPlayer()` is null and the command throws an NPE.
- Run by an op who isn't in the target team, the lookup never finds a member, so no Research Effect is applied or reversed and no member is notified.

The fix: look players up through the server's player list (`source.getServer().getPlayerList()::getPlayer`). Together with 01, the effects then no longer depend on who ran the command.

Found while designing 26.1 ticket 21 (Research Progression).

**Blocked by:** None — can start immediately (it overlaps 01; if 01 lands first, only the NPE and member notification remain)

**Status:** ready-for-agent

- [ ] Each of the four commands runs from the console without throwing
- [ ] An op outside the target team completes a research, and the team's online members get the completion message and the effect
- [ ] The command source gets feedback on what was completed or removed (it currently returns 0 silently)
- [ ] A GameTest on `main` covers a command run with no player source
