# Building DayZ Inventory

How the multi-version, multi-loader build is put together, why it is shaped this way, how to add a
Minecraft version, and the list of things that have actually broken here before. Read it before
changing the build or anything under `client/`.

---

## 1. Overview

One branch, one source tree, **23 Minecraft versions (1.20.1 – 26.3) and 54 jars**: 23 Fabric,
18 NeoForge and 13 Forge. Version differences are marked inline with
[Stonecutter](https://stonecutter.kikugie.dev/) `//? if` comments, and the build produces one jar
per *node* - a (loader, Minecraft version) pair such as `:fabric:26.2`.

| Minecraft | Fabric | NeoForge | Forge | Java |
| :--- | :---: | :---: | :---: | :---: |
| 1.20.1 | ✅ | — | ✅ | 17 |
| 1.20.2 – 1.20.4 | ✅ | — | — | 17 |
| 1.20.5 | ✅ | — | — | 21 |
| 1.20.6 – 1.21.1 | ✅ | ✅ | ✅ | 21 |
| 1.21.2 | ✅ | ✅ | — | 21 |
| 1.21.3 – 1.21.11 | ✅ | ✅ | ✅ | 21 |
| 26.1 – 26.3 | ✅ | ✅ | — | 25 |

Why the gaps exist is in [section 7](#7-enabled-targets-and-the-gaps-that-remain).

The project uses Stonecutter's **branched** layout (a shared `common/` plus one module per loader,
each with its own version nodes) rather than the flat one, because the loaders need different
toolkits. [Architectury](https://docs.architectury.dev/) was evaluated and is not used: it would be a
hard runtime dependency for every player, and a small hand-written platform abstraction
(`IPlatformHelper`) does the job.

## 2. Layout

```
settings.gradle.kts           Stonecutter tree, built from versions/ (+ plugin versions)
stonecutter.gradle.kts        Controller: active version, chiseledBuild, buildMods, publishAll, matrix
gradle.properties             Mod metadata, publishing ids, toolchain versions
build-logic/                  Convention plugins: dayz-common, dayz-loader, Utils.kt
versions/<mc>/gradle.properties   Everything about one Minecraft version, including its loaders

common/     Loader-neutral code: screen, menu, packets, config, mixins, platform interface
fabric/     Fabric entrypoints + FabricPlatformHelper
neoforge/   NeoForge entrypoint, payload registration + NeoForgePlatformHelper
forge/      Forge entrypoint, channel + ForgePlatformHelper (1.20.1 and 1.20.6 - 1.21.11)
```

`<branch>/versions/<mc>/` are build directories (gitignored). Only `versions/<mc>/gradle.properties`
at the root is source.

## 3. How the build fits together

### The version matrix

There is no list of versions in any build script. Each `versions/<mc>/gradle.properties` declares
which loaders that version is built for:

```properties
loaders=fabric,neoforge,forge
deps.minecraft=1.21.1
deps.java=21
...
```

`settings.gradle.kts` lists the `versions/` directory, sorts it numerically and builds the
Stonecutter tree from it; CI lists the same directory to create one job per version. Adding a
version is one new directory.

`-Pdayz.versions=1.21.1,26.2` limits the tree to those versions. Configuring a node downloads and
prepares that Minecraft release, so a full configuration is heavy; CI builds one version per job this
way. In a limited tree the `common` branch also keeps the active version (`stonecutter active` must
name a version that exists), which `buildMods` does not build.

### Controller tasks (`stonecutter.gradle.kts`)

| Task | What it does |
| :--- | :--- |
| `chiseledBuild` | builds every node, `common` included |
| `buildMods` | builds every loader node - what CI runs |
| `publishAll` / `publishModrinthAll` / `publishCurseforgeAll` | publishes every loader node, ordered one at a time |
| `matrix` | prints the node list |

### Convention plugins (`build-logic/`)

| Plugin | Applied to | Responsibility |
| :--- | :--- | :--- |
| `dayz-common` | every node | versions, Java toolchain, repositories, manifest expansion, version replacements, licence in the jar, generated-source wiring |
| `dayz-loader` | fabric / neoforge / forge | compiling `common`'s sources into the jar, refmap entries, the `verifyJar` packaging check, publishing |

`Utils.kt` holds the `prop()` / `mc` / `branch` / `isLegacyForge` / `needsRefmap` helpers and
`changelogSection()`. Inside a precompiled script plugin, Stonecutter's extension is reached through
the `sc` accessor; `stonecutter { }` is not available there.

`prop()` reads `findProperty` first (so `-P` and the root `gradle.properties` win) and then
`versions/<mc>/gradle.properties` directly: Gradle only merges a project's own properties file with
the root one, and the node directories are not where the version files live.

### The three loader toolchains

| Loader | Plugin | Notes |
| :--- | :--- | :--- |
| Fabric | Fabric Loom via `dev.kikugie.loom-back-compat` | picks `fabric-loom-remap` below 26.1 and `fabric-loom` from 26.1; the remapped jar is `remapJar` below 26.1, `jar` above |
| NeoForge | ModDevGradle (`net.neoforged.moddev`) | official names since 1.20.2: no refmap, no reobfuscation |
| Forge 1.20.6+ | ForgeGradle 7 | official names: no refmap, no reobfuscation |
| Forge 1.20.1 | ModDevGradle Legacy (`net.neoforged.moddev.legacyforge`) | SRG names: `reobfJar` writes the shipped jar to `build/libs` (the dev jar goes to `build/devlibs`), and the Mixin annotation processor writes the Searge refmap |

`forge/build.gradle.kts` declares both Forge plugins with `apply false` and applies one per node.
What was tried for Forge, so nobody has to try it again:

| Toolkit | Verdict |
| :--- | :--- |
| ForgeGradle 6 | Gradle 8 only; Loom needs Gradle 9 |
| ModDevGradle Legacy | builds 1.20.1 (and older); cannot build 1.20.2+, which have no `universal-srg` artifact |
| ForgeGradle 7 | builds 1.20.6+; has no reobfuscation or mixin support, so it cannot build 1.20.1 |

ForgeGradle 7 is stateless: it does nothing until `minecraft.dependency(...)` is declared, and it does
not add its own repositories once another plugin has. `forge/build.gradle.kts` therefore adds the
mavenizer repository and Mojang's library repository itself. Without them the Forge dependency
resolves to an empty module and every `net.minecraft.*` import fails - which looks like a broken
source tree and is not one.

## 4. Version-processed sources

Stonecutter writes each node's processed copy of the shared sources to
`<branch>/versions/<mc>/build/generated/stonecutter/main/{java,resources}`. `dayz-common` compiles
that generated tree, never the raw `src/`, which holds every version's code side by side.

`dayz-loader` adds the **`common` node's** generated tree to each loader's compile, so every class and
mixin config exists once, inside the loader jar. Do not "simplify" this to
`implementation(project(":common"))`: that puts a second copy of every class and mixin config on the
classpath, and a refmap can only be produced by running the Mixin annotation processor over the
annotated sources - a precompiled `common.jar` would give Mixin classes it cannot remap.

**Keep every loader source under `src/main/java`.** A `src/client/java` root is invisible to this
pipeline: 1.8.0 shipped every Fabric jar without its client entrypoint that way.

## 5. Conditional compilation

### `//? if` comments

For anything structural - a different signature, hook target or removed method:

```java
//? if >=26.1 {
    private void drawDayZPanels(GuiGraphicsExtractor guiGraphics, float partialTick, int mouseX, int mouseY) {
//?} else {
    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
//?}
```

- `elif` is available; **conditions cannot nest** - write a flat `if / elif / else` chain.
- Keep `//` comments *before* a `//? if` line where you can, and never put `/* */` or `/** */`
  comments inside a conditional block.

### Replacements

Pure renames are bulk replacements in `dayz-common.gradle.kts`, written against the **newest** names
and applied backwards to older nodes:

```kotlin
sc.replacements.string(sc.current.parsed < "1.21.11") {
    replace("Identifier", "ResourceLocation")
}
```

**A replacement works in both directions.** Where its condition holds, the first string becomes the
second. Where it does not, every occurrence of the *second* string becomes the first. So the second
string must never appear in the sources on its own: a bare `render` once turned the
`net.minecraft.client.renderer` package into `extractRenderStateer` on 26.x. Keep both sides
specific (`"super.extractRenderState("` rather than `"extractRenderState"`), and use a `//? if`
block for anything that is not a plain rename.

### Per-version properties

`versions/<mc>/gradle.properties` holds `loaders`, `deps.minecraft`, `deps.java`,
`deps.mixin-compat`, `deps.pack-format`, the loader versions and `meta.minecraft-range`. Optional
`meta.<loader>-range` entries set the minimum loader version players need; without one it is the
build version. The root `gradle.properties` sets `meta.fabric-loader-range` for every version.

## 6. Per-version API boundaries

Which version each API changed in. Every row was established from that version's real jar or
sources, not inferred from the version number - check before trusting a guess.

| Since | Change |
| :--- | :--- |
| 1.20.2 | `RecipeHolder`: `getRecipeFor` returns `Optional<RecipeHolder<CraftingRecipe>>`, `setRecipeUsed` takes a `RecipeHolder<?>`. `mouseScrolled` gains a horizontal axis; `renderBackground` a 4-argument form; `renderEntityInInventoryFollowsMouse` replaces `renderEntityInInventory`. Forge stops using SRG names |
| 1.20.5 | Typed payloads (`CustomPacketPayload.Type`, `StreamCodec`, `RegistryFriendlyByteBuf`); `ItemStack#getTag` removed (`isSameItemSameComponents`); `Block#use` split into `useItemOn` / `useWithoutItem`; `MobEffectInstance#getEffect` returns a `Holder<MobEffect>` |
| 1.21 | `CraftingInput`; `ResourceLocation.fromNamespaceAndPath`; Curse of Binding becomes the `PREVENT_ARMOR_CHANGE` enchantment component |
| 1.21.2 | `InteractionResult.sidedSuccess` removed; `ResultSlot#setRecipeUsed` loses its `Level`; `blitSprite` takes a render-type function |
| 1.21.4 | `GuiGraphics#enableScissor` transforms its rectangle through the current pose |
| 1.21.5 | `Inventory#selected` becomes private (`getSelectedSlot()`) |
| 1.21.6 | GUI pose stack becomes `Matrix3x2f`; `setTooltipForNextFrame`; `renderContents` split out of `render` **with the `hoveredSlot` assignment**; `renderBackground` moves into `renderWithTooltip`, before `render`; `blitSprite` takes a `RenderPipeline`; mob effect icons become GUI sprites (`Gui.getMobEffectSprite`); Forge moves to EventBus 7 (`bus`/`listener` packages, `getModBusGroup()` returning a `BusGroup`, no `IEventBus`) |
| 1.21.7 | NeoForge's client packet distributor moves to `ClientPacketDistributor` |
| 1.21.9 | `Level#isClientSide` private; `ServerPlayer#getServer` removed; `Window#getWindow` → `handle`; `MouseButtonEvent` input objects |
| 1.21.11 | `ResourceLocation` → `Identifier` |
| 26.1 | Unobfuscated: no mappings, refmaps or remap step. `GuiGraphics` → `GuiGraphicsExtractor`, `render*` → `extract*`, `renderBg` removed, `ClickType` → `ContainerInput`, `imageWidth`/`imageHeight` final; Fabric `screenhandler` API → `menu`, `playC2S` → `serverboundPlay` |
| 26.2 | `Minecraft#setScreen` deleted (hook `Gui#setScreen`); `Recipe#assemble` loses `RegistryAccess`; `getMobEffectSprite` moves to `Hud` |
| 26.3 | GLFW → SDL3 (`org.lwjgl.glfw` gone; use `SDL_WarpMouseInWindow` and `hasShiftDown()`); `InputConstants.Type.KEYSYM` → `KEYBOARD`; `Player#drop` takes a `Prediction` |

## 7. Enabled targets and the gaps that remain

- **NeoForge 1.20.1 - 1.20.5:** 1.20.1 predates NeoForge; 1.20.2 used the old `SimpleChannel` stack;
  1.20.3 had no NeoForge release; 1.20.4 predates `StreamCodec`; 1.20.5's release lacks the
  `moddev-config.json` ModDevGradle needs.
- **Forge 1.20.2 - 1.20.5:** 1.20.2 - 1.20.4 would need their own networking variant and are rarely
  played on Forge; Forge never released 1.20.5.
- **Forge 1.21.2:** Forge never released it.
- **Forge 26.x:** not a target; NeoForge is the supported route.

## 8. Runtime rules

1. **`Platform.HELPER` is null until the loader entrypoint runs.** Never touch it from a static
   initialiser or early mixin; use `Platform.isReady()` / `Platform.isModLoaded()`.
2. **New loader-specific behaviour goes through `IPlatformHelper`**, implemented in all three loader
   helpers. Never branch on the loader inside `common/`.
3. **Optional mods are never referenced directly.** JEI, REI, EMI, Curios, Accessories, Trinkets,
   Mouse Tweaks and the thirst mods are reached through reflection or key mappings
   (`client/compat/`), and the mod must launch and work with none of them.
4. **Mouse Tweaks' API interface** is compiled from a stand-in in `common/src/main/java/yalter/`.
   `dayz-loader` strips it from every jar (and `verifyJar` fails if it ships), and the mixin that adds
   it to the screen lives in `dayz-inventory.compat.mixins.json`, a non-required config whose plugin
   only applies it when Mouse Tweaks is installed.
5. **The main and client mixin configs are `required: true`.** A target that moved is a crash at
   launch, not a no-op. When a target moves, find the method every call site goes through rather than
   guessing; `ScreenRedirectMixin` hooks `Gui#setScreen` on 26.2+ for that reason.
6. **`@Shadow` on `AbstractContainerScreen` does not resolve in production.** The screen reads its
   inherited fields itself; mixins call into it.
7. **The screen draws under its own scale** whenever the window is narrower than the 540 x 220
   panel. Anything that hands real screen coordinates to vanilla (the player preview, the Vicinity
   scissor) cancels that scale first, which is correct whether or not `enableScissor` applies the
   pose.
8. **Settings** live in `config/dayz_inventory.properties` (`DayZConfig`), loaded by each loader's
   entrypoint right after the platform helper is installed.

Features that must survive refactors: the Vicinity grid and its 10-tick rescan, inline container
drawers, the 2x2 crafting grid with the server-side result, the 2x Hands slot bound to the selected
hotbar slot, drag-to-equip onto the Survivor panel, and the CURIOS / ACCESSORIES / TRINKETS and
recipe-viewer buttons.

## 9. Adding a Minecraft version

1. Copy the closest `versions/<mc>/gradle.properties` to a new directory and update `loaders`,
   `deps.*` and `meta.minecraft-range`.
2. Build it alone: `./gradlew buildMods -Pdayz.versions=<mc>`. Port what breaks - with a `//? if`
   block, or a replacement when it is a pure rename (mind the reverse rule in section 5).
3. Add the version to the README tables and `docs/store-descriptions/`.

CI and publishing pick the version up from the directory; game version and loader tags come from the
node.

## 10. CI and publishing

`.github/workflows/build.yml`:

- **Pull requests and pushes to `main`** build every version, one job each, and upload the jars as
  artifacts. A job named `Build` reports the overall result. Pull requests never publish.
- **A `v*` tag** builds everything, then creates the GitHub release with every jar and this
  version's CHANGELOG section, then publishes each version to Modrinth and CurseForge - only after
  every build has passed.
- **Manual runs** can publish (optionally to one platform, for finishing a partial upload) or
  delete Modrinth versions.

Publishing uses `me.modmuss50.mod-publish-plugin`, configured once in `dayz-loader`:

- Tokens come from the environment: `MODRINTH_TOKEN` (or `MODRINTH_PAT`) and `CURSEFORGE_API_KEY`
  (or `CURSEFORGE_TOKEN`). Store them as repository **secrets**, not variables.
- `publish.dry_run` defaults to `true`; CI passes `-Ppublish.dry_run=false`.
- The version page text is the version's section of `CHANGELOG.md`; publishing refuses to run when
  that section is missing.
- **CurseForge never replies with a URL** - every upload goes to human review, so a green task means
  *submitted*.
- Project descriptions are **not** published by the build. `docs/store-descriptions/*.md` are the
  copy-paste sources for both pages and are edited by hand.

Release checklist: bump `mod.version` in `gradle.properties`, add the CHANGELOG section, update the
README and store descriptions, merge, then tag `v<version>`.

## 11. Gotchas

1. **`gradlew` must stay executable** (mode `100755`).
2. **Do not hardcode `org.gradle.java.home`.** The launcher JDK (25) comes from `JAVA_HOME`; the
   Java 17 / 21 toolchains are downloaded by the foojay resolver.
3. **Loom needs a recent Gradle.** Loom publishes the minimum Gradle API it supports, and too old a
   wrapper fails with a variant-matching error that never mentions Gradle.
4. **`fabric.mod.json` does not understand Maven ranges.** `[1.21.5,1.21.6)` matches nothing and
   produces "requires version [1.21.5,1.21.6) ... but only the wrong version is present: 1.21.5".
   Fabric gets the comparison form (`>=1.21.5 <1.21.6`) from `minecraft_range_fabric`; the toml files
   use the Maven form.
5. **Every manifest value is a placeholder** expanded by `processResources`; an unknown token fails
   the build on purpose. A hardcoded `version` once shipped a 1.4.1 jar that called itself 1.4.0.
6. **The refmap entry is added per loader.** Fabric below 26.1 and Forge 1.20.1 need
   `"refmap": "dayz-inventory.refmap.json"` in the mixin configs (`dayz-loader` adds it); everything
   else must not have it. `verifyJar` checks both the file and the reference.
7. **The mixin compatibility level is per version** (`deps.mixin-compat`); `JAVA_25` on a Java 21
   node fails Mixin's validation.
8. **Below 26.1, a Fabric node's shippable jar is `remapJar`, and Forge 1.20.1's is `reobfJar`.**
   Never detect `remapJar` with `tasks.names`: Loom registers it later, and the development jar went to
   Modrinth that way in 1.7.1.
9. **`KeyMapping` fields are obfuscated in production.** Use the `KeyMappingAccessor` mixin or public
   methods (`getName`, `saveString`), never reflection by field name.
