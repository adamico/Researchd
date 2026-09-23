# One long-lived branch per Minecraft version

Each supported Minecraft version lives on its own long-lived branch, named after the version line (`main` for 1.21.1 upstream, `26.1` for the port). Fixes and features are cherry-picked between them. We rejected a single multi-version codebase (Stonecutter or similar): the 1.21.1 → 26.1 gap (unobfuscated Minecraft, NeoForge's transfer API rewrite, the GUI render-state rework, the recipe system split) would put version conditionals in most files, and upstream would have to adopt the tooling too.

## Consequences

- A change needed on both lines is written twice, or cherry-picked and adapted.
- Behaviour parity between lines is checked by GameTests that exist on both branches, not by shared source.
