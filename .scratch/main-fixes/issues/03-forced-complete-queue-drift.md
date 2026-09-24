# 03 — A forced complete keeps server and client research queues in step (1.21.1)

**What to build:** On `main` (1.21.1), `/researchd unlock` marks a research Researched but never removes it from the server's queue. Only the natural path in `ResearchdServerTickHandler:58` pops the queue head. The forced `ClientResearchCompletedPayload` makes each member's client pop its own queue head anyway (`ClientResearchCompletedPayload.java:63-74` on `main`). After a forced complete:
- **The server** still has the research in its queue. If it's the head, the next tick completes it again, which is a no-op because of the timestamp guard, and then pops it.
- **The client** has removed its head, which may be a different research when the forced one wasn't first.

The screens then show a queue the server doesn't have until the next `SyncTeamPayload`.

The fix: completing a research, forced or natural, removes it from the server queue wherever it sits. The client stops changing its queue on completion and relies on the team sync the server sends.

Found while designing 26.1 ticket 21 (Research Progression).

**Blocked by:** None — can start immediately

**Status:** ready-for-agent

- [ ] A forced complete of a queued research that isn't at the head removes exactly that research from the server queue
- [ ] After a forced complete, a member's client queue matches the server's, with no desync warning in the log
- [ ] A natural completion still pops the head and behaves the same for players
- [ ] A GameTest on `main` covers the non-head forced complete
