# DayZ Inventory

[English](README.md) · **中文** · [Français](README.fr.md) · [日本語](README.ja.md) · [한국어](README.ko.md)

[![Modrinth](https://img.shields.io/modrinth/v/dayz-inventory?label=Modrinth&logo=modrinth)](https://modrinth.com/mod/dayz-inventory)
[![CurseForge](https://img.shields.io/curseforge/v/1596267?label=CurseForge&logo=curseforge&color=F16436)](https://www.curseforge.com/minecraft/mc-mods/dayz-inventory)
[![Minecraft](https://img.shields.io/modrinth/game-versions/dayz-inventory?label=Minecraft)](https://modrinth.com/mod/dayz-inventory/versions)
[![Downloads](https://img.shields.io/modrinth/dt/dayz-inventory?label=Downloads&logo=modrinth)](https://modrinth.com/mod/dayz-inventory)
[![CurseForge Downloads](https://img.shields.io/curseforge/dt/1596267?label=Downloads&logo=curseforge&color=F16436)](https://www.curseforge.com/minecraft/mc-mods/dayz-inventory)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Ko-fi](https://img.shields.io/badge/Ko--fi-Support%20me-ff5e5b?logo=kofi&logoColor=white)](https://ko-fi.com/suoim)

把 Minecraft 的背包界面彻底重做成 **DayZ** 风格：一个 **Vicinity（周围环境）** 网格列出附近地面物品与容器，
一个跟随快捷栏变化的 **Hands（手持）** 挂载槽，内嵌 **2x2 合成台**，以及把装备拖到 **Survivor（幸存者）**
面板上直接穿脱的交互。

**强烈建议搭配 [DayZ Hotbar](https://modrinth.com/mod/dayz-hotbar) 一起使用** —— 两者是一对：
HUD 和背包界面共享同一套视觉语言。

## 支持的版本

| 加载器 | Minecraft |
| :--- | :--- |
| **Fabric** | 1.20.1 – 26.3 |
| **NeoForge** | 1.20.6 – 26.3 |
| **Forge** | 1.20.6 – 1.21.11 |

一共 **23 个 Minecraft 版本、53 个下载**，全部来自同一个源码树。每个文件名都标明了它对应的
Minecraft 版本和加载器。每个版本的完整列表见下方[完整版本矩阵](#完整版本矩阵)，加载器的最低版本要求见[依赖](#依赖)。

![DayZ Inventory 界面：VICINITY 网格、展开的 CHEST 抽屉、带装备的 SURVIVOR 面板、header 里的 CURIOS 与 JEI 按钮、2.0x 的 HANDS 槽以及 2x2 CRAFTING 合成格](docs/screenshots/ui-example.png)

---

## 功能

### 统一的 Vicinity 网格与可展开抽屉
- **近距扫描器**：每 10 tick 扫描一次玩家 3 格半径内的地面掉落物与容器方块（箱子、木桶、潜影盒）。
- **单一滚动列**：把附近掉落物与容器合并进 VICINITY 标题下的同一个可滚动网格。
- **容器选择器**：容器以槽位图标显示，悬停可看坐标与距离。
- **内联抽屉**：点击容器图标即可在列表内联展开它的槽位抽屉。

### 动态 Hands 挂载槽
- **跟随快捷栏**：始终镜像当前选中的快捷栏槽位。
- **加大的挂载槽**：半透明面板，把手里物品以 **2.0x**（32x32 像素）居中渲染。
- **大写物品名横幅**：在标题下方显示物品名（例如 `HUNTING KNIFE`）。

### 合成与装备交换
- **原版 2x2 合成**：合成格与结果槽直接内嵌在自定义界面里，结果由服务端重算。
- **拖拽穿戴**：把护甲/衣物拖到中间的 Survivor 面板即可自动穿上或替换。

### 可选模组联动
- **配方查看器（JEI / REI / EMI）**：header 上会出现风格一致的开关键。**完全可选** —— 没装就不画。
- **Curios API**：装了才显示 CURIOS 按钮。
- **Trinkets**：装了才显示 TRINKETS 按钮。

所有联动都在运行时用 `isModLoaded` 探测。模组在**一个都不装**的情况下也能正常启动、打开界面并完整工作。

---

## 安装

1. 只要你的 Minecraft 版本在上面表格里就能用：**Fabric** 覆盖全部 23 个版本，**NeoForge** 从
   1.20.6 起，**Forge** 覆盖 1.20.6 – 1.21.11。
2. 从 [Modrinth](https://modrinth.com/mod/dayz-inventory/versions) 或
   [CurseForge](https://www.curseforge.com/minecraft/mc-mods/dayz-inventory/files) 下载文件名里
   **Minecraft 版本和加载器都对得上**的那个 jar。
3. 丢进 `mods/` 文件夹。

> **别下错。** Fabric 的构建在 Forge / NeoForge 上不会加载，NeoForge 的构建也不会在 Forge 上加载，
> 不同 Minecraft 版本之间同样不能混用。每个文件名都标了 Minecraft 版本与加载器。

---

## 完整版本矩阵

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

一共 **23 个 Minecraft 版本、53 个可发布 jar**：Fabric 23 个、NeoForge 18 个、Forge 12 个。
Fabric 和 NeoForge 覆盖 1.20.6 及以后的每一个版本。矩阵里的空缺只有下面这几种情况：

- **1.20.1–1.20.4** 只有 Fabric。1.20.1 早于 NeoForge 出现；NeoForge 的 1.20.2 发布仍在使用旧的 `SimpleChannel` 网络栈；1.20.3 完全没有 NeoForge 发布；1.20.4 的 payload API 早于 `StreamCodec`，需要自带一个 payload 类型。Forge 1.20.1 还需要 SRG reobfuscation 和 Searge mixin refmap，当前 Forge 工具链做不到。
- **1.20.5** 只有 Fabric：NeoForge 的那次发布缺少构建所需的元数据，Forge 则没有 1.20.5 发布。
- **1.21.2** 没有 Forge 构建，因为 Forge 跳过了那个版本。
- **26.x 没有 Forge 构建**：Forge 的 26.x 线不是本模组构建的目标，那里 NeoForge 才是受支持的路线。

整份代码来自 **同一个源码树**：用 [Stonecutter](https://stonecutter.kikugie.dev/) 做多版本预处理，
用 `common/` + 各加载器模块做加载器抽象。目标矩阵在 `settings.gradle.kts` 里声明，
新增一个版本只需要加一行加一个 `versions/<mc>/gradle.properties`。

---

## 依赖

| Minecraft | 加载器 | Java | 必需版本 | 可选 |
| :--- | :--- | :---: | :--- | :--- |
| **1.20.1** | Fabric | 17 | Fabric Loader `>=0.16.14`，Fabric API `0.92.12+1.20.1` | JEI / REI / EMI、Trinkets（Fabric）、Curios |
| **1.20.2** | Fabric | 17 | Fabric Loader `>=0.16.14`，Fabric API `0.91.6+1.20.2` | JEI / REI / EMI、Trinkets（Fabric）、Curios |
| **1.20.3** | Fabric | 17 | Fabric Loader `>=0.16.14`，Fabric API `0.91.1+1.20.3` | JEI / REI / EMI、Trinkets（Fabric）、Curios |
| **1.20.4** | Fabric | 17 | Fabric Loader `>=0.16.14`，Fabric API `0.97.3+1.20.4` | JEI / REI / EMI、Trinkets（Fabric）、Curios |
| **1.20.5** | Fabric | 21 | Fabric Loader `>=0.16.14`，Fabric API `0.97.8+1.20.5` | JEI / REI / EMI、Trinkets（Fabric）、Curios |
| **1.20.6** | Fabric / NeoForge / Forge | 21 | Fabric Loader `>=0.16.14`，Fabric API `0.100.8+1.20.6`；NeoForge `20.6.141`；Forge `50.2.10` | JEI / REI / EMI、Trinkets（Fabric）、Curios |
| **1.21** | Fabric / NeoForge / Forge | 21 | Fabric Loader `>=0.16.14`，Fabric API `0.102.0+1.21`；NeoForge `21.0.167`；Forge `51.0.33` | JEI / REI / EMI、Trinkets（Fabric）、Curios |
| **1.21.1** | Fabric / NeoForge / Forge | 21 | Fabric Loader `>=0.16.14`，Fabric API `0.116.17+1.21.1`；NeoForge `21.1.250`；Forge `52.1.12` | JEI / REI / EMI、Trinkets（Fabric）、Curios |
| **1.21.2** | Fabric / NeoForge | 21 | Fabric Loader `>=0.16.14`，Fabric API `0.106.1+1.21.2`；NeoForge `21.2.1-beta` | JEI / REI / EMI、Trinkets（Fabric）、Curios |
| **1.21.3** | Fabric / NeoForge / Forge | 21 | Fabric Loader `>=0.16.14`，Fabric API `0.114.1+1.21.3`；NeoForge `21.3.97`；Forge `53.1.12` | JEI / REI / EMI、Trinkets（Fabric）、Curios |
| **1.21.4** | Fabric / NeoForge / Forge | 21 | Fabric Loader `>=0.16.14`，Fabric API `0.119.4+1.21.4`；NeoForge `21.4.157`；Forge `54.1.18` | JEI / REI / EMI、Trinkets（Fabric）、Curios |
| **1.21.5** | Fabric / NeoForge / Forge | 21 | Fabric Loader `>=0.19.5`，Fabric API `0.128.2+1.21.5`；NeoForge `21.5.98`；Forge `55.1.13` | JEI / REI / EMI、Trinkets（Fabric）、Curios |
| **1.21.6** | Fabric / NeoForge / Forge | 21 | Fabric Loader `>=0.19.5`，Fabric API `0.128.2+1.21.6`；NeoForge `21.6.20-beta`；Forge `56.0.9` | JEI / REI / EMI、Trinkets（Fabric）、Curios |
| **1.21.7** | Fabric / NeoForge / Forge | 21 | Fabric Loader `>=0.19.5`，Fabric API `0.129.0+1.21.7`；NeoForge `21.7.25-beta`；Forge `57.0.3` | JEI / REI / EMI、Trinkets（Fabric）、Curios |
| **1.21.8** | Fabric / NeoForge / Forge | 21 | Fabric Loader `>=0.19.5`，Fabric API `0.136.1+1.21.8`；NeoForge `21.8.54`；Forge `58.1.22` | JEI / REI / EMI、Trinkets（Fabric）、Curios |
| **1.21.9** | Fabric / NeoForge / Forge | 21 | Fabric Loader `>=0.19.5`，Fabric API `0.134.1+1.21.9`；NeoForge `21.9.16-beta`；Forge `59.0.5` | JEI / REI / EMI、Trinkets（Fabric）、Curios |
| **1.21.10** | Fabric / NeoForge / Forge | 21 | Fabric Loader `>=0.19.5`，Fabric API `0.138.4+1.21.10`；NeoForge `21.10.64`；Forge `60.1.15` | JEI / REI / EMI、Trinkets（Fabric）、Curios |
| **1.21.11** | Fabric / NeoForge / Forge | 21 | Fabric Loader `>=0.19.5`，Fabric API `0.141.6+1.21.11`；NeoForge `21.11.45`；Forge `61.2.1` | JEI / REI / EMI、Trinkets（Fabric）、Curios |
| **26.1** | Fabric / NeoForge | 25 | Fabric Loader `>=0.19.5`，Fabric API `0.145.1+26.1`；NeoForge `26.1.0.19-beta` | JEI / REI / EMI、Trinkets（Fabric）、Curios |
| **26.1.1** | Fabric / NeoForge | 25 | Fabric Loader `>=0.19.5`，Fabric API `0.145.4+26.1.1`；NeoForge `26.1.1.15-beta` | JEI / REI / EMI、Trinkets（Fabric）、Curios |
| **26.1.2** | Fabric / NeoForge | 25 | Fabric Loader `>=0.19.5`，Fabric API `0.155.3+26.1.2`；NeoForge `26.1.2.109` | JEI / REI / EMI、Trinkets（Fabric）、Curios |
| **26.2** | Fabric / NeoForge | 25 | Fabric Loader `>=0.19.5`，Fabric API `0.160.0+26.2`；NeoForge `26.2.0.88` | JEI / REI / EMI、Trinkets（Fabric）、Curios |
| **26.3** | Fabric / NeoForge | 25 | Fabric Loader `>=0.19.5`，Fabric API `0.160.6+26.3`；NeoForge `26.3.0.1-beta` | JEI / REI / EMI、Trinkets（Fabric）、Curios |

可选依赖全部在运行时探测，缺少时不会崩溃；Trinkets 只做 Fabric，NeoForge / Forge 上装的是 Curios。

---

## 从源码构建

需要 **JDK 25** 作为 Gradle 启动 JDK。矩阵里其它版本所需的 Java 17 / 21 工具链由 foojay
resolver 自动下载，不需要手动安装。

```bash
# 构建矩阵里的每一个版本 × 每一个加载器
./gradlew chiseledBuild

# 只构建某一个目标（产物在 <加载器>/versions/<mc>/build/libs/ 下）
./gradlew :fabric:26.2:build
./gradlew :neoforge:1.21.11:build
./gradlew :forge:1.21.11:build

# 看看当前矩阵里有哪些节点
./gradlew matrix
```

产物命名规则是 `dayz-inventory-<加载器>-<minecraft 版本>-<模组版本>.jar`，例如
`dayz-inventory-fabric-26.2-1.8.0+mc26.2.jar`。Minecraft 版本写进文件名是有意的：
同一个模组版本会为多个游戏版本发布，而 CurseForge 会拒绝同一项目下**显示名重复**的文件。

架构说明、条件编译约定，以及"怎么加一个新版本"，见 **[docs/BUILDING.md](docs/BUILDING.md)**。

### 发布到 CurseForge 与 Modrinth

```bash
MODRINTH_TOKEN=... CURSEFORGE_API_KEY=... \
  ./gradlew publishAll -Ppublish.dry_run=false
```

- 每个节点都会带上**自己的**游戏版本与加载器标签，标签值来自 `versions/<mc>/gradle.properties`，
  所以不可能把某个 jar 传到错误的 Minecraft 版本下。
- `publish.dry_run` 默认为 `true`。不显式关掉的话，`publishMods` 只打日志、不会真的上传。
- **CurseForge 的发布是"提交即止"**：每个文件都会进入人工审核，API 收下文件后不会返回链接。
  因此 CurseForge 任务"成功"只代表已提交，不代表已上线。
- 只发一个平台可以用 `:fabric:26.2:publishModrinth` / `:fabric:26.2:publishCurseforge`。

CI（`.github/workflows/build.yml`）在打 `v*` tag 时会构建整个矩阵、挂到 GitHub Release，
并自动发布到两个平台。

## 版本号

`gradle.properties` 里的 `mod.version` 是唯一真源，会被展开进 `fabric.mod.json`、`neoforge.mods.toml`、`mods.toml`
和 `pack.mcmeta`。改完记得更新 [CHANGELOG.md](CHANGELOG.md)。

## 许可证

Apache License 2.0，见 [LICENSE](LICENSE)。
