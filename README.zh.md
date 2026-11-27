# DayZ Inventory

[English](README.md) · **中文** · [Français](README.fr.md) · [日本語](README.ja.md) · [한국어](README.ko.md)

[![Modrinth](https://img.shields.io/modrinth/v/dayz-inventory?label=Modrinth&logo=modrinth)](https://modrinth.com/mod/dayz-inventory)
[![CurseForge](https://img.shields.io/curseforge/v/1596267?label=CurseForge&logo=curseforge&color=F16436)](https://www.curseforge.com/minecraft/mc-mods/dayz-inventory)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Ko-fi](https://img.shields.io/badge/Ko--fi-Support%20me-ff5e5b?logo=kofi&logoColor=white)](https://ko-fi.com/suoim)

把 DayZ 物品栏的外观、手感和机制完整搬进 Minecraft：显示附近地面物品与容器的 **附近（Vicinity）**
网格、绑定当前快捷栏格子的 **手持（Hands）** 槽位、展示装备、状态效果与生命体征的 **幸存者（Survivor）**
面板，以及内置的 **2x2 合成格**。

**强烈推荐搭配 [DayZ Hotbar](https://modrinth.com/mod/dayz-hotbar) 使用** —— 两者成对设计，HUD
与物品栏界面风格统一。

## 支持的版本

| 加载器 | Minecraft |
| :--- | :--- |
| **Fabric** | 1.20.1 – 26.3 |
| **NeoForge** | 1.20.6 – 26.3 |
| **Forge** | 1.20.1、1.20.6 – 1.21.11 |

共 **23 个 Minecraft 版本、54 个文件**，全部出自同一套源码。每个文件名都标明了 Minecraft 版本和加载器；
逐版本的完整列表见[英文 README](README.md#full-version-table)。

![DayZ Inventory 界面](docs/screenshots/ui-example.png)

## 功能

- **附近网格**：半径 3 格（可配置）内的掉落物和所有带物品栏的方块（箱子、木桶、潜影盒等）显示在同一个网格里，
  最近的容器排在前面。点击容器即可在网格下方展开它的格子，可整理、可用 `x` 关闭。上锁的容器、被挡住的箱子和战利品表
  的规则与右键方块时完全一致。
- **手动拾取**：默认关闭走过自动拾取。把物品拖进格子、拖到幸存者面板上直接装备，或按住 Shift 点击收入背包。
- **状态效果**：幸存者面板上显示效果图标，悬停可看名称、等级和剩余时间，最后十秒会闪烁。
- **生命体征**：生命值、饥饿值和口渴值竖条。安装 **Tough As Nails**、**Thirst Was Taken** 或
  **Dehydration** 时显示口渴值。
- **整理**：合并不满的堆叠并排序主物品栏（快捷栏保持不动）；箱子等存储容器也有自己的整理按钮。
- **Shift 点击**：在主物品栏与快捷栏之间移动、放进已打开的容器，或直接穿戴。
- **兼容**：JEI / REI / EMI 的显示/隐藏按钮；Curios、Accessories、Trinkets 各有按钮；支持 Mouse Tweaks。
  以上全部为可选，没有安装也能完整使用。

## 安装

1. 在[完整版本表](README.md#full-version-table)中找到你的 Minecraft 版本和加载器。
2. 从 [Modrinth](https://modrinth.com/mod/dayz-inventory/versions) 或
   [CurseForge](https://www.curseforge.com/minecraft/mc-mods/dayz-inventory/files) 下载文件名与 **两者**
   都匹配的文件。
3. 放进 `mods/` 文件夹。Fabric 还需要安装 [Fabric API](https://modrinth.com/mod/fabric-api)。

**服务端和每个客户端都需要安装**：物品栏界面、拾取和打开容器都有一部分在服务端运行。

> Fabric 版不能在 Forge / NeoForge 上加载，NeoForge 版不能在 Forge 上加载，不同 Minecraft 版本的文件也不能混用。

## 配置

首次启动时会生成 `config/dayz_inventory.properties`，修改后重启生效。

| 选项 | 默认值 | 说明 |
| :--- | :--- | :--- |
| `autoPickup` | `false` | 原版的走过自动拾取。创造模式玩家始终保留。 |
| `openContainersInDayZScreen` | `true` | 右键箱子或木桶时用 DayZ 界面打开。 |
| `vicinityRadius` | `3` | 附近面板的范围，1–8 格。服务器与客户端请设为相同的值。 |
| `replaceInventoryScreen` | `true` | 物品栏按键打开 DayZ 界面。 |
| `showStatusEffects` | `true` | 在幸存者面板显示状态效果。 |
| `showVitals` | `true` | 在幸存者面板显示生命值、饥饿值和口渴值。 |
| `dragAndDrop` | `true` | 按住物品拖到另一个格子松开即可移动。安装 Mouse Tweaks 时自动关闭。 |

## 从源码构建

需要 **JDK 25**：`./gradlew chiseledBuild` 构建全部版本，`./gradlew buildMods -Pdayz.versions=1.21.1`
只构建一个版本。构建说明见 **[docs/BUILDING.md](docs/BUILDING.md)**（英文）。

## 许可证

Apache License 2.0 —— 见 [LICENSE](LICENSE)。
