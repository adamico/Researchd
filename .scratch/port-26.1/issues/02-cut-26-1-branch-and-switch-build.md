# 02 — Cut the `26.1` branch and switch the build

**What to build:** A `26.1` branch on the fork, cut from the tip of `lab-energy`, whose build targets Minecraft 26.1.2 / NeoForge 26.1.2.109 and resolves every dependency. This is the "expand" step of the wide refactor: after it, compile errors are expected across the codebase, and tickets 03–06 fix them package by package (see ADR 0001).

**Blocked by:** None — can start immediately

**Status:** ready-for-agent

- [ ] Java 25 toolchain, Gradle 9.1 or newer, ModDevGradle 2.0.14x; Parchment removed
- [ ] NeoForge 26.1.2.109; MC range `[26.1.2, 26.2)`; mod version 1.3.0
- [ ] Porting-Dead-Libs 1.1.15
- [ ] EMI, Immersive Engineering, Create and KubeJS removed from dependencies. Their compat packages stay in the tree but out of the source set, and their mixins are removed from the mixin config
- [ ] JEI, FTB Teams/Library and Jade temporarily out of the source set too (they come back in 14, 15 and 16)
- [ ] Mixin compatibility level raised to match Java 25
- [ ] Spotless and the other build plugins still run on Java 25, or are updated
- [ ] Gradle sync and dependency resolution succeed; the remaining failures are Java compile errors only
- [ ] Local-only files (CLAUDE.md, CONTEXT.md, docs/agents, docs/adr, .scratch) aren't committed
