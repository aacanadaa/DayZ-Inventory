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
(module, Minecraft version) pair such as `:fabric:26.2`.

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
forge/                       Legacy Forge module - present, not built (see section 7)
```

`stonecutter` names each node after its Minecraft version and places it under the branch directory,
so `:fabric:26.2` has its project directory at `fabric/versions/26.2/`. Those directories are build
output; only `versions/<mc>/gradle.properties` is source-controlled.

## 3. How the Gradle build fits together

### `settings.gradle.kts`

Declares the tree and the matrix:

```kotlin
val fabricVersions = listOf("1.21.1", "1.21.11", "26.2")
val neoforgeVersions = listOf("1.21.1", "1.21.11", "26.2")

stonecutter {
    create(rootProject) {
        branch("common") { versions(*commonVersions.toTypedArray()) }
        branch("fabric") { versions(*fabricVersions.toTypedArray()) }
        branch("neoforge") { versions(*neoforgeVersions.toTypedArray()) }
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
| `dayz-loader` | fabric / neoforge | sharing the `common` node's sources and configuring `publishMods` |

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

| Minecraft | Node | Java | Fabric Loom | Notes |
| :--- | :--- | :--- | :--- | :--- |
| 26.2 | `:fabric:26.2`, `:neoforge:26.2` | 25 | `fabric-loom` (unobfuscated) | no mappings, no refmaps, no remap step |
| 1.21.11 | `:fabric:1.21.11`, `:neoforge:1.21.11` | 21 | `fabric-loom-remap` | last obfuscated line; input events are objects |
| 1.21.1 | `:fabric:1.21.1`, `:neoforge:1.21.1` | 21 | `fabric-loom-remap` | payload networking; raw mouse coordinates |

`loom-back-compat` picks the Loom flavour from `sc.current.parsed < "26"` and aliases the `mod*`
configurations the unobfuscated Loom removed, so one build script covers both sides of that split.

## 7. Targets that are not enabled yet

Both are shipped from their own branches today — 1.20.1 from the `1.20.1` branch, Forge from
`1.21.1-forge` — and are **not** regressed by this tree. Each already
has a `versions/<mc>/gradle.properties`, and uncommenting one line in `settings.gradle.kts` is all
the build wiring needed.

### 1.20.1 (Fabric, Forge)

The whole 1.20.5 networking rewrite is missing from this version:

- No `CustomPacketPayload` / `StreamCodec`. `DayZInventoryPayload` and `DayZInventoryOpenData` do not
  exist, so the channel is a bare `ResourceLocation` plus a `FriendlyByteBuf`.
- `ExtendedScreenHandlerType` takes no opening-data codec; the factory implements
  `writeScreenOpeningData(ServerPlayer, FriendlyByteBuf)` instead of `getScreenOpeningData`.
- Fabric registers one receiver per channel via `ServerPlayNetworking.registerGlobalReceiver(ResourceLocation, ...)`.
- `ResourceLocation` still has a public constructor, so `ResourceLocation.fromNamespaceAndPath` is not
  available.

The `GuiGraphics` era also differs: `render` takes `(GuiGraphics, int, int, float)` and there is no
`renderContents`; the inventory-screen redirect mixin targets `Minecraft#setScreen`, not
`Gui#setScreen`.

### Forge (1.20.1, 1.21.1)

`forge/` still contains 1.20.1-era `SimpleChannel` / `NetworkRegistry` / `registerMessage` code. The
1.21.1 Forge module needs the same payload-based networking the NeoForge module already has, against
`net.minecraftforge` packages. `forge/build.gradle.kts` uses ModDevGradle's `legacyforge` platform,
which is wired up and resolving - only the sources need porting.

## 8. Adding a Minecraft version

1. Add `versions/<mc>/gradle.properties`, copying the closest existing version and updating
   `deps.minecraft`, `deps.java`, `deps.mixin-compat`, `deps.pack-format`, the loader versions and
   `meta.minecraft-range`.
2. Add the version to the relevant list in `settings.gradle.kts`.
3. Run `./gradlew :common:<mc>:compileJava` and port what breaks.
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
