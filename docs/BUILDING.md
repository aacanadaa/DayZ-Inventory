# 构建 DayZ Inventory

**中文** | [English](BUILDING.en.md)

本文说明统一后的多版本 / 多加载器构建是怎么搭起来的、为什么长这样，以及怎么再加一个 Minecraft 版本。

---

## 1. 为什么从"一版一分支"改成单源码树

这个项目原来每个 Minecraft 版本一个分支（`main` 是 1.20.1，另有 `1.21.1`、`1.21.11`、`26.2`）。
每个 bug 修复都要往所有分支合一遍，每个分支还要单独构建、单独测试。

现在是一个分支、一份源码树。版本相关的代码用 `//? if <条件>` 注释内联标注，
构建则按 **node**（节点）产出产物 —— 一个 node 就是一对（模块, Minecraft 版本），例如 `:fabric:26.2`。

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
forge/                       旧版 Forge 模块 —— 存在但未构建（见第 7 节）
```

Stonecutter 以 Minecraft 版本给每个 node 命名，并把它们放在分支目录下，
所以 `:fabric:26.2` 的工程目录是 `fabric/versions/26.2/`。这些目录都是构建产物，
受版本控制的只有 `versions/<mc>/gradle.properties`。

## 3. Gradle 构建是怎么串起来的

### `settings.gradle.kts`

声明树与矩阵：

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
| `dayz-loader` | fabric / neoforge | 共享 `common` node 的源码，并配置 `publishMods` |

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

| Minecraft | Node | Java | Fabric Loom | 说明 |
| :--- | :--- | :--- | :--- | :--- |
| 26.2 | `:fabric:26.2`、`:neoforge:26.2` | 25 | `fabric-loom`（不混淆） | 无 mappings、无 refmap、无 remap 步骤 |
| 1.21.11 | `:fabric:1.21.11`、`:neoforge:1.21.11` | 21 | `fabric-loom-remap` | 最后一个混淆版本；输入事件已对象化 |
| 1.21.1 | `:fabric:1.21.1`、`:forge:1.21.1`、`:neoforge:1.21.1` | 21 | `fabric-loom-remap` | payload 网络层；鼠标坐标仍是裸参数 |

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

`loom-back-compat` 依据 `sc.current.parsed < "26"` 选择 Loom 变体，
并为不混淆版 Loom 删掉的 `mod*` 配置提供别名，因此一份构建脚本同时覆盖这条分界线两侧。

## 7. 尚未启用的目标

`1.20.1` 目前仍由自己的分支发布，**没有**因为本次重构而回退。
它的 `versions/1.20.1/gradle.properties` 已就位，在 `settings.gradle.kts` 的版本列表里加上 `"1.20.1"` 即可接上构建。

### 1.20.1（Fabric、Forge）

这个版本缺的是整个 1.20.5 网络层重写：

- 没有 `CustomPacketPayload` / `StreamCodec`。`DayZInventoryPayload` 与 `DayZInventoryOpenData`
  都不存在，通道就是一个 `ResourceLocation` 加一个 `FriendlyByteBuf`。
- `ExtendedScreenHandlerType` 不接受 opening-data codec；工厂实现的是
  `writeScreenOpeningData(ServerPlayer, FriendlyByteBuf)` 而不是 `getScreenOpeningData`。
- Fabric 侧是按通道逐个注册：`ServerPlayNetworking.registerGlobalReceiver(ResourceLocation, ...)`。
- `ResourceLocation` 的构造器还是公开的，所以没有 `ResourceLocation.fromNamespaceAndPath`。

`GuiGraphics` 时代也有差异：`render` 的签名是 `(GuiGraphics, int, int, float)`，没有 `renderContents`；
背包重定向 mixin 的目标是 `Minecraft#setScreen` 而不是 `Gui#setScreen`。

### Forge

**Forge 1.21.1 已经完成。** `forge/` 已从 1.20.1 时代的 `SimpleChannel` / `NetworkRegistry` /
`registerMessage` 迁到 `net.minecraftforge` 下的 payload API，`:forge:1.21.1` 与其它 node 一样
可以构建、打包和发布。它的 mixin 配置走的是 jar 清单里的 `MixinConfigs` 属性 ——
因为 Forge 不认识 NeoForge 在 `mods.toml` 里用的 `[[mixins]]` 块。

1.20.1 的 Forge 卡在上面那个 1.20.1 移植本身，而不是卡在 Forge 相关内容上。
Forge 在 1.20.x 之后就没有新版本了，所以 1.21.1 是它能支持的最高版本。

## 8. 新增一个 Minecraft 版本

1. 加 `versions/<mc>/gradle.properties`，照抄最接近的版本，改掉 `deps.minecraft`、`deps.java`、
   `deps.mixin-compat`、`deps.pack-format`、各加载器版本以及 `meta.minecraft-range`。
2. 把版本号加进 `settings.gradle.kts` 里对应的列表。
3. 跑 `./gradlew :common:<mc>:compileJava`，然后照着报错移植。如果目标版本是 1.20.1 或 1.21.1，
   记得也加进 `forgeVersions` —— Forge 没有更晚的版本。
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
