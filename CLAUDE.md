# CLAUDE.md

Guidance for Claude Code when working in this repository.

## Project Overview

**DayZ Inventory** is a Minecraft mod that replaces the vanilla inventory screen with a DayZ-style
UI: a Vicinity grid (nearby ground items + containers), a dynamic Hands attachment slot, an
integrated 2x2 crafting grid, and drag-to-equip onto the Survivor panel.

It ships for **three loaders from one codebase**: **Fabric**, **NeoForge** and **Forge**.

- **Mod version**: `1.8.0`
- **Group / package root**: `com.suoim.dayzinventory`
- **Mod ID**: `dayz_inventory`
- **License**: Apache License 2.0 (see `LICENSE`)
- **Modrinth slug**: `dayz-inventory` (project id `8asZxzdc`), CurseForge project `1596267`

### One source tree, twenty-three Minecraft versions

This is a **Stonecutter** project. There is no longer a branch per Minecraft version. The enabled
matrix is **23 Minecraft versions from 1.20.1 to 26.3**, producing **53 artifacts** — 23 Fabric,
18 NeoForge and 12 Forge — and the version differences are marked inline with `//? if` comments.

| Minecraft | Fabric | NeoForge | Forge | Java |
| :--- | :---: | :---: | :---: | :---: |
| 1.20.1 – 1.20.4 | ✅ | — | — | 17 |
| 1.20.5 | ✅ | — | — | 21 |
| 1.20.6 – 1.21.1 | ✅ | ✅ | ✅ | 21 |
| 1.21.2 | ✅ | ✅ | — | 21 |
| 1.21.3 – 1.21.11 | ✅ | ✅ | ✅ | 21 |
| 26.1 – 26.3 | ✅ | ✅ | — | 25 |

`./gradlew matrix` prints the authoritative node list; `versions/<mc>/gradle.properties` is the only
place a version's coordinates live. Do not go below **1.20.1**: the older lines predate the screen
handler rewrite this mod is built around.

Why the gaps exist is recorded in [docs/BUILDING.en.md](docs/BUILDING.en.md) §7 — read it before
trying to re-enable one. In short: NeoForge 1.20.2 used the old `SimpleChannel` stack, 1.20.3 had no
NeoForge release at all, and 1.20.4 predates `StreamCodec`, so the whole 1.20.1–1.20.5 stretch is
Fabric-only; Forge 1.20.1 needs SRG reobfuscation and a Searge refmap no Gradle-9-capable Forge
plugin provides; Forge skipped 1.21.2; and the 26.x line has no Forge build.

**Read [docs/BUILDING.en.md](docs/BUILDING.en.md) first.** It covers the project tree, the
convention plugins, the conditional-compilation conventions and the publishing setup. The rest of
this file is the accumulated "things that have actually broken here" list.

26.2 is the reference target for the modern line and the one that differs most: it is the first line
built on an **unobfuscated** Minecraft and the first with the rewritten GUI render model. Both
changes are structural, not cosmetic — read *Critical Gotchas* before touching build config or
anything under `client/`. 26.3 adds a second structural break on top (GLFW → SDL3).

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
forge/      Forge entrypoints - built for 1.20.6-1.21.11 (see docs/BUILDING.en.md section 7)
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
under `net.minecraftforge`, and every Forge node (**1.20.6 through 1.21.11**) builds and publishes
like any other node.

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

**1.21.6 moved Forge to EventBus 7**, which is a two-part break and neither part is solved by an
import alone:
- `net.minecraftforge.eventbus.api` split into `bus` and `listener`, so `@SubscribeEvent` is now
  `net.minecraftforge.eventbus.api.listener.SubscribeEvent`.
- `IEventBus` is **gone** — it does not exist in EventBus 7 at all. `FMLJavaModLoadingContext`
  replaced `getModEventBus()` with **`getModBusGroup()`, which returns a `BusGroup`**, and
  `DeferredRegister#register` takes that `BusGroup`.

So `DayZInventoryForge` switches on `//? if >=1.21.6` around the import block **and** around the
bus-fetch/register statement, because there is no common supertype to fall back on. Do not try to
"clean that up" into a single `var` — `BusGroup` and `IEventBus` are unrelated types.

Forge's mod constructor is only ever invoked with a `FMLJavaModLoadingContext` or no argument at
all; the bus is **not** injectable as a constructor parameter. `@Mod.EventBusSubscriber(bus = ...Bus.MOD)`
still works on both sides (its default did change from `FORGE` to `BOTH` in 1.21.6, so always name
the bus explicitly).

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
JAVA_HOME=/usr/lib/jvm/temurin-25-jdk-amd64 ./gradlew :neoforge:1.21.11:build
JAVA_HOME=/usr/lib/jvm/temurin-25-jdk-amd64 ./gradlew :forge:1.21.11:build
./gradlew matrix                    # list the nodes
./gradlew :fabric:26.2:runClient    # dev client for one node
```

`chiseledBuild` is 686 tasks deep and takes a long time; when driving it from an agent, run it as a
background job rather than in the foreground, and pass `--max-workers=4` so the per-version
toolchain and mapping caches do not thrash.

Output JARs live under the node, not the module:

| Loader   | Path |
| :------- | :--- |
| Fabric   | `fabric/versions/<mc>/build/libs/dayz-inventory-fabric-<mc>-<version>.jar` |
| NeoForge | `neoforge/versions/<mc>/build/libs/dayz-inventory-neoforge-<mc>-<version>.jar` |
| Forge    | `forge/versions/<mc>/build/libs/dayz-inventory-forge-<mc>-<version>.jar` (`<mc>` 1.20.6–1.21.11) |

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
     for nodes **below 26** — those are the ones that still need a refmap. Only Forge declares a
     `MixinConfigs` manifest attribute, because Forge needs it on *every* version; see the Forge
     section above. Loom and ModDevGradle find the configs through `fabric.mod.json` /
     `neoforge.mods.toml`.
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
   drives the toolchain and `options.release` (17 on 1.20.1–1.20.4, 21 on 1.20.5–1.21.11, **25** on
   26.x), and `deps.mixin-compat` is expanded into the mixin configs as `compatibilityLevel`. A
   hardcoded `JAVA_25` on a Java 21 node fails Mixin's own validation.

10. **Every manifest placeholder must stay a placeholder.** `fabric.mod.json`, both `mods.toml`s,
    `pack.mcmeta` and the mixin configs are expanded by `processResources` from the per-version
    properties. A hardcoded `version` drifted once and shipped a 1.4.1 jar that reported itself as
    1.4.0 in crash reports, and a hardcoded Minecraft range would ship a 1.21.11 jar claiming to be
    for 26.2. Expanding an unknown token fails the build, which is the intended behaviour.

11. **Dev runs and refmaps differ above and below 26.1.** `:fabric:runClient` starts the game with
    mixins applied, so it *is* valid for judging GUI work — but the mechanism is not the same on
    both sides of the obfuscation boundary.

    Above 26.1 there are no mappings and no refmap at all, so mixins resolve by official Mojang
    names directly and there is nothing to go stale. Below 26.1 `useLegacyMixinAp` produces a real
    refmap from the annotated sources, and `dayz-loader` compiles `common`'s **generated** sources
    precisely so that refmap can be built — which is also why `implementation project(':common')`
    breaks things rather than simplifying them.

    If a mixin is silently skipped, the cause is a wrong target far more often than a refmap. Check
    the target's real name in the version's jar before suspecting the mapping pipeline.

12. **The version boundaries are not guessable — they were read off the real jars.** Each `//? if`
    boundary in `dayz-common.gradle.kts` corresponds to a concrete API change, and the full table
    lives in [docs/BUILDING.en.md](docs/BUILDING.en.md) §6. The ones that most often surprise you:

    - **26.1** dropped obfuscation entirely, so the build has no mappings, no refmaps and no
      `remapJar`. It also renamed `GuiGraphics` → `GuiGraphicsExtractor`, every GUI `render*` →
      `extract*`, `ClickType` → `ContainerInput`, and Fabric's `screenhandler` API → `menu`.
    - **26.2** deleted `Minecraft#setScreen` (hence `ScreenRedirectMixin` targeting `Gui#setScreen`)
      and removed the `RegistryAccess` argument from `Recipe#assemble`.
    - **26.3** moved Minecraft from GLFW to SDL3, so `org.lwjgl.glfw` is **gone from the
      classpath** and raw modifier bits became SDL keymods. Use the event's
      `hasShiftDown()`-style helpers instead of testing modifier flags by hand.
    - **1.20.2** is the quiet one. `RecipeHolder` arrives here, so `RecipeManager#getRecipeFor`
      returns `Optional<RecipeHolder<CraftingRecipe>>` and `RecipeCraftingHolder#setRecipeUsed`
      takes a `RecipeHolder<?>` rather than a bare `Recipe<?>`. The recipe branch in
      `DayZInventoryScreenHandler` therefore switches at **1.20.2**, not at 1.20.5. `mouseScrolled`
      also gains a horizontal axis here, and `renderEntityInInventoryFollowsMouse` replaces
      `renderEntityInInventory`.
    - **1.20.5** split `Block#use` into `useItemOn` (held item) and **`useWithoutItem`** (empty
      hand), and the older `use` carries an extra `InteractionHand` parameter. `ChestBlockMixin`
      and `BarrelBlockMixin` switch on that boundary for **both** the target name and the method
      signature. Below 1.20.5 the `useWithoutItem` target does not exist and the mixin is declared
      `required`, so getting this wrong is a launch crash, not a no-op — check the target with
      `javap` on the real version jar rather than trusting the comment.
    - **1.21.6** swapped the GUI stack to `Matrix3x2f`, moved Forge to EventBus 7, and split
      **`renderContents` out of `render`** — taking the `hoveredSlot` assignment with it. The
      boundary that matters is where the *assignment* moves, not where the method first appears:
      `AbstractContainerScreenMixin` targets `render` below 1.21.6 and `renderContents` from 1.21.6
      to 26.0. Targeting `render` on 1.21.6–1.21.10 finds no injection point at all, and with
      `defaultRequire: 1` that is a launch crash on five versions. This was wrong in 1.8.0.
    - **1.21.11** renamed `ResourceLocation` → `Identifier`.
    - **1.21.9** is where `Level#isClientSide` and `ServerPlayer#getServer` stopped being public.

    Two mechanical rules come out of this. First, **Stonecutter conditions cannot nest** — write a
    flat `if / elif / else` chain instead of putting one inside another. Second, keep `//` comments
    *before* a `//? if` line rather than between it and the code it guards; a comment adjacent to a
    conditional boundary can be swallowed by the wrong branch.

13. **`fabric.mod.json` does not accept Maven version ranges.** This shipped a completely unloadable
    Fabric jar on 1.7.1, 1.7.2 *and* 1.8.0, so it is worth stating precisely.

    The version properties store one range per Minecraft version in Maven form, e.g.
    `meta.minecraft-range=[1.21.5,1.21.6)`. That form is correct for the **NeoForge and Forge**
    `mods.toml` files, which parse Maven ranges. It is **wrong for Fabric**: `fabric-loader`'s
    `VersionPredicateParser` has no bracket-range syntax, so it falls back to an *equality* test
    against the whole bracket string as an opaque literal. It can therefore match only itself, never
    a real Minecraft or Java version.

    The symptom is a self-contradictory error that reads like a bug in the loader:

    ```
    requires version [1.21.5,1.21.6) of 'Minecraft' (minecraft),
    but only the wrong version is present: 1.21.5!
    ```

    Fabric needs the **space-separated comparison form** instead — `>=1.21.5 <1.21.6`, and `>=21`
    for Java. (`java` is matched against the bare major taken from `java.specification.version`, so
    `>=21` covers both `21` and `21.0.7`.)

    This is why `expandProps` exposes **two** pairs: `minecraft_range` / `java_range` stay Maven for
    the loader TOMLs, while `minecraft_range_fabric` / `java_range_fabric` are converted by
    `mavenRangeToFabricPredicate`. Use the `_fabric` pair in `fabric.mod.json` and never the bare
    one. `fabric_loader_range` and `fabric_api_range` are already in the right form.

    Do not "simplify" this back to a single placeholder. If you add a dependency to
    `fabric.mod.json`, give it the comparison form. Verify any change by running the loader's own
    parser over the built jar — reasoning from the version number is what caused this.

    Note the one non-obvious row: `1.21.1`'s declared range is `[1.21,1.21.2)`, so its Fabric
    predicate is `>=1.21 <1.21.2` — deliberately admitting `1.21` as well, matching what the Forge
    and NeoForge jars already advertise.

14. **A loader's metadata is not proof its classes are in the jar.** 1.8.0 shipped *every* Fabric
    jar with `fabric.mod.json` naming a client entrypoint class that was never compiled in: it sat
    in `fabric/src/client/java`, a source root added to the build after Stonecutter had already
    claimed the main source set, so nothing ever preprocessed or compiled it. Fabric refused to
    start with an "entrypoint not found" error naming a class that was plainly present in the
    source tree.

    The rule is **keep every loader source under `src/main/java`**. `dayz-loader` compiles the
    generated copy of `src/main/java` and nothing else, so a `src/client/java` root is invisible to
    that pipeline wherever it is declared. Client-only safety comes from the `"environment":
    "client"` marker on the Fabric entrypoint, not from the source root.

    `verifyJar` (wired into `check`, so `chiseledBuild` runs it) opens the built jar and resolves
    every class its own metadata names — Fabric entrypoints from `fabric.mod.json`, every class in
    every mixin config, and the hardcoded NeoForge/Forge `@Mod` classes. A missing class fails the
    build instead of reaching a user.

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
- **Recipe viewer toggle** — looks up the installed viewer's *own* toggle keybind (JEI's
  `toggleOverlay`-style mapping, falling back to `O` / GLFW 79) and simulates a tap on it. JEI, REI
  and EMI each bind this differently and users rebind it, so never hardcode the key. `getBoundKey`
  reads `KeyMapping` reflectively and must prefer the field named `key` over `defaultKey`, and
  `simulateKeyTap(InputConstants.Key)` must keep the key **type** rather than reducing it to an int.
- **Curios on Fabric** — Curios is NeoForge/Forge-centric, so on Fabric the mod falls back to
  opening the vanilla inventory instead of pretending the Curios UI exists.

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

`docs/store-descriptions/curseforge.md` and `docs/store-descriptions/modrinth.md` are the
**copy-paste sources** for the two project pages. **Both are manual dashboard edits.** Nothing in
the build touches either project body: `mod-publish-plugin` 2.1.1 only calls Modrinth's
*create version* endpoint, so the only text it pushes is the per-version `changelog` — the project
description is not sent at all. The `CURSEFORGE_TOKEN` used for uploads is a legacy upload-only
token, and the Eternal API that edits project metadata rejects it (403), so CurseForge could not be
automated this way even if the plugin supported it.

Consequences when features change:

- Update **both** store descriptions by hand, and keep them in sync with `README.md`. Keeping them
  deliberately short is the point — a store page is not the place for the full version matrix, which
  belongs in the README and `docs/BUILDING.md`. Both files open with a three-row loader table; the
  23-row matrix and per-version dependency pins stay in the README.
- The CurseForge **license field** is also a manual dashboard edit.
- The only store text the build does publish is each version's changelog, read from `CHANGELOG.md`.

## Versioning

`mod.version` in `gradle.properties` is the single source of truth. It is expanded by
`processResources` into `fabric.mod.json`, `neoforge.mods.toml`, `mods.toml` and `pack.mcmeta`, and
it is part of every artifact filename. Bump it, then update `CHANGELOG.md`.
