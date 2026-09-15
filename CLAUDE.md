# CLAUDE.md

Guidance for Claude Code when working in this repository.

## Project Overview

**DayZ Inventory** is a Minecraft **26.2** mod that replaces the vanilla inventory screen with a
DayZ-style UI: a Vicinity grid (nearby ground items + containers), a dynamic Hands attachment slot,
an integrated 2x2 crafting grid, and drag-to-equip onto the Survivor panel.

It ships for **two loaders from one codebase**: **Fabric** and **NeoForge**. Both artifacts are
built from the same Gradle build.

- **Mod version**: `1.5.0`
- **Group / package root**: `com.suoim.dayzinventory`
- **Mod ID**: `dayz_inventory`
- **License**: Apache License 2.0 (see `LICENSE`)
- **Modrinth slug**: `dayz-inventory` (project id `8asZxzdc`)

This branch targets **26.2**, which is the first line built on an **unobfuscated** Minecraft and the
first with the rewritten GUI render model. Both changes are structural, not cosmetic — read
*Critical Gotchas* before touching build config or anything under `client/`.

## Repository Layout

This is a multi-loader Gradle project using a shared `common` module.

```
common/     Loader-agnostic code: screen, screen handler, packets, mixins, platform interface
fabric/     Fabric entrypoints + FabricPlatformHelper
neoforge/   NeoForge entrypoints + NeoForgePlatformHelper + ModNetwork
forge/      Present on disk but NOT built on this branch (see below)
```

- `common/` uses **Loom** for its Minecraft dependency, but its code must stay loader-neutral. It
  must never import `net.fabricmc.*` or NeoForge classes outside of the platform abstraction.
- Loader-specific behaviour is reached through `IPlatformHelper`
  (`common/src/main/java/com/suoim/dayzinventory/platform/`). The active implementation is assigned
  to the static field `Platform.HELPER` during each loader's initialization.

### Why `forge/` is not built

`settings.gradle` has `include 'forge'` **commented out**; the built modules are `common`, `fabric`
and `neoforge`. Forge does not carry forward past 1.20.x — the ecosystem moved to NeoForge, and the
plugin, the package names and the networking stack all differ, which is why NeoForge is a separate
module rather than a port of `forge/`. The `forge/` directory is left in place for reference; the
**`main` branch (1.20.1) still ships Forge**.

Do not "restore" the Forge module here to fix a build that references it — it is not expected to
compile on 26.2.

### Platform abstraction rule

If you need a loader-specific API in `common/`, **add a method to `IPlatformHelper` and implement it
in both `FabricPlatformHelper` and `NeoForgePlatformHelper`**. Do not branch on loader inside
`common/`.

## Build

Requires **JDK 25** — Minecraft 26.2 ships as `java-runtime-epsilon`, major version 25. Do not
hardcode `org.gradle.java.home` in `gradle.properties`; it is machine-specific and breaks CI. Set
`JAVA_HOME` instead. The Gradle wrapper is **9.5.1**, which supports JDK 25.

```bash
JAVA_HOME=/usr/lib/jvm/temurin-25-jdk-amd64 ./gradlew build

./gradlew :fabric:build
./gradlew :neoforge:build
./gradlew :fabric:runClient
./gradlew :neoforge:runClient
```

Output JARs:

| Loader   | Path |
| :------- | :--- |
| Fabric   | `fabric/build/libs/dayz-inventory-fabric-26.2-<version>.jar` |
| NeoForge | `neoforge/build/libs/dayz-inventory-neoforge-26.2-<version>.jar` |

`archivesName` is `dayz-inventory-<loader>-${minecraft_version}` — the Minecraft version is part of
the filename on purpose, so bumping the target version renames the artifact automatically.

### NeoForge compiles `common` directly

`neoforge/build.gradle` adds `common`'s `java` and `resources` source directories to the NeoForge
`main` source set, so common's classes and resources are compiled into NeoForge's own output. That
keeps a single copy of every class — including the shared mixin classes — on the mod classpath.

Because of that, `common` must **not** also be added as a project dependency. Adding
`implementation project(':common')` puts a second copy of every class and mixin config on the
classpath.

The module uses the **`net.neoforged.moddev`** plugin, and needs **no refmap and no MixinGradle**:
NeoForge has run on official Mojang names since 1.20.2, so mixin selectors resolve as written.

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
   - The `loom { mixin { defaultRefmapName = ... } }` block in `common/build.gradle` has been
     **removed**, and no module declares a `MixinConfigs` manifest attribute. (`forge/build.gradle`
     still has one, but that module is not built on this branch — see *Why `forge/` is not built*.)
   - Mixins resolve by **official Mojang names natively on both loaders**. There is no refmap to
     name and no `named:intermediary` refmap to verify — if you find yourself debugging refmap
     contents, you are on the wrong branch.

   Class and method names in code are still **Mojang names** (`Minecraft`, `LocalPlayer`,
   `AbstractContainerScreen`, `KeyMapping`, ...), not Yarn names (`MinecraftClient`,
   `ClientPlayerEntity`, ...).

3. **A mixin whose target has been renamed does not degrade — it fails.** These client mixin
   configs are declared `"required": true`, so a bad target is a **crash at launch**, not a silent
   no-op. This is exactly what happened in the 26.2 port:

   - The class formerly called `MinecraftClientMixin` is now **`GuiMixin`**
     (`common/src/main/java/com/suoim/dayzinventory/client/mixin/GuiMixin.java`).
   - It no longer mixes into `Minecraft`; it mixes into **`net.minecraft.client.gui.Gui`** and hooks
     **`setScreen`**.
   - Reason: **`Minecraft#setScreen` was deleted in 26.2.** The redirect that swaps the vanilla
     inventory for the DayZ screen had hooked it.

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

9. **Java 25 bytecode.** `options.release = 25`, `sourceCompatibility` / `targetCompatibility` are
   `VERSION_25`, and the mixin configs declare `"compatibilityLevel": "JAVA_25"` (formerly
   `JAVA_17`). Gradle 9.5.1 supports JDK 25; do not move the toolchain without re-testing both
   loaders.

10. **`neoforge.mods.toml`'s `version` must stay `${version}`.**
    `neoforge/src/main/resources/META-INF/neoforge.mods.toml` carries `version="${version}"`,
    expanded by `processResources` in `neoforge/build.gradle`. A hardcoded value drifted once and
    shipped a 1.4.1 jar that reported itself as 1.4.0 in crash reports.

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

`.github/workflows/build.yml` builds both loaders on pushes to `[main, "1.*", "2*"]`, pull requests,
tags, and manual dispatch. It uses **JDK 25**. On a `v*` tag it also attaches both JARs to a GitHub
Release.

The `2*` filter is what covers the calendar-versioned branches (`26.2`, `26.3`, ...); `1.*` covers
the pre-26 per-version branches. A plain `1.x` would match only a branch literally named that.

Artifacts are read from `fabric/build/libs/` and `neoforge/build/libs/` — **not** the root
`build/libs/`, which contains no mod JARs.

## Publishing

Publishing uses `me.modmuss50.mod-publish-plugin` (Minotaur) and is configured in
`fabric/build.gradle` and `neoforge/build.gradle`.

- Tokens are read from the environment (`MODRINTH_TOKEN`, `CURSEFORGE_TOKEN`). Never hardcode a
  token in a build file or commit one.
- Modrinth project id: `8asZxzdc`. CurseForge project id: `1596267`.
- `:fabric:publishMods` / `:neoforge:publishMods` publish to **both** platforms. Use the
  platform-specific tasks (`publishModrinth`, `publishCurseforge`) when you only want one.
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

`mod_version` in `gradle.properties` is the single source of truth. It is expanded into
`fabric.mod.json` by `processResources` and into `neoforge.mods.toml` by the NeoForge module's own
`processResources`. Bump it, then update `CHANGELOG.md`.
