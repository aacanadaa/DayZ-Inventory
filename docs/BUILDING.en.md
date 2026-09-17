# Building DayZ Inventory

[中文](BUILDING.md) | **English**

This document explains how the unified multi-version / multi-loader build is put together, why it is
shaped the way it is, and how to add another Minecraft version.

---

## 1. Why one tree instead of one branch per version

The project used to carry a branch per Minecraft version (`main` for 1.20.1, `1.21.1`, `1.21.11`,
`26.2`). Every bug fix had to be merged into each of them, and each merge had to be built and tested
separately.

It is now one branch with one source tree. Version-specific code is marked inline with
`//? if <condition>` comments, and the build produces one artifact per *node* - a node being a
(module, Minecraft version) pair such as `:fabric:26.2`. The tree now covers **23 Minecraft
versions** (1.20.1 through 26.3) and produces **53 shippable jars**: 23 Fabric, 18 NeoForge and
12 Forge.

A note on the two ways to get here. [Stonecutter's own guide](https://stonecutter.kikugie.dev/)
describes both a **flat** layout (one `src/`, one node per version *and* loader, loader logic
selected by build constants) and a **branched** layout (a shared `common/` plus loader modules, each
with its own set of version nodes). This project uses the branched layout, because the two loaders
genuinely need different toolkits - Fabric Loom and NeoForge ModDevGradle - and because it keeps the
existing `common/` / `fabric/` / `neoforge/` separation that the codebase already had.

[Architectury](https://docs.architectury.dev/) was evaluated as the loader-abstraction layer and is
**not** used. Two reasons:

1. Architectury API would become a hard runtime dependency of every download. This mod already has a
   small hand-written platform abstraction (`IPlatformHelper` / `Platform`) that costs users nothing.
2. From 26.1 Minecraft ships **unobfuscated** and Fabric Loom split into `fabric-loom` (26.1+) and
   `fabric-loom-remap` (<26.1). Architectury Loom adds a second layer on top of that split with no
   established precedent on 26.x, which is the version that matters most.

---

## 2. Directory layout

```
settings.gradle.kts          Stonecutter tree + plugin management + the version matrix
stonecutter.gradle.kts       Controller script: active version, aggregate tasks
gradle.properties            Mod metadata, publishing ids, shared tool versions
build-logic/                 Convention plugins shared by every node
versions/<mc>/gradle.properties   Per-version dependency coordinates

common/                      Loader-agnostic code (screens, menus, packets, mixins)
  build.gradle.kts           Runs once per Minecraft version
  src/main/java              Shared sources, with `//? if` markers
fabric/                      Fabric entrypoints + FabricPlatformHelper
neoforge/                    NeoForge entrypoints + NeoForgePlatformHelper
forge/                       Forge entrypoints + ForgePlatformHelper (built for 1.20.6-1.21.11, see sections 6 and 7)
```

`stonecutter` names each node after its Minecraft version and places it under the branch directory,
so `:fabric:26.2` has its project directory at `fabric/versions/26.2/`. Those directories are build
output; only `versions/<mc>/gradle.properties` is source-controlled.

## 3. How the Gradle build fits together

### `settings.gradle.kts`

Declares the tree and the matrix:

```kotlin
val fabricVersions = listOf("1.20.1", "1.20.2", "1.20.3", "1.20.4", "1.20.5", "1.20.6", "1.21",
    "1.21.1", "1.21.2", "1.21.3", "1.21.4", "1.21.5", "1.21.6", "1.21.7", "1.21.8", "1.21.9",
    "1.21.10", "1.21.11", "26.1", "26.1.1", "26.1.2", "26.2", "26.3")
val neoforgeVersions = listOf("1.20.6", "1.21", "1.21.1", "1.21.2", "1.21.3", "1.21.4",
    "1.21.5", "1.21.6", "1.21.7", "1.21.8", "1.21.9", "1.21.10", "1.21.11",
    "26.1", "26.1.1", "26.1.2", "26.2", "26.3")
val forgeVersions = listOf("1.20.6", "1.21", "1.21.1", "1.21.3", "1.21.4", "1.21.5", "1.21.6",
    "1.21.7", "1.21.8", "1.21.9", "1.21.10", "1.21.11")
val commonVersions = (fabricVersions + neoforgeVersions + forgeVersions).distinct()

stonecutter {
    create(rootProject) {
        branch("common") { versions(*commonVersions.toTypedArray()) }
        branch("fabric") { versions(*fabricVersions.toTypedArray()) }
        branch("neoforge") { versions(*neoforgeVersions.toTypedArray()) }
        branch("forge") { versions(*forgeVersions.toTypedArray()) }
    }
}
```

Two details in that file are load-bearing and easy to break:

- **The root branch is deliberately empty.** It exists (Stonecutter always has one) but lists no
  versions. Giving it versions would create a project per Minecraft version with no build script and
  no artifacts, and the controller's publishing tasks cannot be ordered across projects that do not
  have them.
- **`pluginManagement.plugins {}` names both Loom flavours.** `dev.kikugie.loom-back-compat` applies
  one of `net.fabricmc.fabric-loom` / `net.fabricmc.fabric-loom-remap` *programmatically*, and a
  programmatic `pluginManager.apply(id)` is resolved against the project's buildscript repositories
  rather than against `pluginManagement.repositories`. Naming both ids there - and declaring them
  with `apply false` in `stonecutter.gradle.kts` - is what makes them resolvable from every node.

### `stonecutter.gradle.kts`

The controller. Holds the active version, registers `chiseledBuild` / `publishAll` / `matrix`, and
declares the loader plugins with `apply false` so the node projects inherit them on their buildscript
classpath.

### `build-logic/`

Two precompiled convention plugins:

| Plugin | Applied to | Responsibility |
| :--- | :--- | :--- |
| `dayz-common` | every node | version strings, Java toolchain, repositories, manifest expansion, version renames, licence in the jar, generated-source wiring |
| `dayz-loader` | fabric / neoforge / forge | sharing the `common` node's sources and configuring `publishMods` |

`build-logic/src/main/kotlin/Utils.kt` holds the `prop()` / `mc` / `branch` accessors. Note that
`stonecutter { }` is *not* available inside a precompiled script plugin - the extension has to be
reached through the `sc` accessor.

## 4. Version-processed sources

Stonecutter preprocesses the shared `src/` trees and writes the result to

```
<branch>/versions/<mc>/build/generated/stonecutter/<sourceSet>/{java,resources}
```

`dayz-common` points `compileJava` at that generated tree and **not** at the raw sources, because the
raw tree contains every version's code side by side and only comments out the inactive branches in
the copy it generates. Building from the generated tree also means the "active" version behaves
exactly like every other node, instead of depending on the working tree having been chiselled to it
first.

`dayz-loader` then adds the *common* node's generated tree to the loader's own compile task, so each
loader jar contains one copy of every shared class and one copy of every shared mixin config. That
matters beyond tidiness: below 26.1 Fabric and Forge need a **mixin refmap**, and a refmap can only be
produced by running the Mixin annotation processor over the annotated *sources*. A pre-compiled
`common.jar` would supply classes Mixin can never remap.

### Per-version properties

`versions/<mc>/gradle.properties` is the single place a Minecraft version's coordinates are
declared (loader versions, Java level, mixin compatibility level, pack format, publishing range).
Stonecutter only reads those files for versions registered on the *root* branch, and the root branch
is empty here, so `Utils.kt` reads them directly:

```kotlin
fun Project.propOrNull(key: String): String? =
    findProperty(key)?.toString()?.takeIf { it.isNotBlank() }
        ?: versionProperties()[key]?.takeIf { it.isNotBlank() }
```

That keeps one file per Minecraft version instead of one per (branch, version) pair, and makes
`-P` overrides and the root `gradle.properties` still win.

## 5. Conditional compilation

Two mechanisms are used, and choosing between them is a style rule rather than a hard requirement.

### `//? if` comments

For anything structural - a different method signature, a different hook target, a method that was
removed:

```java
//? if >=26.2 {
    private void drawDayZPanels(GuiGraphicsExtractor guiGraphics, float partialTick, int mouseX, int mouseY) {
//?} else {
    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
//?}
```

`elif` is available for three-way splits. **Do not put comments inside a conditional block** - the
processor rewrites comment markers and a surviving `//` can end up stripped, which turns a comment
into code. That failure is easy to miss and shows up as a syntax error in the generated file.

### `replacements`

For pure renames that would otherwise put a `//? if` around the same identifier dozens of times, the
convention plugin declares bulk rewrites:

```kotlin
sc.replacements.string(sc.current.parsed < "1.21.11") {
    replace("Identifier", "ResourceLocation")
    replace("isClientSide()", "isClientSide")
}
```

The sources are written against the **newest** names (`Identifier`, `isClientSide()`,
`GuiGraphicsExtractor`, `ContainerInput`, ...) and the renames are applied *backwards* for older
nodes. Keeping the base text on the current release means the newest target is never rewritten at
all, which is the target most likely to be worked on.

## 6. The version matrix

| Minecraft | Fabric | NeoForge | Forge | Java |
| :--- | :---: | :---: | :---: | :---: |
| **1.20.1** | ✅ | — | — | 17 |
| **1.20.2** | ✅ | — | — | 17 |
| **1.20.3** | ✅ | — | — | 17 |
| **1.20.4** | ✅ | — | — | 17 |
| **1.20.5** | ✅ | — | — | 21 |
| **1.20.6** | ✅ | ✅ | ✅ | 21 |
| **1.21** | ✅ | ✅ | ✅ | 21 |
| **1.21.1** | ✅ | ✅ | ✅ | 21 |
| **1.21.2** | ✅ | ✅ | — | 21 |
| **1.21.3** | ✅ | ✅ | ✅ | 21 |
| **1.21.4** | ✅ | ✅ | ✅ | 21 |
| **1.21.5** | ✅ | ✅ | ✅ | 21 |
| **1.21.6** | ✅ | ✅ | ✅ | 21 |
| **1.21.7** | ✅ | ✅ | ✅ | 21 |
| **1.21.8** | ✅ | ✅ | ✅ | 21 |
| **1.21.9** | ✅ | ✅ | ✅ | 21 |
| **1.21.10** | ✅ | ✅ | ✅ | 21 |
| **1.21.11** | ✅ | ✅ | ✅ | 21 |
| **26.1** | ✅ | ✅ | — | 25 |
| **26.1.1** | ✅ | ✅ | — | 25 |
| **26.1.2** | ✅ | ✅ | — | 25 |
| **26.2** | ✅ | ✅ | — | 25 |
| **26.3** | ✅ | ✅ | — | 25 |

Totals: **23 Minecraft versions, 53 shippable jars** (23 Fabric, 18 NeoForge, 12 Forge).

The Fabric Loom flavour split is still at `26.1`: `fabric-loom-remap` below it (1.21.11 and older) and
`fabric-loom` from 26.1 on for the unobfuscated line. `loom-back-compat` picks the flavour from
`sc.current.parsed < "26"` and aliases the `mod*` configurations the unobfuscated Loom removed, so one
build script covers both sides of that split.

### Per-version API boundaries

The table below records **which version each API changed in**. It is the most valuable thing in this
document: when porting a new version it says what to change instead of making you try one method at a
time. Every row was established by **inspecting that version's real jar and mappings, not inferred
from the version number**.

| Since | Change |
| :--- | :--- |
| 1.20.2 | `RecipeHolder` is introduced: `RecipeManager#getRecipeFor` now returns `Optional<RecipeHolder<CraftingRecipe>>` and `RecipeCraftingHolder#setRecipeUsed` takes a `RecipeHolder<?>` instead of a bare `Recipe<?>`. `mouseScrolled` gained a horizontal axis; `renderBackground` gained a 4-argument form; `renderEntityInInventoryFollowsMouse` replaced `renderEntityInInventory` |
| 1.20.5 | `CustomPacketPayload` gains its nested `Type` and the `StreamCodec` system; `RegistryFriendlyByteBuf` appears; `ItemStack#getTag` is removed in favour of `isSameItemSameComponents`; `Block#use` is split into `useItemOn` (held item) and `useWithoutItem` (empty hand), so the container-open mixin targets `useWithoutItem` from here on and the older `use` (with its extra `InteractionHand` parameter) below |
| 1.21 | `CraftingInput`; `ResourceLocation.fromNamespaceAndPath` (the public constructor becomes private) |
| 1.21.2 | `InteractionResult.sidedSuccess` removed; `ResultSlot#setRecipeUsed` lost its `Level` argument |
| 1.21.5 | `Inventory#selected` becomes private, replaced by `getSelectedSlot()` |
| 1.21.6 | GUI stack swaps to `Matrix3x2f` (`pushPose`→`pushMatrix`, 3-arg `translate`/`scale` lose z); `setTooltipForNextFrame` replaces `renderTooltip(Font, ItemStack, …)`; Forge moves to EventBus 7, which splits `net.minecraftforge.eventbus.api` into `bus`/`listener` and replaces `FMLJavaModLoadingContext#getModEventBus()` with `getModBusGroup()` returning a `BusGroup`. `BusGroup` is not an `IEventBus` and `IEventBus` does not exist in EventBus 7, so the bus-fetch and `DeferredRegister#register` *statements* are version-conditional, not just the imports |
| 1.21.7 | NeoForge moves the client packet distributor to `ClientPacketDistributor` |
| 1.21.9 | `Level#isClientSide` becomes private; `ServerPlayer#getServer` removed; `Window#getWindow` renamed to `handle`; `MouseButtonEvent` input objects |
| 1.21.11 | `ResourceLocation` renamed to `Identifier`; `renderContents` split out of `render` |
| 26.1 | Minecraft ships unobfuscated: no mappings, no refmaps, no remap step; `GuiGraphics` → `GuiGraphicsExtractor`, every `render*` → `extract*`; `Minecraft#setScreen` still exists; Fabric `screenhandler` API renamed to `menu`; `ExtendedScreenHandlerFactory` → `ExtendedMenuProvider` |
| 26.2 | `Minecraft#setScreen` deleted (the inventory redirect hook moves to `Gui#setScreen`); `Recipe#assemble` loses `RegistryAccess` |
| 26.3 | Minecraft moves from GLFW to SDL3 (`org.lwjgl.glfw` leaves the classpath; use `org.lwjgl.sdl.SDLMouse.SDL_WarpMouseInWindow`); raw modifier bits become SDL keymods, so use `InputWithModifiers#hasShiftDown`; `InputConstants.Type.KEYSYM` → `KEYBOARD`; `Player#drop` gains a `Prediction` argument |

### The Forge toolkit

Forge has no workable plugin of its own here, so it is worth recording what was tried:

| Toolkit | Usable? | Why |
| :--- | :--- | :--- |
| ForgeGradle 6 | no | Gradle 8 only, and Loom 1.18.1 needs Gradle 9 |
| ModDevGradle `legacyforge` | no | asks for `net.minecraftforge:forge:<v>:universal-srg` - a classifier that only exists for the pre-1.20.2 SRG layout, so it cannot build 1.21.1 |
| **ForgeGradle 7** | **yes** | the rewrite that runs on Gradle 9.3+; resolves Forge through its own mavenizer |

ForgeGradle 7 is *stateless*: applying it does nothing until `minecraft.dependency(...)` is declared,
and it does not add its own repositories once another plugin has already declared some.
`forge/build.gradle.kts` therefore registers the mavenizer repository and Mojang's libraries
repository explicitly. Without them the Forge dependency resolves to an empty module and every
`net.minecraft.*` import fails - which reads as a source problem and is not one.

## 7. Enabled targets and the gaps that remain

Not every loader has a node on every one of the 23 versions. What is left is a short list, each entry
with a concrete reason. Two entries this section used to carry are now closed, and they are recorded
here so their reasons are not lost.

### 1.20.1: Fabric is supported, Forge is not

**1.20.1 is supported on Fabric** - it is in `fabricVersions` and uses the pre-1.20.5 networking
implementation (the `//? if >=1.20.5` guards on `DayZInventoryPayload` / `DayZInventoryOpenData` mark
that boundary).

What is missing is **Forge 1.20.1**, and the reason is specific: Forge 1.20.1 runs on SRG names, so it
needs reobfuscation plus a Searge mixin refmap. ForgeGradle 7 has neither (it asks for a companion
"Renamer Gradle" for the reobf and has no mixin support at all), and ForgeGradle 6 - which has both -
is Gradle 8 only, while Loom 1.18.1 needs Gradle 9. Adding it means a nested Gradle 8 build, not
another node.

### Forge 1.21.6-1.21.11: no longer blocked

Forge is **done** on these versions. Forge moved to EventBus 7 in 1.21.6, which split
`net.minecraftforge.eventbus.api` into the `bus` and `listener` subpackages and replaced
`FMLJavaModLoadingContext#getModEventBus()` with `getModBusGroup()` returning a `BusGroup`.
`DayZInventoryForge` now switches the import block *and* the bus/register statement on
`//? if >=1.21.6`, because `BusGroup` is not an `IEventBus` and `IEventBus` does not exist in EventBus
7 at all. Forge is therefore built for 1.20.6 through 1.21.11.

### 1.20.2-1.20.4 Fabric: done and in the matrix

These three versions are in the matrix on Fabric. They sit on the pre-1.20.5 networking path
(`CustomPacketPayload` exists but has no `Type` and no `StreamCodec`, so the bare `ResourceLocation` +
`FriendlyByteBuf` channel used by 1.20.1 still applies). Two extra conditions were needed: the
`RecipeHolder` boundary is 1.20.2 rather than 1.20.5, and the `Block#use` → `useWithoutItem` split is
1.20.5.

### NeoForge 1.20.2-1.20.4

1.20.1 predates NeoForge. NeoForge 20.2.x still uses the old `NetworkRegistry` / `SimpleChannel`
stack, which this tree does not carry. There is **no NeoForge release for 1.20.3 at all** (NeoForge
went straight from 20.2.x to 20.4.x). NeoForge 20.4.x has the registrar API
(`RegisterPayloadHandlerEvent` → `IPayloadRegistrar`) but predates `StreamCodec`, so the shared payload
would need a second shape. Fabric covers all three versions.

### 1.21.2 Forge

Forge never published a 1.21.2 release.

### 26.x Forge

Not a target this mod builds for; NeoForge is the supported route on the 26.x line.

### 1.20.5: Fabric only

NeoForge published that release without the `moddev-config.json` ModDevGradle requires, and Forge has
no 1.20.5 release at all, so this version has a Fabric node only.

Fabric and NeoForge together cover every version from 1.20.6 upward, plus Fabric alone for
1.20.1-1.20.5.

## 8. Adding a Minecraft version

1. Add `versions/<mc>/gradle.properties`, copying the closest existing version and updating
   `deps.minecraft`, `deps.java`, `deps.mixin-compat`, `deps.pack-format`, the loader versions and
   `meta.minecraft-range`.
2. Add the version to the relevant list in `settings.gradle.kts`.
3. Run `./gradlew :common:<mc>:compileJava` and port what breaks. Add the version to `forgeVersions`
   as well only if it falls inside Forge's buildable range (1.20.6-1.21.11, see section 7; 1.21.2 is
   skipped because Forge never released it).
4. Add the version to `README.md` / `README.en.md` and to the store descriptions in
   `docs/store-descriptions/`.
5. `./gradlew chiseledBuild` to confirm the whole matrix still builds.

Publishing picks the new version up automatically: the game version tag and the loader tag are read
from the node, not hardcoded per release.

## 9. Publishing

`dayz-loader` configures `me.modmuss50.mod-publish-plugin` for every loader node:

- Modrinth project `8asZxzdc`, CurseForge project `1596267`.
- Tokens come from the environment: `MODRINTH_TOKEN` (falls back to `MODRINTH_PAT`) and
  `CURSEFORGE_API_KEY` (falls back to `CURSEFORGE_TOKEN`).
- `publish.dry_run` defaults to `true`, so a stray `publishMods` cannot submit anything.
- `publishAll` depends on every node's `publishMods`, ordered so CurseForge receives one upload at a
  time instead of a burst.

**CurseForge never replies with a URL.** Every upload goes into human review; the API accepts the
file and returns. A green `publishCurseforge` therefore means *submitted*. Modrinth returns
immediately.

The CurseForge project **description** cannot be updated from the build - the upload token is a
legacy upload-only token and the Eternal API that edits metadata rejects it. `docs/store-descriptions/curseforge.md`
is the copy-paste source for that page and has to be kept in sync by hand. The Modrinth description
*is* pushed via the API.

## 10. Gotchas

1. **`gradlew` must stay executable** (mode `100755`). CI also runs `chmod +x ./gradlew`.
2. **The Gradle wrapper is 9.7.0.** Fabric Loom 1.18.1 publishes
   `org.gradle.plugin.api-version = 9.7.0`; an older wrapper fails to resolve it with a variant
   matching error that does not mention the Gradle version.
3. **Do not hardcode `org.gradle.java.home`.** It is machine-specific and breaks CI. The launcher JDK
   is supplied through `JAVA_HOME`; per-version toolchains come from `versions/<mc>/gradle.properties`
   and are downloaded by the foojay resolver.
4. **`prop()` reads two sources.** `findProperty` first (so `-P` and the root `gradle.properties`
   win), then `versions/<mc>/gradle.properties`. If a property "disappears", check the node's file
   name matches `sc.current.project`.
5. **Never put comments inside a `//? if` block** - see section 5.
6. **A mixin whose target has been renamed does not degrade, it crashes.** The client mixin configs
   are `required: true`. When a target moves, grep for the call sites and hook the method everything
   routes through; do not guess.
7. **`MixinConfigs` compatibility level is per version.** It is expanded from `deps.mixin-compat` into
   the mixin configs by `processResources`; a hardcoded `JAVA_25` on a Java 21 node fails Mixin's
   own validation.
8. **Do not access `Platform.HELPER` before loader init.** It is `null` until the entrypoint runs.
   Use the null-safe `Platform.isModLoaded(...)` / `Platform.isReady()` probes.
