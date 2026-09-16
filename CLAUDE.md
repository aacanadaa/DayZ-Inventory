# CLAUDE.md

Guidance for Claude Code when working in this repository.

## Project Overview

**DayZ Inventory** is a Minecraft mod that replaces the vanilla inventory screen with a DayZ-style
UI: a Vicinity grid (nearby ground items + containers), a dynamic Hands attachment slot, an
integrated 2x2 crafting grid, and drag-to-equip onto the Survivor panel.

It ships for **three loaders from one codebase**: **Fabric**, **NeoForge** and **Forge**.

- **Mod version**: `1.7.2`
- **Group / package root**: `com.suoim.dayzinventory`
- **Mod ID**: `dayz_inventory`
- **License**: Apache License 2.0 (see `LICENSE`)
- **Modrinth slug**: `dayz-inventory` (project id `8asZxzdc`), CurseForge project `1596267`

### One source tree, three Minecraft versions

This is a **Stonecutter** project. There is no longer a branch per Minecraft version. The enabled
matrix is **26.2, 1.21.11 and 1.21.1** — Fabric and NeoForge for all three, plus Forge on 1.21.1,
which is seven artifacts from one tree — and the version differences are marked inline with
`//? if` comments.

**Read [docs/BUILDING.en.md](docs/BUILDING.en.md) first.** It covers the project tree, the
convention plugins, the conditional-compilation conventions and the publishing setup. The rest of
this file is the accumulated "things that have actually broken here" list.

26.2 is the newest target and the one that differs most: it is the first line built on an
**unobfuscated** Minecraft and the first with the rewritten GUI render model. Both changes are
structural, not cosmetic — read *Critical Gotchas* before touching build config or anything under
`client/`.

## Repository Layout

Stonecutter branches, one per module, each producing one node per Minecraft version it supports
(`:fabric:26.2`, `:neoforge:1.21.11`, ...):

```
settings.gradle.kts          the version matrix + the Stonecutter tree
stonecutter.gradle.kts       controller: active version, chiseledBuild, publishAll, matrix
gradle.properties            mod metadata, publishing ids, shared tool versions
build-logic/                 convention plugins: dayz-common and dayz-loader
versions/<mc>/gradle.properties   one file per Minecraft version - its only coordinates
common/     Loader-agnostic code: screen, menu, packets, mixins, platform interface
fabric/     Fabric entrypoints + FabricPlatformHelper
neoforge/   NeoForge entrypoints + NeoForgePlatformHelper
forge/      Forge entrypoints - built for 1.21.1 only (Forge has no later releases)
```

`<branch>/versions/<mc>/` are build directories; they are gitignored and regenerated.

- `common/` uses **Loom** for its Minecraft dependency, but its code must stay loader-neutral. It
  must never import `net.fabricmc.*` or NeoForge classes outside of the platform abstraction.
- Loader-specific behaviour is reached through `IPlatformHelper`
  (`common/src/main/java/com/suoim/dayzinventory/platform/`). The active implementation is assigned
  to the static field `Platform.HELPER` during each loader's initialization.
- Each loader jar compiles the `common` node's **processed sources**, not its jar. Do not "simplify"
  that to a project dependency: below 26.1 the mixin refmap can only be produced from annotated
  sources, and a precompiled `common.jar` would hand Mixin classes it can never remap.

### Forge is built with ForgeGradle 7, not ModDevGradle or ForgeGradle 6

`forge/` was ported off the 1.20.1-era `SimpleChannel` / `NetworkRegistry` stack onto the payload API
under `net.minecraftforge`, and `:forge:1.21.1` builds and publishes like any other node.

The plugin choice is not free. ForgeGradle 6 is Gradle 8 only, and Loom 1.18.1 needs Gradle 9, so
the two cannot share an invocation. ModDevGradle's `legacyforge` cannot build 1.21.1 either: it asks
for `net.minecraftforge:forge:<v>:universal-srg`, a classifier that only exists for the pre-1.20.2
SRG layout. ForgeGradle 7 is the rewrite that runs on Gradle 9.3+.

ForgeGradle 7 is **stateless** and does not add repositories once another plugin has declared some,
so `forge/build.gradle.kts` registers the mavenizer repository and Mojang's libraries repository by
hand. Without both, the Forge dependency resolves to an empty module and every `net.minecraft.*`
import fails — which looks like a broken source tree and is not one.

Two smaller Forge-specific facts: mixin configs come from the `MixinConfigs` **jar manifest
attribute** (Forge does not understand the `[[mixins]]` blocks in `mods.toml` that NeoForge uses),
and since 1.20.2 there is no reobfuscation and no Searge refmap because Forge runs on official Mojang
names.

Do not "restore" the old `SimpleChannel` code to fix a build that references it.

### Platform abstraction rule

If you need a loader-specific API in `common/`, **add a method to `IPlatformHelper` and implement it
in both `FabricPlatformHelper` and `NeoForgePlatformHelper`**. Do not branch on loader inside
`common/`.

## Build

The launcher needs **JDK 25**. The Java 17 / 21 toolchains the other nodes need are downloaded by
the foojay resolver from `versions/<mc>/gradle.properties`. Do not hardcode
`org.gradle.java.home` in `gradle.properties`; it is machine-specific and breaks CI. Set
`JAVA_HOME` instead. The Gradle wrapper is **9.7.0** — Loom 1.18.1 publishes
`org.gradle.plugin.api-version = 9.7.0`, and an older wrapper fails with a variant-matching error
that never mentions the Gradle version.

**There is no bare `./gradlew build` any more** — `build` only exists per node, and the root project
has none.

```bash
JAVA_HOME=/usr/lib/jvm/temurin-25-jdk-amd64 ./gradlew chiseledBuild   # whole matrix
JAVA_HOME=/usr/lib/jvm/temurin-25-jdk-amd64 ./gradlew :fabric:26.2:build
JAVA_HOME=/usr/lib/jvm/temurin-25-jdk-amd64 ./gradlew :neoforge:26.2:build
JAVA_HOME=/usr/lib/jvm/temurin-25-jdk-amd64 ./gradlew :forge:1.21.1:build
./gradlew matrix                    # list the nodes
./gradlew :fabric:26.2:runClient    # dev client for one node
```

Output JARs live under the node, not the module:

| Loader   | Path |
| :------- | :--- |
| Fabric   | `fabric/versions/<mc>/build/libs/dayz-inventory-fabric-<mc>-<version>.jar` |
| NeoForge | `neoforge/versions/<mc>/build/libs/dayz-inventory-neoforge-<mc>-<version>.jar` |
| Forge    | `forge/versions/1.21.1/build/libs/dayz-inventory-forge-1.21.1-<version>.jar` |

`archivesName` is `dayz-inventory-<loader>-${minecraft_version}` — the Minecraft version is part of
the filename on purpose, so bumping the target version renames the artifact automatically.

### Every loader compiles `common`'s processed sources

`dayz-loader` adds the `:common:<mc>` node's **generated** source tree to each loader's compile task,
so common's classes and resources end up in the loader jar. That keeps a single copy of every class —
including the shared mixin classes — on the mod classpath.

Because of that, `common` must **not** be added as a project dependency. Adding
`implementation project(':common')` puts a second copy of every class and mixin config on the
classpath, and below 26.1 it also brings Loom's intermediary refmap, which NeoForge and Forge cannot
use.

It has to be the *generated* tree and not the raw `common/src`: the raw tree still contains every
version's conditional branches, and a refmap can only be produced from annotated sources.

The NeoForge module uses **`net.neoforged.moddev`** and needs **no refmap and no MixinGradle**:
NeoForge has run on official Mojang names since 1.20.2. The Fabric module uses
**`dev.kikugie.loom-back-compat`**, which picks `fabric-loom-remap` below 26.1 and `fabric-loom`
from 26.1 on — and only the remap flavour produces a refmap, which is why `common/build.gradle.kts`
sets `useLegacyMixinAp` for the older nodes.

## Critical Gotchas

These have all caused real build/runtime failures in this repo. Read before changing build config or
the client screen.

1. **`gradlew` must stay executable.** It is committed with mode `100755`. If it ever shows up as
   `100644`, fix it with `git update-index --chmod=+x gradlew`. CI also runs `chmod +x ./gradlew`.

2. **Minecraft 26.x is not obfuscated — there are no mappings.** Mojang stopped publishing mappings
   from 26.1 onward, and this ripples through the whole build:
   - There is **no `mappings` line** in `common/build.gradle` or `fabric/build.gradle`. Asking for
     `loom.officialMojangMappings()` fails the build with `Failed to find official mojang mappings
     for 26.2`, because the version manifest no longer publishes a `client_mappings` entry.
   - There are **no refmaps**, no remap step, and **no `remapJar` task** at all. `publishMods` reads
     `jar.archiveFile`, not `remapJar.archiveFile`.
   - Loom's plugin id is now **`net.fabricmc.fabric-loom`** (it was `fabric-loom`).
   - The `mod*` configurations are gone: use plain **`implementation` / `compileOnly`**, not
     `modImplementation` / `modCompileOnly`.
   - `common/build.gradle.kts` and `fabric/build.gradle.kts` only configure `loom { mixin { ... } }`
     for nodes **below 26** — those are the ones that still need a refmap. No module declares a
     `MixinConfigs` manifest attribute.
   - Mixins resolve by **official Mojang names natively on both loaders**. Above 26.1 there is no
     refmap to name; if you find yourself debugging refmap contents on 26.2, the cause is something
     else. Below 26.1 the refmap is real and is named `dayz-inventory.refmap.json`.

   Class and method names in code are still **Mojang names** (`Minecraft`, `LocalPlayer`,
   `AbstractContainerScreen`, `KeyMapping`, ...), not Yarn names (`MinecraftClient`,
   `ClientPlayerEntity`, ...).

3. **A mixin whose target has been renamed does not degrade — it fails.** These client mixin
   configs are declared `"required": true`, so a bad target is a **crash at launch**, not a silent
   no-op. This is exactly what happened in the 26.2 port:

   - The class is now **`ScreenRedirectMixin`**
     (`common/src/main/java/com/suoim/dayzinventory/client/mixin/ScreenRedirectMixin.java`). It was
     briefly called `GuiMixin`, but it no longer mixes into `Gui` below 26.2, so a target-specific
     name was misleading. The mixin config entry is a stable name, so the class can be renamed
     without touching JSON.
   - On 26.2 it mixes into **`net.minecraft.client.gui.Gui`** and hooks **`setScreen`**; below that
     it still mixes into **`Minecraft`** via `//? if >=26.2`.
   - Reason for the move: **`Minecraft#setScreen` was deleted in 26.2.** The redirect that swaps the
     vanilla inventory for the DayZ screen had hooked it.

   Targeting `Gui#setScreen` is deliberate: that is where **every** screen switch funnels.
   `Minecraft#handleKeybinds` opens the inventory with `this.gui.setScreen(new InventoryScreen(...))`
   directly, and `Minecraft#setScreenAndShow` delegates to the same method. A mixin on
   `setScreenAndShow` would never be reached from the E-key path — it is `gui.setScreen(...)` *plus*
   a forced `renderFrame(false)`, which the E-key path does not go through.

   When a mixin target moves, grep for the call sites before choosing a new hook, and prefer the
   method that *everything* routes through.

4. **The GUI is a render-state pipeline, not immediate-mode drawing.**
   - **`GuiGraphics` no longer exists**; it is replaced by
     **`net.minecraft.client.gui.GuiGraphicsExtractor`**.
   - **Every `render*` method became `extract*`.** The GUI *records render state* which the game
     replays later — it does not draw immediately. On `Screen` / `AbstractContainerScreen`:
     `render` → `extractRenderState`, `renderBackground` → `extractBackground`, `renderLabels` →
     `extractLabels`, `renderTooltip` → `extractTooltip`, `renderContents` → `extractContents`,
     `renderSlots` → `extractSlots`, `renderSlot` → `extractSlot`, `renderCarriedItem` →
     `extractCarriedItem`. The `Screen` entry point is `extractRenderStateWithTooltipAndSubtitles`,
     which calls `extractBackground` and then `extractRenderState`.
   - **`AbstractContainerScreen#renderBg` was removed outright.** There is no `renderBg` and no
     `extractBg`. The DayZ panel drawing is now a **private helper `drawDayZPanels(GuiGraphicsExtractor,
     float, int, int)`** called from the screen's own `extractRenderState`. It is no longer an
     override, so do not try to re-add one.
   - `GuiGraphics#drawString` → `GuiGraphicsExtractor#text` (same argument order:
     `(Font, String, int x, int y, int color, boolean dropShadow)`).
   - `GuiGraphics#renderFakeItem` → `fakeItem`; `renderItemDecorations` → `itemDecorations`.
   - `blitSprite` now takes a `RenderPipeline` as its first argument.
   - `InventoryScreen.renderEntityInInventoryFollowsMouse` →
     `extractEntityInInventoryFollowsMouse` (same parameter list).
   - **`Gui` no longer renders the HUD.** `Gui` has `extractRenderState(DeltaTracker, boolean,
     boolean)`; the HUD moved to a new `net.minecraft.client.gui.Hud` class, and render state flows
     through `net.minecraft.client.renderer.state.gui.GuiRenderState`.

5. **`AbstractContainerScreen`'s `imageWidth` / `imageHeight` are `final`.** They can no longer be
   assigned in the subclass body. Pass the screen size through the five-argument constructor
   `AbstractContainerScreen(T, Inventory, Component, int, int)` instead.

6. **Other 26.2 renames that had to be handled.**
   - `net.minecraft.world.inventory.ClickType` → **`ContainerInput`** (the constants, e.g. `PICKUP`,
     are unchanged).
   - `Recipe#assemble` **no longer takes a `RegistryAccess`** — it takes only the `CraftingInput`.
   - Fabric API was restructured for 26.2, following Mojang's screen-handler → menu rename:
     `fabric-screen-handler-api-v1` → **`fabric-menu-api-v1`**, package
     `net.fabricmc.fabric.api.screenhandler.v1` → **`net.fabricmc.fabric.api.menu.v1`**,
     `ExtendedScreenHandlerFactory` → **`ExtendedMenuProvider`**, `ExtendedScreenHandlerType` →
     **`ExtendedMenuType`**, and `PayloadTypeRegistry#playC2S()` → **`serverboundPlay()`** (the
     registry is now generic over the buffer type; `serverboundPlay()` returns the
     `RegistryFriendlyByteBuf`-typed registry).
   - Mouse/key input already used event objects (`MouseButtonEvent`, `KeyEvent`) as of 1.21.11;
     26.2 does not change that.

7. **Do not access `Platform.HELPER` before loader init.** It is `null` until the loader entrypoint
   runs. Anything reachable before that (e.g. a static initializer) will NPE. Use the null-safe
   `Platform.isModLoaded(...)` / `Platform.isReady()` helpers for probes.

8. **`KeyMapping` has no public getter for its bound key.** The code reads it reflectively in
   `DayZInventoryScreen#getBoundKey`. If you touch that method, keep it defensive — it must return
   `null` rather than throw when the field layout differs.

9. **The Java level is per version, not global.** `deps.java` in `versions/<mc>/gradle.properties`
   drives the toolchain and `options.release` (17 on 1.20.1, 21 on 1.21.x, **25** on 26.x), and
   `deps.mixin-compat` is expanded into the mixin configs as `compatibilityLevel`. A hardcoded
   `JAVA_25` on a Java 21 node fails Mixin's own validation.

10. **Every manifest placeholder must stay a placeholder.** `fabric.mod.json`, both `mods.toml`s,
    `pack.mcmeta` and the mixin configs are expanded by `processResources` from the per-version
    properties. A hardcoded `version` drifted once and shipped a 1.4.1 jar that reported itself as
    1.4.0 in crash reports, and a hardcoded Minecraft range would ship a 1.21.11 jar claiming to be
    for 26.2. Expanding an unknown token fails the build, which is the intended behaviour.

11. **Fabric dev runs apply mixins normally on this branch.** `:fabric:runClient` starts the game
    with mixins applied and shows the real DayZ screen, so it *is* valid for judging GUI work.

    This was **not** true on the 1.20.1 / 1.21.x branches: there, `loom.officialMojangMappings()`
    wrote a refmap in the production namespace while dev ran on official names, so the mixin was
    remapped to a class that did not exist and was skipped. With no mappings and no refmap on 26.2,
    that failure mode is gone. If you are debugging a skipped mixin here, the cause is a wrong
    target, not a refmap.

## Optional Dependencies

JEI, REI, EMI, Trinkets and Curios are **optional**. They are probed at runtime via
`Platform.HELPER.isModLoaded(...)` and their UI buttons are only drawn when present.

**Rule:** the mod must launch, open its inventory screen, and function fully with **none** of these
installed. Never add a hard reference to an optional mod's classes. Any new integration must be
guarded by an `isModLoaded` check and must not run at class-load time.

## Features That Must Be Preserved

When refactoring, do not regress these:

- **Vicinity grid** — proximity scan (3-block radius) of ground items and container blocks,
  scrollable unified column, clickable container selectors with inline drawer grids.
- **Proximity Scanner** — periodic rescan every 10 ticks in `containerTick()`.
- **2x2 Crafting** — vanilla-shaped crafting grid + result slot inside the custom screen, with the
  result recomputed server-side in `DayZInventoryScreenHandler#updateCraftingResult`.
- **Hands slot 2.0x render** — the held item is drawn at 2.0x scale in the middle column, bound to
  the currently selected hotbar slot.
- **Drag-to-equip** — dragging armor onto the Survivor panel equips/swaps it.
- **Recipe viewer toggle** — simulates an `O` keypress (GLFW 79) to toggle the overlay.

## GitHub Actions

`.github/workflows/build.yml` runs `./gradlew chiseledBuild` on pushes to `main` and `refactor/**`,
on pull requests, tags and manual dispatch. It uses **JDK 25**; the foojay resolver supplies the
other toolchains. On a `v*` tag a second job attaches every jar to the GitHub Release and a third
publishes the whole matrix to both platforms.

Artifacts are read from `*/versions/*/build/libs/` — **not** the module-level `build/libs/` or the
root one, neither of which contains mod jars any more.

## Publishing

Publishing uses `me.modmuss50.mod-publish-plugin` (Minotaur), configured **once** in
`build-logic/src/main/kotlin/dayz-loader.gradle.kts` and applied to every loader node.

- Tokens are read from the environment. Never hardcode one in a build file or commit one:
  `MODRINTH_TOKEN` (falls back to `MODRINTH_PAT`) and `CURSEFORGE_API_KEY` (falls back to
  `CURSEFORGE_TOKEN`).
- Modrinth project id: `8asZxzdc`. CurseForge project id: `1596267`.
- `./gradlew publishAll -Ppublish.dry_run=false` publishes the matrix; `publish.dry_run` defaults to
  `true`, so a stray `publishMods` only logs. One platform:
  `:fabric:26.2:publishModrinth` / `:fabric:26.2:publishCurseforge`.
- Game version and loader tags come from the node, never from a literal, so a jar cannot be uploaded
  under the wrong Minecraft version.
- **CurseForge accepts the file and returns no URL** — every upload goes through human review, so a
  green `publishCurseforge` means *submitted*, not *live*. Do not treat the missing response as a
  failure.
- Modrinth placeholder slugs must be real Modrinth project slugs or the publish fails with a 404 —
  e.g. REI is `rei`, not `roughlyenoughitems`. Verify before adding one.

### Store descriptions

`docs/store-descriptions/curseforge.md` is the **copy-paste source** for the CurseForge project
page, because that page cannot be updated from the build. The `CURSEFORGE_TOKEN` used for uploads
is a legacy upload-only token; the Eternal API that edits project metadata rejects it (403).

Consequences when features change:

- The Modrinth description **is** pushed via the API, so it must be updated deliberately.
- The CurseForge description and the CurseForge **license field** are manual dashboard edits.
  Keep the CurseForge description in sync with `README.md` by hand.

## Versioning

`mod.version` in `gradle.properties` is the single source of truth. It is expanded by
`processResources` into `fabric.mod.json`, `neoforge.mods.toml`, `mods.toml` and `pack.mcmeta`, and
it is part of every artifact filename. Bump it, then update `CHANGELOG.md`.
