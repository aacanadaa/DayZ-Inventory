# DayZ Inventory 1.7.0 — Forge on 1.21.1, and the whole matrix publishes itself

**Forge support is back, on 1.21.1.** The unified build now produces **seven** jars from one source
tree: Fabric and NeoForge for 26.2, 1.21.11 and 1.21.1, plus Forge for 1.21.1.

Nothing about the inventory screen changed. The Vicinity grid, Proximity Scanner, 2x2 crafting, the
2.0x Hands slot, drag-to-equip and every optional integration behave exactly as before.

## What is available now

| Minecraft | Fabric | NeoForge | Forge | Java |
| :--- | :---: | :---: | :---: | :---: |
| 26.2 | ✅ | ✅ | — | 25 |
| 1.21.11 | ✅ | ✅ | — | 21 |
| 1.21.1 | ✅ | ✅ | ✅ | 21 |

Forge stopped at 1.20.x — the ecosystem moved to NeoForge — so 1.21.1 is the newest version Forge
can be built for at all.

## Why Forge needed real work

The Forge module was still the 1.20.1-era `SimpleChannel` / `NetworkRegistry` /
`registerMessage` stack, which no longer exists. It has been ported to the same typed-payload model
the Fabric and NeoForge modules use, against `net.minecraftforge` packages, and shares the same
`DayZInventoryPayload` and packet-dispatch code as the rest of the mod.

Building it took three attempts, which is worth writing down:

- **ForgeGradle 6** only supports Gradle 8, and the newer Minecraft targets need Gradle 9 for Loom.
- **ModDevGradle's `legacyforge`** asks for `net.minecraftforge:forge:<version>:universal-srg` — a
  classifier that only exists for the pre-1.20.2 SRG layout — so it cannot build 1.21.1 at all.
- **ForgeGradle 7** is the rewrite that runs on Gradle 9, and is what the module now uses.

Since Forge has run on official Mojang names since 1.20.2, there is no reobfuscation step and no
Searge refmap, exactly as on NeoForge.

## 1.20.1 is still on its own branch

1.20.1 predates the 1.20.5 networking rewrite — no custom payload records, and
`ExtendedScreenHandlerType` does not take an opening-data codec yet — so it needs a genuine source
port rather than a rebuild. It keeps shipping from the `1.20.1` branch, and the exact breakpoints
are documented in `docs/BUILDING.en.md` for whoever picks it up.

## Publishing

Every jar now goes up with its own Minecraft version and loader tags, read from the build rather than
typed per release, so nothing can be filed under the wrong game version. `./gradlew publishAll`
publishes the matrix; on a `v*` tag CI builds everything, attaches the jars to the GitHub Release and
publishes to both platforms.

Note that CurseForge routes every upload through human review and never returns a link, so a green
CurseForge run means *submitted*, not *live*.

---

# DayZ Inventory 1.6.0 — one source tree, three Minecraft versions

**The mod is unchanged. How it is built is not.**

Until now every Minecraft version lived on its own Git branch: `main` for 1.20.1, and `1.21.1`,
`1.21.11` and `26.2` alongside it. A bug fix had to be merged into each of them, and each merge had
to be built and tested on its own. This release replaces that with a single branch that builds
1.21.1, 1.21.11 and 26.2 for **both Fabric and NeoForge** from one source tree, and publishes all of
them automatically.

Nothing about the inventory screen changed. The Vicinity grid, Proximity Scanner, 2x2 crafting, the
2.0x Hands slot, drag-to-equip and every optional integration behave exactly as before.

## What this means for you

- **Download the same way.** The files on Modrinth and CurseForge are still one per Minecraft
  version and loader; the names now look like `dayz-inventory-fabric-26.2-1.6.0+mc26.2.jar`, with the
  Minecraft version spelled out.
- **1.20.1 is not gone.** The 1.20.1 files (Fabric and Forge) remain published and still work. They
  come from the older per-version build and are not part of the unified tree yet — see below.
- **Forge is not in the unified tree either.** Forge stopped at 1.20.x, so the only Forge targets
  ever were 1.20.1 and 1.21.1.

## How it is built now

[Stonecutter](https://stonecutter.kikugie.dev/) preprocesses one shared source tree for several
Minecraft versions, and the loader modules stay exactly as they were:

```
common/     loader-agnostic code, shared by every version
fabric/     Fabric entrypoints
neoforge/   NeoForge entrypoints
forge/      legacy Forge module (present, not built)
```

Version differences are marked inline with `//? if` comments, so a method whose signature changed
between 1.21.1 and 26.2 reads as one file with the two variants next to each other instead of as two
branches that drift apart. Renames that touch dozens of lines — `ResourceLocation` to `Identifier`,
`isClientSide` to `isClientSide()`, `GuiGraphics` to `GuiGraphicsExtractor` — are declared once in the
build and applied automatically.

| Minecraft | Fabric | NeoForge | Java |
| :--- | :---: | :---: | :---: |
| 26.2 | ✅ | ✅ | 25 |
| 1.21.11 | ✅ | ✅ | 21 |
| 1.21.1 | ✅ | ✅ | 21 |

```bash
./gradlew chiseledBuild          # every version × every loader
./gradlew :fabric:26.2:build     # one target
```

## Automated publishing

Every jar is now published with its own Minecraft version and loader tags, taken from
`versions/<mc>/gradle.properties` rather than typed per release, so an artifact can no longer go up
under the wrong game version. `./gradlew publishAll` releases the whole matrix; on a `v*` tag CI
builds everything, attaches the jars to the GitHub Release and publishes to both platforms.

Note that CurseForge routes every upload through human review: the API accepts the file and never
returns a link, so a green CurseForge run means *submitted*, not *live*.

## Still to come

1.20.1 and Forge are scaffolded but not ported. 1.20.1 predates the 1.20.5 networking rewrite, so it
has no custom payload records at all, and `forge/` still holds 1.20.1-era `SimpleChannel` code. The
breakpoints are written down in `docs/BUILDING.en.md` so the port is a matter of following the list.

---

# DayZ Inventory 1.5.0 — now on Minecraft 26.2

**DayZ Inventory is now available for Minecraft 26.2 on Fabric and NeoForge.**

The DayZ UI is unchanged — the Vicinity grid, Proximity Scanner, 2x2 crafting, the 2.0x Hands slot
and drag-to-equip all behave exactly as on 1.21.11, 1.21.1 and 1.20.1. This release is about reaching
the current Minecraft version, and it is the port the 1.4.0 notes said could not be a port.

## The rewrite the last release deferred

The 1.4.0 entry explained why the mod stopped at 1.21.11: Minecraft 26.1 was the first **unobfuscated**
release, and 26.x replaced the GUI rendering model, which made it "a rewrite of this mod's entire
custom screen rather than a port". That rewrite is this release.

Two changes account for almost all of it:

- **Minecraft is no longer obfuscated.** Mojang stopped publishing mappings at 26.1, so there is
  nothing to remap: no mappings, no refmaps, no remap step, no reobfuscation. The build got simpler
  rather than harder. Loom's plugin id moved to `net.fabricmc.fabric-loom`, the `mod*` dependency
  configurations are gone (plain `implementation` now), and `remapJar` no longer exists.
- **The GUI draws through a render-state pipeline.** `GuiGraphics` is gone, replaced by
  `GuiGraphicsExtractor`, and every `render*` method became `extract*`. Nothing draws immediately any
  more — each call records state that the game replays later.

One consequence is worth spelling out, because it is not obvious: a mixin whose target has been
renamed does not degrade, it **crashes**. This mod's client mixins are declared `required: true`, so
the redirect that swaps the vanilla inventory for the DayZ screen — which hooked
`Minecraft#setScreen` — would have failed at launch rather than quietly doing nothing.
`Minecraft#setScreen` was deleted in 26.2, so that hook moved to `Gui#setScreen`, which is where both
the E-key path and `setScreenAndShow` actually go.

## What changed

Internal only, but it explains the scale:

- **`renderBg` was removed outright.** `AbstractContainerScreen` no longer has that extension point,
  so the DayZ panel drawing became a private helper called from the screen's own render state.
- **`drawString` became `text`**, and **`ClickType` became `ContainerInput`**.
- **`Recipe#assemble` dropped its `RegistryAccess` argument**, which affected the 2x2 crafting result.
- **`imageWidth`/`imageHeight` are now `final`**, so the screen size goes through the constructor
  instead of being assigned in the subclass.
- **Fabric API restructured.** `fabric-screen-handler-api-v1` is now `fabric-menu-api-v1`
  (`ExtendedScreenHandlerFactory`/`ExtendedScreenHandlerType` became
  `ExtendedMenuProvider`/`ExtendedMenuType`), following Mojang's screen-handler → menu rename, and
  `PayloadTypeRegistry#playC2S` became `serverboundPlay`.
- **Fabric development runs now apply mixins.** The long-standing caveat that `:fabric:runClient`
  silently skips this mod's mixins was a consequence of the mapping/refmap mismatch, which no longer
  exists. On this branch the dev client shows the real screen.

## Java 25

**Minecraft 26.2 requires Java 25** (Mojang ship it as `java-runtime-epsilon`). The 1.21.x branches
stay on Java 21 and 1.20.1 on Java 17.

## NeoForge

The NeoForge module targets **26.2.x**, with dependency ranges updated to `neoforge [26.2,)` and
`minecraft [26.2,26.3)`. As before, NeoForge needs no refmap — it has run on official Mojang names
since 1.20.2.

Forge is not built on this line. The ecosystem moved to NeoForge past 1.20.x; the 1.20.1 branch still
ships Forge.

## Still on an older version?

Nothing changes for you. 1.20.1 (Fabric and Forge), 1.21.1 and 1.21.11 (Fabric and NeoForge) keep
being built and will keep receiving fixes — pick the download matching your Minecraft version.

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
