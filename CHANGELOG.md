# DayZ Inventory 1.4.0

This is a maintenance and stability release. There are no gameplay or UI changes — the DayZ
Vicinity grid, Proximity Scanner, 2x2 crafting grid, 2.0x Hands slot render and drag-to-equip panel
all behave exactly as before. What changed is the build, the licensing, and the foundation the mod
sits on.

**Available for Minecraft 1.20.1 on Fabric and Forge.**

## Relicensed to Apache 2.0

DayZ Inventory is now open source under the **Apache License 2.0**. The previous "All Rights
Reserved" terms have been removed everywhere — `LICENSE`, `README.md`, `gradle.properties`,
`build.gradle`, `fabric.mod.json` and `mods.toml`.

You are now free to use, modify, redistribute and ship this mod in modpacks without asking for
permission first.

## Fixed: Fabric would not load

This was the significant one. The shared mixin configuration declares a refmap named
`dayz-inventory.refmap.json`, but the Fabric build was actually producing `common-refmap.json`,
because the mixins live in the shared `common` module and Loom names refmaps after the project
directory.

The result was that Fabric shipped a refmap under a name nothing referenced, so every mixin
failed to remap at runtime. The mod's core behaviour — blocking vanilla auto-pickup, redirecting
chests and barrels into the DayZ container UI, and swapping the inventory screen — could not
apply.

The Fabric refmap is now generated under the declared name and verified to contain the correct
`named:intermediary` mappings.

Verified against a real Fabric 1.20.1 server: the mod loads, all four server mixins apply and the
server starts.

### Known limitation: Fabric development runs

`./gradlew :fabric:runClient` and `:fabric:runServer` start the game but do not apply mixins, so
the DayZ inventory screen is not redirected in a Fabric *development* environment. This is a
limitation of running Mixin on Fabric with Mojang official mappings, not a problem with the
released mod — the packaged Fabric jar is unaffected and was verified working.

If you are developing against the GUI, use `./gradlew :forge:runClient`, where mixins resolve
correctly. To test Fabric behaviour, drop the built jar into a real Fabric installation.

## Fixed: Forge crashed on startup

Forge was failing during Mixin application, before the game window or server console ever came up:

```
@Inject annotation on onPlayerTouch specifies a target class
'net/minecraft/class_1542', which is not supported
```

`class_1542` is the intermediary name for `ItemEntity`. Forge runs on SRG, not intermediary, so
Mixin was resolving the wrong refmap and could not apply a single mixin.

The cause was a duplicated mod classpath. The Forge project already compiled the shared `common`
sources directly into its own output, but the shared module was *also* being added as a separate
mod source and project dependency. That put a second copy of the mixin configs on the classpath
alongside `common`'s intermediary refmap, and Mixin picked that one up.

Forge no longer puts `common` on the mod classpath at all — it compiles and packages those sources
itself, and ships a single refmap containing correct `searge` mappings. Verified against a real
Forge 1.20.1 server: all four server mixins apply and the server starts.

## Fixed: Forge development runs

`./gradlew :forge:runClient` and `:forge:runServer` were failing for a second, related reason.
MixinGradle only injects the generated refmap into the packaged jar, never into the build output
that development runs load from, so those runs fell back to the wrong refmap. The correct refmap is
now copied into the Forge resources output as well.

## Compatibility: optional mods degrade gracefully

JEI, REI, EMI, Trinkets and Curios remain fully optional, and the code paths that probe for them
have been hardened so a missing optional dependency can never crash the game:

- Mod presence checks are now null-safe. If a probe runs before the loader has installed its
  platform helper, it reports "not loaded" instead of throwing.
- The chest and barrel mixins fall through to vanilla behaviour if the helper is not ready, rather
  than dereferencing it.
- The inventory redirect falls back to showing the vanilla inventory screen instead of crashing.

The recipe viewer button only appears when JEI, REI or EMI is actually present, and the
CURIOS / TRINKETS buttons only appear when those mods are present.

## Dependency metadata

- Fabric now declares `fabricloader >=0.15.0` instead of requiring a single pinned loader build,
  and lists JEI, REI, EMI, Trinkets and Curios as suggested rather than required.
- Forge metadata adds JEI as an optional dependency and corrects the Curios load ordering.

## Build and tooling

- **`gradlew` is now committed as executable.** It was previously stored non-executable, which
  broke `./gradlew` for anyone cloning the repository.
- **Removed the hardcoded Windows JDK path** from `gradle.properties`. It pointed at one
  contributor's machine and made CI and other developers' builds fail. Set `JAVA_HOME` instead.
- Pinned Fabric Loom to `1.6.12` rather than a moving `1.6-SNAPSHOT`.
- Increased the Gradle heap so the Forge toolchain stops running out of memory.

## GitHub Actions

The build workflow was rewritten. It previously uploaded `build/libs/`, which contains no mod
jars, and ran on JDK 25 against a Gradle version that does not support it.

It now runs on JDK 17, builds both loaders, uploads the Fabric and Forge jars as separate
artifacts, and fails loudly if either is missing. Pushing a `v*` tag additionally attaches both
jars to a GitHub Release.

## Issue tracking

Bug reports now go to this repository's own tracker:
<https://github.com/aacanadaa/DayZ-Inventory/issues>

The previous separate issue-tracker repository has been retired and all links in the README have
been updated.

## Repository housekeeping

- Removed a stray `err.log` and an unreferenced image that had been committed by accident.
- Rewrote `.gitignore` and `.gitattributes` so build output, run directories, logs and secrets
  stay out of the repository, and line endings are consistent across platforms.
- Added a `CLAUDE.md` documenting the multi-loader architecture and the build traps in this
  project.

## Publishing

Modrinth and CurseForge publish tasks now derive their display names from the project version
instead of hardcoding "1.3.0", so they can no longer go stale. Tokens continue to be read from
the `MODRINTH_TOKEN` and `CURSEFORGE_TOKEN` environment variables — nothing is hardcoded.

---

# DayZ Inventory 1.3.0

- Forge support restored, with Forge builds working natively (no Sinytra Connector required).
- Dependency metadata updated: Curios and Trinkets declared as optional.
- Issue tracking moved to a separate public tracker repository (since retired).

# DayZ Inventory 1.2.0

- Trinkets compatibility, with a TRINKETS button in the Survivor column header that temporarily
  opens the vanilla inventory screen so Trinkets can initialise and render its slots.
- Curios and Trinkets buttons sit side-by-side when both are installed, and align to the right
  when only one is present.
- Simplified the recipe viewer toggle to simulate an `O` keypress (GLFW 79), the standard shortcut
  for toggling the JEI / REI / EMI overlay.
- Compatibility confirmed for JEI, REI, EMI, Curios and Trinkets on Minecraft 1.20.1 (Fabric and
  Forge), Java 17+.
