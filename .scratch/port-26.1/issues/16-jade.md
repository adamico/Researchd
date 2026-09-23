# 16 — Jade

**What to build:** With Jade installed, looking at any block of a Research Lab shows the Lab's stored energy, its stocked Research Packs, and the research it's working on with its progress. A Lab Part shows the same as the Lab Controller. Energy and Research Packs come from Jade's built-in readers: the Lab Controller now exposes its energy and item handlers only when queried with no side, so pipes still can't connect to it. A Researchd Jade plugin adds the current research and its progress for both block types. Jade stays optional, and this work is on 26.1 only.

**Blocked by:** 08 — Lab Energy Draw on 26.1

**Status:** ready-for-agent

- [ ] Jade 26.1.11 is in the build (compileOnly) and the dev runtime
- [ ] The Lab Controller's capabilities answer only for no side; queries with a side still get nothing
- [ ] The plugin loads only when Jade is present; it has a server data provider and a tooltip line for the current research and progress, registered for the Lab Controller and the Lab Part
- [ ] Manual: energy, Research Packs, the current research and its progress show on the Lab Controller and on each Lab Part
- [ ] Manual: at draw 0 the energy line still shows the buffer (accepted)
- [ ] Manual: a pipe next to the Lab Controller doesn't connect
- [ ] Manual: without Jade, the game starts normally
