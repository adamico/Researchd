# 01 — Lab Energy Draw GameTests on 1.21.1

**What to build:** On `lab-energy` (1.21.1), the GameTest server runs the Lab Energy Draw scenarios against a real Research Lab and passes. These tests are the reference behaviour the 26.1 port must match, and they strengthen the open upstream PR. Each test builds a Research Lab from a structure, gives a team a current research, stocks Research Packs and supplies energy through a Lab Part, ticks the world, then checks only what a player could see.

**Blocked by:** None — can start immediately

**Status:** ready-for-agent

- [ ] Draw 0: the Lab progresses and ignores energy
- [ ] Draw above 0 with an empty buffer: no progress, no Research Pack used, no energy taken
- [ ] Less than one tick's draw stored: no progress, no Research Pack used, energy unchanged
- [ ] Fully powered: exactly one tick's draw is taken per tick of progress, and Research Packs are used at the normal rate
- [ ] Energy inserted through a Lab Part is accepted; extracting through a Lab Part gets nothing
- [ ] The gameTestServer run passes on a clean checkout
- [ ] Tests set the Lab Energy Draw config per test and restore it afterwards
- [ ] Pushing to the upstream PR is left to the maintainer
