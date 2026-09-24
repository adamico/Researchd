# 14 — JEI back in

**What to build:** With JEI installed, each Research Pack variant appears as a separate JEI entry, and Researchd can open a recipe's JEI page, as on 1.21.1. JEI stays optional.

**Blocked by:** 12 — Recipe displays on the research screen

**Status:** ready-for-agent

- [ ] The JEI 26.1.2 API is back in the build (compileOnly), the JEI runtime is in the dev client, and the compat package is back in the source set. The runtime part is done: 07 added `localRuntime` JEI 29.40.0.102 (`jei_version` in `gradle.properties`).
- [ ] Research Pack subtypes register with JEI's 26.1 API
- [ ] Opening a recipe page works from a recipe key or display
- [ ] Manual: without JEI, the game starts and the research screen works
