# 构建 DayZ Inventory

**中文** | [English](BUILDING.en.md)

本文说明统一后的多版本 / 多加载器构建是怎么搭起来的、为什么长这样，以及怎么再加一个 Minecraft 版本。

---

## 1. 为什么从"一版一分支"改成单源码树

这个项目原来每个 Minecraft 版本一个分支（`main` 是 1.20.1，另有 `1.21.1`、`1.21.11`、`26.2`）。
每个 bug 修复都要往所有分支合一遍，每个分支还要单独构建、单独测试。

现在是一个分支、一份源码树。版本相关的代码用 `//? if <条件>` 注释内联标注，
构建则按 **node**（节点）产出产物 —— 一个 node 就是一对（模块, Minecraft 版本），例如 `:fabric:26.2`。
当前矩阵覆盖 **23 个 Minecraft 版本**（1.20.1 至 26.3），共 **53 个可发布 jar**：Fabric 23 个、NeoForge 18 个、Forge 12 个。

关于实现方式有两条路。[Stonecutter 官方文档](https://stonecutter.kikugie.dev/) 描述了两种布局：
**扁平式**（一份 `src/`，node 同时承载版本与加载器，加载器差异用构建常量切换）和
**分支式**（共享 `common/` + 各加载器模块，每个模块各有一套版本 node）。本项目用的是分支式，因为
两个加载器确实需要各自的工具链（Fabric Loom 与 NeoForge ModDevGradle），而且它能保留代码库原本就有的
`common/` / `fabric/` / `neoforge/` 划分。

[Architectury](https://docs.architectury.dev/) 作为加载器抽象层被评估过，但**没有采用**，两个原因：

1. Architectury API 会成为每一份下载的硬运行时依赖。本模组已经有一个手写的、对用户零成本的
   平台抽象（`IPlatformHelper` / `Platform`）。
2. 从 26.1 起 Minecraft **不再混淆**，Fabric Loom 也拆成了 `fabric-loom`（26.1+）与
   `fabric-loom-remap`（<26.1）。Architectury Loom 在这一层之上又叠了一层，而在 26.x 上
   没有成熟的先例 —— 而 26.x 恰恰是最重要的目标。

---

## 2. 目录结构

```
settings.gradle.kts          Stonecutter 树 + 插件管理 + 版本矩阵
stonecutter.gradle.kts       控制器脚本：当前激活版本、聚合任务
gradle.properties            模组元数据、发布 ID、共享工具版本
build-logic/                 所有 node 共用的约定插件
versions/<mc>/gradle.properties   每个版本的依赖坐标

common/                      与加载器无关的代码（界面、菜单、网络包、mixin）
  build.gradle.kts           每个 Minecraft 版本各执行一次
  src/main/java              共享源码，内含 `//? if` 标记
fabric/                      Fabric 入口 + FabricPlatformHelper
neoforge/                    NeoForge 入口 + NeoForgePlatformHelper
forge/                       Forge 入口 + ForgePlatformHelper（构建 1.20.6–1.21.11，见第 6、7 节）
```

Stonecutter 以 Minecraft 版本给每个 node 命名，并把它们放在分支目录下，
所以 `:fabric:26.2` 的工程目录是 `fabric/versions/26.2/`。这些目录都是构建产物，
受版本控制的只有 `versions/<mc>/gradle.properties`。

## 3. Gradle 构建是怎么串起来的

### `settings.gradle.kts`

声明树与矩阵：

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

这个文件里有两个容易踩的、属于"承重墙"级别的细节：

- **根分支是刻意留空的。** 它存在（Stonecutter 一定有一个根分支），但不注册任何版本。
  给它注册版本会为每个 Minecraft 版本生成一个没有构建脚本、没有产物的空工程，
  而控制器里那些按项目排布的发布任务无法跨越"根本没有该任务"的工程工作。
- **`pluginManagement.plugins {}` 必须同时声明两个 Loom。**
  `dev.kikugie.loom-back-compat` 是**以编程方式**应用 `net.fabricmc.fabric-loom` 或
  `net.fabricmc.fabric-loom-remap` 之一的，而编程式的 `pluginManager.apply(id)` 走的是该工程的
  buildscript 仓库、而不是 `pluginManagement.repositories`。在这里写上两个 id，
  再在 `stonecutter.gradle.kts` 里以 `apply false` 声明，才能让每个 node 都解析得到。

### `stonecutter.gradle.kts`

控制器脚本。保存当前激活版本，注册 `chiseledBuild` / `publishAll` / `matrix`，
并以 `apply false` 声明各加载器插件，让 node 工程继承它们到 buildscript classpath。

### `build-logic/`

两个预编译约定插件：

| 插件 | 应用对象 | 职责 |
| :--- | :--- | :--- |
| `dayz-common` | 每个 node | 版本字符串、Java 工具链、仓库、manifest 展开、版本重命名、jar 内许可证、生成源码接线 |
| `dayz-loader` | fabric / neoforge / forge | 共享 `common` node 的源码，并配置 `publishMods` |

`build-logic/src/main/kotlin/Utils.kt` 里是 `prop()` / `mc` / `branch` 等访问器。
注意：在预编译脚本插件里**不能**直接写 `stonecutter { }`，必须通过 `sc` 访问器取扩展。

## 4. 版本预处理后的源码

Stonecutter 会预处理共享的 `src/` 目录树，并把结果写到：

```
<分支>/versions/<mc>/build/generated/stonecutter/<sourceSet>/{java,resources}
```

`dayz-common` 让 `compileJava` 指向这棵生成树，而**不是**原始源码 —— 因为原始树里同时躺着所有版本的代码，
Stonecutter 只在它生成的副本里把非激活分支注释掉。直接编译生成树还有一个好处：
"当前激活版本"和其它 node 的行为完全一致，不再依赖工作区是否已经 chisel 到它。

`dayz-loader` 再把 **common** node 的生成树加到加载器自己的编译任务里，
于是每个加载器 jar 里只有一份共享类、一份共享 mixin 配置。这不只是整洁问题：
26.1 以下 Fabric 与 Forge 需要 **mixin refmap**，而 refmap 只能由 Mixin 注解处理器扫描被注解的
**源码**产生。给它一个编译好的 `common.jar`，得到的类 Mixin 永远无法重映射。

### 每版本属性

`versions/<mc>/gradle.properties` 是声明某个 Minecraft 版本坐标的唯一位置
（加载器版本、Java 级别、mixin 兼容级别、pack format、发布版本区间）。
Stonecutter 只会为注册在**根分支**上的版本读取这些文件，而这里的根分支是空的，
所以 `Utils.kt` 自己读：

```kotlin
fun Project.propOrNull(key: String): String? =
    findProperty(key)?.toString()?.takeIf { it.isNotBlank() }
        ?: versionProperties()[key]?.takeIf { it.isNotBlank() }
```

这样每个 Minecraft 版本只有一个文件，而不是每个（分支, 版本）组合一个，
同时 `-P` 覆盖和根 `gradle.properties` 依然优先。

## 5. 条件编译

用了两套机制，怎么选是风格约定而不是硬性要求。

### `//? if` 注释

用于任何**结构性**差异 —— 签名不同、hook 目标不同、方法被删掉：

```java
//? if >=26.2 {
    private void drawDayZPanels(GuiGraphicsExtractor guiGraphics, float partialTick, int mouseX, int mouseY) {
//?} else {
    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
//?}
```

三分支可以用 `elif`。**不要在条件块内部写注释** —— 处理器会重写注释标记，
残留的 `//` 有可能被吃掉，于是一行注释变成一行代码。这种错误很隐蔽，
表现为生成文件里的语法错误。

### `replacements`

对于纯重命名（否则同一个标识符要包几十个 `//? if`），约定插件里声明批量替换：

```kotlin
sc.replacements.string(sc.current.parsed < "1.21.11") {
    replace("Identifier", "ResourceLocation")
    replace("isClientSide()", "isClientSide")
}
```

源码一律按**最新**命名书写（`Identifier`、`isClientSide()`、`GuiGraphicsExtractor`、`ContainerInput`……），
重命名只对更老的 node **反向**应用。把基准文本留在当前正式版上，意味着最新目标完全不需要被改写 ——
而那通常是最常动的目标。

## 6. 版本矩阵

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

合计：**23 个 Minecraft 版本、53 个可发布 jar**（Fabric 23、NeoForge 18、Forge 12）。

Fabric 侧的 Loom 变体分界线仍在 `26.1`：`fabric-loom-remap` 用于 26.1 以下（1.21.11 及更早），
`fabric-loom` 从 26.1 起接管不混淆版本。`loom-back-compat` 依据 `sc.current.parsed < "26"` 选择变体，
并为不混淆版 Loom 删掉的 `mod*` 配置提供别名，因此一份构建脚本同时覆盖这条分界线两侧。

### 每版本 API 边界

下面这张表记录每个 API **从哪个版本起**发生变化，是这份文档里最值钱的部分：
下一次移植新版本时照着它就知道要改什么，不必逐个方法去试。
每一行都是**直接检查对应版本的 jar 与 mappings 得到的，不是从版本号推断出来的**。

| Since | Change |
| :--- | :--- |
| 1.20.2 | 引入 `RecipeHolder`：`RecipeManager#getRecipeFor` 现在返回 `Optional<RecipeHolder<CraftingRecipe>>`，`RecipeCraftingHolder#setRecipeUsed` 接收 `RecipeHolder<?>` 而不是裸的 `Recipe<?>`；`mouseScrolled` 增加了横向轴；`renderBackground` 增加了 4 参数形式；`renderEntityInInventoryFollowsMouse` 取代了 `renderEntityInInventory` |
| 1.20.5 | `CustomPacketPayload` 增加了嵌套的 `Type` 与 `StreamCodec` 体系；出现 `RegistryFriendlyByteBuf`；`ItemStack#getTag` 被移除，改用 `isSameItemSameComponents`；`Block#use` 拆成 `useItemOn`（手持物品）与 `useWithoutItem`（空手），所以开容器的 mixin 从这里起改为针对 `useWithoutItem`，更老的 `use`（多一个 `InteractionHand` 参数）留在下面 |
| 1.21 | `CraftingInput`；`ResourceLocation.fromNamespaceAndPath`（公开构造器变为私有） |
| 1.21.2 | 移除 `InteractionResult.sidedSuccess`；`ResultSlot#setRecipeUsed` 去掉了 `Level` 参数 |
| 1.21.5 | `Inventory#selected` 变为私有，改由 `getSelectedSlot()` 提供 |
| 1.21.6 | GUI 栈换成 `Matrix3x2f`（`pushPose`→`pushMatrix`，3 参数 `translate`/`scale` 去掉 z）；`setTooltipForNextFrame` 取代 `renderTooltip(Font, ItemStack, …)`；Forge 转向 EventBus 7：`net.minecraftforge.eventbus.api` 拆成 `bus`/`listener`，`FMLJavaModLoadingContext#getModEventBus()` 换成返回 `BusGroup` 的 `getModBusGroup()`。`BusGroup` 不是 `IEventBus`，EventBus 7 里也根本不存在 `IEventBus`，所以取 bus 与 `DeferredRegister#register` 的**语句**同样按版本分支，不只是导入 |
| 1.21.7 | NeoForge 把客户端包分发器移到 `ClientPacketDistributor` |
| 1.21.9 | `Level#isClientSide` 变为私有；移除 `ServerPlayer#getServer`；`Window#getWindow` 更名为 `handle`；输入改用 `MouseButtonEvent` 对象 |
| 1.21.11 | `ResourceLocation` 更名为 `Identifier`；`renderContents` 从 `render` 中拆出 |
| 26.1 | Minecraft 不再混淆：没有 mappings、没有 refmap、没有 remap 步骤；`GuiGraphics` → `GuiGraphicsExtractor`，所有 `render*` → `extract*`；`Minecraft#setScreen` 仍然存在；Fabric 的 `screenhandler` API 更名为 `menu`；`ExtendedScreenHandlerFactory` → `ExtendedMenuProvider` |
| 26.2 | 删除 `Minecraft#setScreen`（背包重定向 hook 移到 `Gui#setScreen`）；`Recipe#assemble` 去掉 `RegistryAccess` |
| 26.3 | Minecraft 从 GLFW 转向 SDL3（`org.lwjgl.glfw` 离开 classpath；改用 `org.lwjgl.sdl.SDLMouse.SDL_WarpMouseInWindow`）；裸修饰键位变成 SDL keymods，所以要用 `InputWithModifiers#hasShiftDown`；`InputConstants.Type.KEYSYM` → `KEYBOARD`；`Player#drop` 增加 `Prediction` 参数 |

### Forge 用的工具链

Forge 没有能直接用的插件，所以把试过的都记下来：

| 工具链 | 可用 | 原因 |
| :--- | :--- | :--- |
| ForgeGradle 6 | 否 | 只支持 Gradle 8，而 Loom 1.18.1 需要 Gradle 9 |
| ModDevGradle `legacyforge` | 否 | 会去找 `net.minecraftforge:forge:<v>:universal-srg`，而这个 classifier 只存在于 1.20.2 之前的 SRG 体系，根本构建不了 1.21.1 |
| **ForgeGradle 7** | **可以** | 支持 Gradle 9.3+ 的重写版，通过自带的 mavenizer 解析 Forge |

ForgeGradle 7 是**无状态**的：不写 `minecraft.dependency(...)` 它什么都不做；而且一旦有别的插件
声明过仓库，它就不会再自己添加仓库。所以 `forge/build.gradle.kts` 显式注册了 mavenizer 仓库和
Mojang 的 libraries 仓库。少了这两步，Forge 依赖会解析成一个空模块，
所有 `net.minecraft.*` 导入全部报错 —— 看起来像源码问题，其实不是。

## 7. 已支持的目标与仍然存在的空缺

矩阵里的 23 个版本并非每个加载器在每个版本上都有节点。剩下的缺口不多，每一条都有具体原因。
本节原先列出的两类缺口现在已经补上，这里也一并说明，免得当初的原因被忘掉。

### 1.20.1：Fabric 已支持，Forge 未支持

**1.20.1 在 Fabric 上是支持的** —— 它就在 `fabricVersions` 里，走的是 1.20.5 之前的网络实现
（`DayZInventoryPayload` / `DayZInventoryOpenData` 上的 `//? if >=1.20.5` 守卫就是这条分界）。

缺的是 **Forge 1.20.1**，原因很具体：Forge 1.20.1 跑在 SRG 名字上，所以既需要 reobfuscation，
又需要一份 Searge mixin refmap。ForgeGradle 7 两样都没有 —— 它的 reobf 要额外的 "Renamer Gradle"
配套插件，mixin 支持则完全没有；ForgeGradle 6 虽然两样都有，却只支持 Gradle 8，而 Loom 1.18.1
需要 Gradle 9。要接上它意味着嵌一套 Gradle 8 构建，而不是再开一个 node。

### Forge 1.21.6–1.21.11：已不再是缺口

这几个版本的 Forge **已经完成**。Forge 在 1.21.6 转向 EventBus 7：它把
`net.minecraftforge.eventbus.api` 拆成 `bus` / `listener` 两个子包，并把
`FMLJavaModLoadingContext#getModEventBus()` 换成了返回 `BusGroup` 的 `getModBusGroup()`。
`DayZInventoryForge` 现在在 `//? if >=1.21.6` 上同时切换导入块**和**取 bus / 注册的语句，
因为 `BusGroup` 不是 `IEventBus`，而 EventBus 7 里根本不存在 `IEventBus`。因此 Forge
现在覆盖 1.20.6 到 1.21.11。

### 1.20.2–1.20.4 的 Fabric：已完成，已进矩阵

这三个版本已经以 Fabric 节点进入矩阵。它们走的是 1.20.5 之前的网络实现
（`CustomPacketPayload` 已经存在，但没有 `Type`、也没有 `StreamCodec`，所以 1.20.1 用的那套
裸 `ResourceLocation` + `FriendlyByteBuf` channel 依然适用）。额外需要满足两个条件：
`RecipeHolder` 这条分界是 1.20.2 而不是 1.20.5，而 `Block#use` → `useWithoutItem` 的拆分是 1.20.5。

### NeoForge 1.20.2–1.20.4

1.20.1 早于 NeoForge。NeoForge 20.2.x 仍然使用旧的 `NetworkRegistry` / `SimpleChannel` 体系，
而这棵树里没有带这套实现。1.20.3 **根本没有 NeoForge 发布**（NeoForge 从 20.2.x 直接跳到
20.4.x）。NeoForge 20.4.x 有注册器 API（`RegisterPayloadHandlerEvent` → `IPayloadRegistrar`），
但早于 `StreamCodec`，所以共享 payload 还得再写一个形态。这三个版本的 Fabric 都已经覆盖。

### Forge 1.21.2

Forge 从未发布 1.21.2 版本。

### 26.x 的 Forge

这不在本模组的构建目标里；26.x 这一线走 NeoForge。

### 1.20.5：只有 Fabric

NeoForge 为那个版本发布的产物里没有 ModDevGradle 需要的 `moddev-config.json`，
而 Forge 根本没有 1.20.5 版本，所以这一版只有 Fabric 节点。

Fabric 与 NeoForge 合起来覆盖 1.20.6 及以上的所有版本，1.20.1–1.20.5 则只有 Fabric。

## 8. 新增一个 Minecraft 版本

1. 加 `versions/<mc>/gradle.properties`，照抄最接近的版本，改掉 `deps.minecraft`、`deps.java`、
   `deps.mixin-compat`、`deps.pack-format`、各加载器版本以及 `meta.minecraft-range`。
2. 把版本号加进 `settings.gradle.kts` 里对应的列表。
3. 跑 `./gradlew :common:<mc>:compileJava`，然后照着报错移植。只有当目标版本落在 Forge 当前可构建的
   区间（1.20.6–1.21.11，见第 7 节；1.21.2 跳过，Forge 从未发布该版本）内时，才把它加进 `forgeVersions`。
4. 把版本号补进 `README.md` / `README.en.md` 以及 `docs/store-descriptions/`。
5. `./gradlew chiseledBuild` 确认整个矩阵仍然能构建。

发布会自动带上新版本：游戏版本标签和加载器标签是从 node 读的，不是每次发版手写的。

## 9. 发布

`dayz-loader` 会为每个加载器 node 配置 `me.modmuss50.mod-publish-plugin`：

- Modrinth 项目 `8asZxzdc`，CurseForge 项目 `1596267`。
- Token 从环境变量读取：`MODRINTH_TOKEN`（回退 `MODRINTH_PAT`）与
  `CURSEFORGE_API_KEY`（回退 `CURSEFORGE_TOKEN`）。
- `publish.dry_run` 默认 `true`，所以误跑 `publishMods` 不会真的提交任何东西。
- `publishAll` 依赖每个 node 的 `publishMods`，并做了排序，让 CurseForge 一次只收到一个上传而不是一串。

**CurseForge 永远不会回链接。** 每个上传都进入人工审核；API 收下文件就返回。
所以 `publishCurseforge` 变绿只代表**已提交**。Modrinth 是立即返回的。

CurseForge 的项目**描述**无法从构建更新 —— 上传 token 是 legacy upload-only 的，
而负责改元数据的 Eternal API 会拒绝它。`docs/store-descriptions/curseforge.md` 是那个页面的
复制粘贴来源，需要手工保持同步。Modrinth 的描述**是**通过 API 推送的。

## 10. 坑

1. **`gradlew` 必须保持可执行**（权限 `100755`）。CI 里也会跑 `chmod +x ./gradlew`。
2. **Gradle wrapper 是 9.7.0。** Fabric Loom 1.18.1 声明的
   `org.gradle.plugin.api-version` 是 `9.7.0`；更老的 wrapper 会在解析时报 variant 匹配错误，
   而错误信息里完全不提 Gradle 版本。
3. **不要硬编码 `org.gradle.java.home`。** 它和机器绑定，会打挂 CI。启动 JDK 通过 `JAVA_HOME` 提供；
   各版本的工具链来自 `versions/<mc>/gradle.properties`，缺失时由 foojay resolver 自动下载。
4. **`prop()` 读两个来源。** 先 `findProperty`（所以 `-P` 和根 `gradle.properties` 优先），
   再读 `versions/<mc>/gradle.properties`。如果某个属性"消失了"，先确认 node 的文件名和
   `sc.current.project` 对得上。
5. **绝不要在 `//? if` 块里写注释** —— 见第 5 节。
6. **mixin 的目标被改名不会降级，而是直接崩。** 客户端 mixin 配置是 `required: true` 的。
   目标搬家时先 grep 调用点，选**所有路径都会经过**的那个方法，不要猜。
7. **MixinConfigs 的兼容级别是按版本变的。** 它由 `processResources` 从 `deps.mixin-compat`
   展开进 mixin 配置；在 Java 21 的 node 上硬编码 `JAVA_25` 会过不了 Mixin 自己的校验。
8. **加载器初始化之前不要碰 `Platform.HELPER`。** 入口点跑之前它是 `null`。
   请用空安全的 `Platform.isModLoaded(...)` / `Platform.isReady()`。
