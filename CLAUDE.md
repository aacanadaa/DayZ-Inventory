# CLAUDE.md

Guidance for Claude Code when working in this repository.

## Project Overview

**DayZ Inventory** is a Minecraft **1.20.1** mod that replaces the vanilla inventory screen with a
DayZ-style UI: a Vicinity grid (nearby ground items + containers), a dynamic Hands attachment slot,
an integrated 2x2 crafting grid, and drag-to-equip onto the Survivor panel.

It ships for **two loaders from one codebase**: Fabric and Forge. Both artifacts are built from the
same Gradle build.

- **Group / package root**: `com.suoim.dayzinventory`
- **Mod ID**: `dayz_inventory`
- **License**: Apache License 2.0 (see `LICENSE`)
- **Modrinth slug**: `dayz-inventory` (project id `8asZxzdc`)

## Repository Layout

This is a multi-loader Gradle project using a shared `common` module.

```
common/    Loader-agnostic code: screen, screen handler, packets, mixins, platform interface
fabric/    Fabric entrypoints + FabricPlatformHelper
forge/     Forge entrypoints + ForgePlatformHelper + SimpleChannel networking
```

- `common/` uses **fabric-loom** for its Minecraft dependency, but its code must stay
  loader-neutral. It must never import `net.fabricmc.*` or `net.minecraftforge.*` outside of the
  platform abstraction.
- Loader-specific behaviour is reached through `IPlatformHelper`
  (`common/src/main/java/com/suoim/dayzinventory/platform/`). The active implementation is assigned
  to the static field `Platform.HELPER` during each loader's initialization.

### Platform abstraction rule

If you need a loader-specific API in `common/`, **add a method to `IPlatformHelper` and implement it
in both `FabricPlatformHelper` and `ForgePlatformHelper`**. Do not branch on loader inside `common/`.

## Build

Requires **JDK 17**. Do not hardcode `org.gradle.java.home` in `gradle.properties` — it is
machine-specific and breaks CI. Set `JAVA_HOME` instead.

```bash
./gradlew build          # builds both loaders
./gradlew :fabric:build
./gradlew :forge:build
./gradlew :fabric:runClient
./gradlew :forge:runClient
```

Output JARs:

| Loader | Path |
| :--- | :--- |
| Fabric | `fabric/build/libs/dayz-inventory-fabric-<version>.jar` |
| Forge  | `forge/build/libs/dayz-inventory-forge-<version>.jar` |

## Critical Gotchas

These have all caused real build/runtime failures in this repo. Read before changing build config.

1. **`gradlew` must stay executable.** It is committed with mode `100755`. If it ever shows up as
   `100644`, fix it with `git update-index --chmod=+x gradlew`. CI also runs `chmod +x ./gradlew`.

2. **Mappings are Mojang official, not Yarn.** All modules use
   `loom.officialMojangMappings()`. Class and method names in code are **Mojang names**
   (`Minecraft`, `LocalPlayer`, `AbstractContainerScreen`, `KeyMapping`, ...), not Yarn names
   (`MinecraftClient`, `ClientPlayerEntity`, ...). Do not mix the two conventions.

3. **Forge compiles `common` — it must NOT also be on the mod classpath.**
   `forge/build.gradle` adds `common`'s `java` and `resources` source directories to the Forge
   `main` source set, so common's classes and resources are built into Forge's own output. That is
   what lets the Mixin annotation processor see the common mixins and emit
   `dayz-inventory.refmap.json` with **Searge** mappings (Forge runs on SRG at runtime).

   Because of that, `common` must not *also* be added as a mod source or a project dependency.
   Adding it puts a second copy of the mixin configs and Loom's **intermediary** refmap on the
   classpath, and Mixin resolves the wrong one — every mixin then fails with
   `@Inject ... specifies a target class 'net/minecraft/class_XXXX', which is not supported`.
   So: no `implementation project(':common')`, and no `source project(':common').sourceSets.main`
   in the `runs { mods { ... } }` blocks.

4. **Forge's Searge refmap must be copied into its resources for dev runs.**
   MixinGradle writes the refmap to `build/tmp` and only injects it into the packaged jar. Dev
   runs load the mod from build output directories, so `processResources` copies it across
   explicitly. Without that, `:forge:runClient` / `:forge:runServer` fail with the error above.

4. **Do not access `Platform.HELPER` before loader init.** It is `null` until the loader entrypoint
   runs. Anything reachable before that (e.g. a static initializer) will NPE.

5. **`KeyMapping` has no public getter for its bound key.** The code reads it reflectively in
   `DayZInventoryScreen#getBoundKey`. If you touch that method, keep it defensive — it must return
   `null` rather than throw when the field layout differs.

6. **Java 17 bytecode.** `options.release = 17`. Gradle 8.8 supports JDK 17–22; do not bump CI to a
   newer JDK without also bumping Gradle and re-testing both loaders.

7. **Fabric dev runs (`:fabric:runClient` / `:fabric:runServer`) do not apply mixins.**
   This is a known limitation of the current mapping choice, not a regression to "fix" casually.

   The project uses `loom.officialMojangMappings()`. Loom writes the mixin refmap
   (`dayz-inventory.refmap.json`) into `common/build/classes/java/main/`, which is on the dev
   classpath. In development the game runs on **official** class names, but Fabric's dev-time
   mixin remapper expects to translate from the production namespace. The refmap gets applied
   anyway, producing intermediary names, and Mixin then reports
   `@Mixin target net.minecraft.class_XXXX was not found` and skips the mixin.

   Consequences:
   - `:fabric:runClient` starts, but the vanilla inventory screen is **not** redirected, because
     `MinecraftClientMixin` never applied. Do not use it to judge whether the GUI works.
   - The **packaged Fabric jar is fine** — production runs on intermediary, so the refmap is
     correct there. Verified: all four server mixins apply and the server starts.

   To exercise the real GUI in a dev loop, use **`:forge:runClient`**, where the Searge refmap
   resolves correctly. To test Fabric behaviour, install the built jar into a real Fabric
   installation.

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

`.github/workflows/build.yml` builds both loaders on pushes to `main`, pull requests, tags, and
manual dispatch. On a `v*` tag it also attaches both JARs to a GitHub Release.

Artifacts are read from `fabric/build/libs/` and `forge/build/libs/` — **not** the root
`build/libs/`, which contains no mod JARs.

## Publishing

Publishing uses `me.modmuss50.mod-publish-plugin` (Minotaur) and is configured in
`fabric/build.gradle` and `forge/build.gradle`.

- Tokens are read from the environment (`MODRINTH_TOKEN`, `CURSEFORGE_TOKEN`). Never hardcode a
  token in a build file or commit one.
- Modrinth project id: `8asZxzdc`.

## Versioning

`mod_version` in `gradle.properties` is the single source of truth. It is expanded into
`fabric.mod.json` by `processResources`. Bump it, then update `CHANGELOG.md`.
