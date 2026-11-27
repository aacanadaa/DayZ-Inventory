# DayZ Inventory

[English](README.md) · **中文** · [Français](README.fr.md) · [日本語](README.ja.md) · [한국어](README.ko.md)

[![Modrinth](https://img.shields.io/modrinth/v/dayz-inventory?label=Modrinth&logo=modrinth)](https://modrinth.com/mod/dayz-inventory)
[![CurseForge](https://img.shields.io/curseforge/v/1596267?label=CurseForge&logo=curseforge&color=F16436)](https://www.curseforge.com/minecraft/mc-mods/dayz-inventory)
[![Minecraft](https://img.shields.io/modrinth/game-versions/dayz-inventory?label=Minecraft)](https://modrinth.com/mod/dayz-inventory/versions)
[![Modrinth Downloads](https://img.shields.io/modrinth/dt/dayz-inventory?label=Downloads&logo=modrinth)](https://modrinth.com/mod/dayz-inventory)
[![CurseForge Downloads](https://img.shields.io/curseforge/dt/1596267?label=Downloads&logo=curseforge&color=F16436)](https://www.curseforge.com/minecraft/mc-mods/dayz-inventory)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Issues](https://img.shields.io/github/issues/aacanadaa/DayZ-Inventory?color=red)](https://github.com/aacanadaa/DayZ-Inventory/issues)
[![Ko-fi](https://img.shields.io/badge/Ko--fi-Support%20me-ff5e5b?logo=kofi&logoColor=white)](https://ko-fi.com/suoim)

将 Minecraft 的背包界面替换为 DayZ 风格布局：包含周围地面物品与容器的 **附近（Vicinity）** 网格、展示装备/效果/体征的 **幸存者（Survivor）** 面板、**2x 手持（Hands）** 槽位以及 **2x2 合成格**。

可搭配同款画风的 HUD 模组 [DayZ Hotbar](https://modrinth.com/mod/dayz-hotbar) 一起使用。

![DayZ Inventory 界面截图](docs/screenshots/ui-example.png)

---

## 支持的版本

| 加载器 | Minecraft |
| :--- | :--- |
| **Fabric** | 1.20.1 – 26.3 |
| **NeoForge** | 1.20.6 – 26.3 |
| **Forge** | 1.20.1、1.20.6 – 1.21.11 |

共 23 个 Minecraft 版本、54 个构建文件，请下载同时匹配游戏版本和加载器的 jar。

---

## 功能

### 附近（Vicinity）
- **地面物品与容器：** 显示 3 格（可配置）内的掉落物与箱子、木桶、潜影盒等容器，最近的容器排在前面。
- **手动拾取：** 生存模式下关闭踩踏自动拾取，通过拖拽到背包/幸存者面板或 Shift 点击拾取。
- **内联抽屉：** 点击容器直接在附近网格下方展开其格子，支持整理并点 `x` 关闭；容器锁定、方块遮挡与战利品表均与原版右键打开一致。

### 幸存者与背包
- **装备、效果与体征：** 护甲与副手槽、药水效果图标（悬停查看名称与剩余时间，最后 10 秒闪烁），以及生命、饥饿和口渴条（安装 **Tough As Nails**、**Thirst Was Taken** 或 **Dehydration** 时显示）。
- **手持与合成：** 2 倍大小的手持预览（绑定当前快捷栏），外加 2x2 合成格。
- **整理与 Shift 点击：** 一键整理并合并零散堆叠（保留快捷栏不动），支持整理打开的容器。

### 模组兼容（均为可选）
- **JEI / REI / EMI：** 顶部按钮切换物品列表显示/隐藏。
- **Curios / Accessories / Trinkets：** 幸存者面板上的快捷按钮。
- **Mouse Tweaks：** 在 DayZ 界面内正常支持拖拽与滚轮移动物品。

---

## 安装

1. 从 [Modrinth](https://modrinth.com/mod/dayz-inventory/versions) 或 [CurseForge](https://www.curseforge.com/minecraft/mc-mods/dayz-inventory/files) 下载对应你游戏版本和加载器的 jar。
2. 同时放入**客户端和服务端**的 `mods/` 文件夹。
3. **Fabric** 端还需安装 [Fabric API](https://modrinth.com/mod/fabric-api)（Fabric Loader 0.15+）。

---

## 配置文件

首次启动会生成 `config/dayz_inventory.properties`（重启后生效）：

| 选项 | 默认值 | 说明 |
| :--- | :--- | :--- |
| `autoPickup` | `false` | 像原版一样踩踏自动拾取（创造模式始终开启） |
| `openContainersInDayZScreen` | `true` | 右键箱子或木桶时用 DayZ 界面打开 |
| `vicinityRadius` | `3` | 附近扫描半径（1–8 格，服务端与客户端需一致） |
| `replaceInventoryScreen` | `true` | 按背包键打开 DayZ 界面 |
| `showStatusEffects` | `true` | 在幸存者面板显示药水效果 |
| `showVitals` | `true` | 在幸存者面板显示生命/饥饿/口渴条 |
| `dragAndDrop` | `true` | 点击并松开移动物品（安装 Mouse Tweaks 时自动关闭） |

在独立服务器上，`autoPickup`、`openContainersInDayZScreen` 和 `vicinityRadius` 以服务端配置为准。

---

## 从源码构建

需要 **JDK 25** 启动 Gradle（Java 17/21 工具链会自动下载），详见 [docs/BUILDING.md](docs/BUILDING.md)：

```bash
./gradlew chiseledBuild                      # 构建全部版本与加载器
./gradlew buildMods -Pdayz.versions=1.21.1   # 构建单个 MC 版本
./gradlew :forge:1.20.1:build                # 构建单个目标
```

## 开源协议

Apache License 2.0 — 见 [LICENSE](LICENSE)。
