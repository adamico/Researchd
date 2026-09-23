# 18 — PDL config write-back fix

**What to build:** A fix to Porting-Dead-Libs (`main`, 26.1) so that loaded config values are copied into the annotated config fields of the mod that owns the config, rather than only for PDL's own mod. Once a PDL release includes it, Researchd can drop its workaround. The work happens in a Porting-Dead-Libs checkout, not in this repo.

**Blocked by:** None — can start immediately

**Status:** ready-for-agent

- [ ] The config load and reload listener runs for every mod that registers a PDL config, not just PDL
- [ ] Checked with a mod whose config values differ from the Java defaults
- [ ] A PR description is drafted, pointing to Researchd's workaround as the symptom
- [ ] Opening the PR upstream is left to the maintainer
- [ ] A follow-up is noted for Researchd: remove the workaround once a PDL release ships the fix
