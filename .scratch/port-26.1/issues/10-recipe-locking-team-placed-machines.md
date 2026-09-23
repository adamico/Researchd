# 10 — Recipe locking in machines placed by a team

**What to build:** A machine placed by a team member processes recipes under that team's Team Context, so Locked recipes can't be automated around. The team that placed a block is still recorded, and the block entity's tick is again wrapped with that Team Context.

**Blocked by:** 09 — Recipe locking at the crafting table

**Status:** ready-for-agent

- [ ] GameTest: a furnace placed by a member of a team without the research refuses a Locked smelting recipe
- [ ] GameTest: once the research completes, the same furnace processes it
- [ ] GameTest: a furnace placed by a member of a team that has the unlock processes it, while another team is still Locked
- [ ] The block-entity tick mixin is back in the mixin config
