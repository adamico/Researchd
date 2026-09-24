# 01 — Research Effects apply and reverse with no team member online (1.21.1)

**What to build:** On `main` (1.21.1), completing or revoking a research applies or reverses its Research Effect even when no member of the team is online. Today `ResearchTeamImpl.onCompleteResearch` and `onRemoveResearch` take their `Level` from the first online member (`playerGetter.apply(...)`, `ResearchTeamImpl.java:237-264, 278-290` on `main`). If nobody is online they return before `onUnlock`/`onLock`. A Research Lab in a chunk-loaded base that finishes overnight therefore marks the research Researched but unlocks nothing, and the team never gets the effect.

The fix: effects always get the server overworld, where the team's SavedData lives. Members still only affect who receives the `ClientResearchCompletedPayload`.

Found while designing 26.1 ticket 21 (Research Progression), which fixes it on 26.1 as part of a larger refactor. This ticket is the small, cherry-pick-sized fix for `main`, not a port of that module.

**Blocked by:** None — can start immediately

**Status:** ready-for-agent

- [ ] A natural completion with no member online applies the Research Effect
- [ ] A revoke (`/researchd remove`) with no member online reverses it
- [ ] `ClientResearchCompletedPayload` is still sent only to online members
- [ ] A GameTest on `main` covers the completion case (ADR-0001: parity is checked by GameTests on both lines)
- [ ] Decide whether it goes upstream on its own, like the `101f616` question in 26.1 ticket 01
