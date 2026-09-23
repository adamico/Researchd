# 02 — Cut the `26.1` branch and switch the build

**What to build:** A `26.1` branch on the fork, cut from the tip of `lab-energy`, whose build targets Minecraft 26.1.2 / NeoForge 26.1.2.109 and resolves every dependency. This is the "expand" step of the wide refactor: after it, compile errors are expected across the codebase, and tickets 03–06 fix them package by package (see ADR 0001).

**Blocked by:** None — can start immediately

**Status:** done (commit on `26.1`; push left to maintainer)

- [x] Java 25 toolchain, Gradle 9.1 or newer, ModDevGradle 2.0.14x; Parchment removed
- [x] NeoForge 26.1.2.109; MC range `[26.1.2, 26.2)`; mod version 1.3.0
- [x] Porting-Dead-Libs 1.1.15
- [x] EMI, Immersive Engineering, Create and KubeJS removed from dependencies. Their compat packages stay in the tree but out of the source set, and their mixins are removed from the mixin config
- [x] JEI, FTB Teams/Library and Jade temporarily out of the source set too (they come back in 14, 15 and 16)
- [x] Mixin compatibility level raised to match Java 25
- [x] Spotless and the other build plugins still run on Java 25, or are updated
- [x] Gradle sync and dependency resolution succeed; the remaining failures are Java compile errors only
- [x] Local-only files (CLAUDE.md, CONTEXT.md, docs/agents, docs/adr, .scratch) aren't committed

**Notes:**
- Gradle 9.2.1 (the 26.1 MDK's version), foojay resolver 1.0.0. `loaderVersion` dropped from `neoforge.mods.toml`, as in the 26.1 MDK.
- Spotless 8.10.2 with palantir-java-format pinned to 2.75.0: releases up to at least 2.70 crash when Gradle runs on Java 25, and 2.80+ reformat text blocks.
- The data run is `clientData()` (MDG 2 split), so the datagen subscriber needs the client gather-data event (07).
- Code outside the excluded packages still imports them. The migrate batches own those call sites (see the extra item in 03–06).
- Porting-Dead-Libs 1.1.15 from JitPack reports mod version 1.1.16; the `[1.1.15, 1.2.0)` range accepts it.
- `.github/workflows/build-and-release.yml` still sets up Java 21; the toolchain auto-provisions 25. Worth bumping in 17.
- Jade had no code or dependency on `lab-energy`, so there was nothing to exclude; it arrives new in 16.
