# 04 — Migrate batch: server logic and the Research Lab

**What to build:** Server-side logic compiles against 26.1: teams, research progress, research methods and effects, networking payloads, events, block entities and the vanilla mixins. The Research Lab moves onto Porting-Dead-Libs handlers: items through a PDL resource handler, energy through a PDL simple energy handler. The Lab Energy Draw pays each tick through a transfer-API transaction that commits only when the full draw was extracted. Lab Parts expose the Lab Controller's items, plus an insert-only energy wrapper, on every side (strict parity, even at draw 0). Recipe locking is temporarily switched off so this batch stays mechanical.

**Blocked by:** 02 — Cut the `26.1` branch and switch the build

**Status:** ready-for-agent

- [ ] No compile errors remain in the impl, events, networking, content or vanilla-mixin packages
- [ ] Block entities save and load through 26.1 value I/O
- [ ] The Lab Energy Draw rules are unchanged: all or nothing per tick, the check sits after the check that Research Packs are present and before a pack is used, a draw of 0 turns it off, capacity is also the transfer limit, and the client sync stays throttled
- [ ] Lab Parts: item handler and insert-only energy on every side; the Lab Controller exposes nothing yet (16 adds null-side access)
- [ ] The config write-back workaround is still in place and wired to the 26.1 config events
- [ ] The Research Effects' server-side recipe resolution uses the 26.1 recipe map
- [ ] Recipe locking temporarily switched off: the recipe-lookup, crafting-grid and block-entity-ticking mixins are removed from the mixin config, and the shared "is this recipe Locked" check answers "never Locked", with a marker pointing to 09 and 10
- [ ] The Locked-dimension mixin is removed from the mixin config, with a marker pointing to 11
- [ ] The reload-listener hook targets the 26.1 server-resources constructor
- [ ] Effect data is still synced to all players (parity)
