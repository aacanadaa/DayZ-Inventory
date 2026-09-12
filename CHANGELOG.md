# DayZ Inventory 1.4.3 (Minecraft 1.20.1)

**Fixes the Forge build crashing on launch.** Update if you are on 1.20.1 Forge.

If you downloaded 1.4.2 from Modrinth or CurseForge, that file was broken and this
one replaces it. Nothing else changed.

## Fixed: the Modrinth and CurseForge files were never reobfuscated

1.4.2 fixed this for the build and for GitHub, but not for publishing, so two
different 1.4.2 Forge jars existed:

| Where it came from | `Registries.` reference | Size |
| :--- | :--- | :--- |
| GitHub release | `f_256798_` | 967,133 bytes |
| Modrinth / CurseForge | `MENU` | 966,295 bytes |

Forge resolves members against SRG at runtime, so the Modrinth and CurseForge jars
died before the game window opened:

```
java.lang.NoSuchFieldError: MENU
    at DayZInventoryForge.<clinit>(DayZInventoryForge.java:41)
```

The reobfuscation step was wired to `assemble`, but `publishMods` read the `jar`
task's output and depended only on `jar` - and publishing on its own never runs
`assemble`. So the fix-up silently did not happen on the publishing path, and the
upload was of the development jar. `--dry-run` confirmed it: the publish task graph
contained no reobfuscation step at all.

Two separate problems were behind that. The reobfuscation step declared the *same
output file* as the `jar` task, so which file was correct depended on task ordering;
and publishing referenced a path rather than the task that produces it, so nothing
tied the upload to the reobfuscation.

Now the `jar` task writes to `build/devlibs`, a single task produces the reobfuscated
jar in `build/libs`, and publishing reads that task's output - so publishing depends
on reobfuscation instead of hoping another task ran first.

Releases are now checked by opening the built jar and confirming its own classes
reference SRG names, and by confirming `:forge:publishMods` shows the reobfuscation
step in its task graph.

## Not affected

Fabric on 1.20.1, and both loaders on 1.21.1 and 1.21.11, are unaffected and stay on
their current versions.

---

# DayZ Inventory 1.4.2 (Minecraft 1.20.1)

**Fixes the Forge build crashing on launch.** Update if you are on 1.20.1 Forge.

(1.4.1 was published but still failed to launch - it fixed one of two faults. Use 1.4.2.)

## Fixed: Forge crashed as soon as you opened a container

The 1.4.0 Forge jar failed during Mixin application, before the inventory screen
could appear:

```
@Shadow field menu was not located in the target class
net.minecraft.client.gui.screens.AbstractContainerScreen
```

One of the mixins shadowed four fields of the container screen, and those shadows
produce no refmap entry. Forge resolves shadow fields purely from the refmap, so it
went looking for fields literally named `menu` and failed. Fabric resolves them
through its own mapping resolver, which is why only Forge was affected.

The check now lives in the screen class itself, which inherits those fields and can
read them without shadowing anything. Behaviour is unchanged.

This slipped through because the development client runs on official names, where
the shadows resolve by name - only the packaged jar was ever affected. Releases are
now checked against the built jar, not just a development run.

## Fixed: Forge crashed during mod construction

With the mixin fault above out of the way, the next one appeared immediately:

```
java.lang.NoSuchFieldError: MENU
    at DayZInventoryForge.<clinit>(DayZInventoryForge.java:41)
```

The shipped Forge jar had **never been reobfuscated**. ForgeGradle writes the
reobfuscated jar to its own output directory, and the file that was being packaged -
the one in `build/libs` - kept the game's official field names. Forge runs on SRG,
so `Registries.MENU` did not exist at runtime; it should have been
`Registries.f_256798_`. The published jar contained zero SRG references anywhere.

The reobfuscated jar is now the one packaged, so `build/libs` always contains
something Forge can actually load.

## Why both of these reached you

Development runs use the game's official names, where both faults resolve cleanly.
`:forge:runClient` worked perfectly throughout, and only the packaged jar was ever
affected. Releases are now checked by inspecting the built jar itself rather than
trusting a successful build or a working development client.

## Also fixed

`mods.toml` declared its version by hand and had drifted - a 1.4.1 jar still
reported itself as 1.4.0 in crash reports. It now derives from the project version,
as `fabric.mod.json` already did.

## Not affected

Fabric on 1.20.1, and both loaders on 1.21.1 and 1.21.11, are unaffected by this
and stay on 1.4.0.

---

# DayZ Inventory 1.4.0 — now on Minecraft 1.21.11

**DayZ Inventory is now available for Minecraft 1.21.11 on Fabric and NeoForge.**

The DayZ UI is unchanged — Vicinity grid, Proximity Scanner, 2x2 crafting, the 2.0x Hands slot and
drag-to-equip all behave exactly as on 1.20.1 and 1.21.1. This release brings the mod to 1.21.11,
which is the last Minecraft release before the game moved to year-based versioning.

## Why 1.21.11 and not 26.x

Minecraft 26.1 was the first **unobfuscated** release. Mojang stopped publishing mappings, which
retired Yarn entirely, and 26.x also replaced the GUI rendering model — `GuiGraphics` is gone,
replaced by a render-state extraction pipeline. That makes it a rewrite of this mod's entire custom
screen rather than a port, so 1.21.11 is the current target. 1.20.1 and 1.21.1 continue to be
maintained.

## What changed for 1.21.11

Internal only, but it explains the scale:

- **`ResourceLocation` was renamed to `Identifier`** by Mojang, which touches most of the codebase.
- **The GUI transform stack went 2D** — `GuiGraphics.pose()` is now a `Matrix3x2fStack`, so the
  scale/translate calls lost their z argument.
- **Mouse input uses event objects** now (`MouseButtonEvent`), so click, drag and release overrides
  take events rather than raw coordinates. Shift-click reads the modifier bits off the event.
- **Tooltips are deferred** — `renderTooltip` became `setTooltipForNextFrame`.
- **`InventoryScreen.renderEntityInInventory` was removed.** The Survivor panel's player model now
  uses the follows-mouse variant, which also does the rotation the old code performed by hand. Note
  the new call *queues* its render state instead of drawing immediately, so its coordinates must be
  in screen space rather than the scaled layout space.
- **The container render pipeline was split.** The background and the screen contents are now
  separate passes, which changed where the DayZ panels can be drawn from.
- **`KeyboardHandler#keyPress` was removed**, so the recipe-viewer toggle simulates a keybind click
  through `KeyMapping` instead of a raw key event.
- Several fields became private with accessors: `Level.isClientSide()`, `Inventory.getSelectedSlot()`.
- `InteractionResult.sidedSuccess` was replaced by explicit per-side constants.

## NeoForge

The NeoForge module targets **21.11.x**. Three changes from the 1.21.1 build:

- `@EventBusSubscriber` no longer has a `bus` attribute.
- `PacketDistributor.sendToServer` was removed; client-to-server sending moved to
  `ClientPacketDistributor`.
- Dependency ranges updated to `neoforge [21.11,)` and `minecraft [1.21.11,1.21.12)`.

NeoForge needs no refmap: it has run on official Mojang names since 1.20.2, so mixin selectors
resolve as written.

## Downloads are now labelled with the Minecraft version

Jars are named `dayz-inventory-<loader>-<minecraft>-<version>.jar`, e.g.
`dayz-inventory-fabric-1.21.11-1.4.0.jar`. With the mod shipping for three Minecraft versions, the
old name was genuinely ambiguous.

## Still on an older version?

Nothing changes for you. 1.20.1 (Fabric and Forge) and 1.21.1 (Fabric and NeoForge) keep being built
and will keep receiving fixes — pick the download matching your Minecraft version.

---

# DayZ Inventory 1.4.0 — now on Minecraft 1.21.1

**DayZ Inventory is now available for Minecraft 1.21.1 on Fabric and NeoForge.**

The DayZ UI is unchanged — the Vicinity grid, Proximity Scanner, 2x2 crafting, the 2.0x Hands slot
and drag-to-equip all behave exactly as they do on 1.20.1. This release is about bringing the mod
to the current Minecraft version, and it took real work to get there: 1.21 was one of the largest
API breaks in recent memory.

## NeoForge, not Forge

The 1.21.1 build targets **NeoForge**. Forge does not carry forward past 1.20.x — the ecosystem
moved to NeoForge — so this is a new implementation, not a port of the old Forge module. The 1.20.1
line still ships Forge as before, and both lines are maintained.

One nice consequence: NeoForge has used official Mojang names at runtime with no remapping since
1.20.2, so the refmap machinery that Forge 1.20.1 required is gone entirely.

## What had to change for 1.21

These are internal, but they explain the scale of the update:

- **Networking was replaced.** 1.20.5+ dropped per-channel receivers and buffer-based sending in
  favour of typed payloads. The transport was rewritten for both loaders, and the payload class is
  now shared between Fabric and NeoForge.
- **Item NBT became data components.** `ItemStack#getTag` no longer exists, which affected stack
  comparison when picking items up from the Vicinity grid.
- **`Block#use` was split into two methods** (`useItemOn` and `useWithoutItem`), which is what the
  chest and barrel hooks target.
- **The recipe API was reworked** around `RecipeHolder` and `CraftingInput`, affecting the 2x2
  crafting result.
- **Several rendering and input signatures changed** — screen scrolling, background rendering and
  the player-model render used by the Survivor panel.
- **The menu-opening contract changed**: Fabric now passes typed data where NeoForge still passes a
  buffer, so the screen handler supports both shapes.

## Also in this release

- **Requires Java 21** on 1.21.1 (Minecraft 1.20.5+ refuses to run on 17). The 1.20.1 build still
  targets Java 17.
- All optional integrations still work and remain optional: JEI / REI / EMI, Curios, and Trinkets
  on Fabric. The mod loads and runs fine with none of them installed.

## Still on 1.20.1?

Nothing changes for you. 1.20.1 continues to be built and released for both Fabric and Forge, and
will keep receiving fixes. Pick the download matching your Minecraft version.

---

# DayZ Inventory 1.4.0 (Minecraft 1.20.1)

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
