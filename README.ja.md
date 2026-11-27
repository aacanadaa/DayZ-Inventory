# DayZ Inventory

[English](README.md) · [中文](README.zh.md) · [Français](README.fr.md) · **日本語** · [한국어](README.ko.md)

[![Modrinth](https://img.shields.io/modrinth/v/dayz-inventory?label=Modrinth&logo=modrinth)](https://modrinth.com/mod/dayz-inventory)
[![CurseForge](https://img.shields.io/curseforge/v/1596267?label=CurseForge&logo=curseforge&color=F16436)](https://www.curseforge.com/minecraft/mc-mods/dayz-inventory)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1%20%E2%80%93%2026.3-62b47a.svg)](https://modrinth.com/mod/dayz-inventory/versions)
[![Modrinth Downloads](https://img.shields.io/modrinth/dt/dayz-inventory?label=Downloads&logo=modrinth)](https://modrinth.com/mod/dayz-inventory)
[![CurseForge Downloads](https://img.shields.io/curseforge/dt/1596267?label=Downloads&logo=curseforge&color=F16436)](https://www.curseforge.com/minecraft/mc-mods/dayz-inventory)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Issues](https://img.shields.io/github/issues/aacanadaa/DayZ-Inventory?color=red)](https://github.com/aacanadaa/DayZ-Inventory/issues)
[![Ko-fi](https://img.shields.io/badge/Ko--fi-Support%20me-ff5e5b?logo=kofi&logoColor=white)](https://ko-fi.com/suoim)

Minecraft のインベントリ画面を DayZ 風のレイアウトに置き換えます。周辺のドロップアイテムやコンテナを一覧できる **Vicinity** グリッド、装備・ステータス効果・バイタルを表示する **Survivor** パネル、**2x Hands** スロット、**2x2 クラフト** を備えています。

同じデザインの HUD Mod である [DayZ Hotbar](https://modrinth.com/mod/dayz-hotbar) と組み合わせると統一感が出ます。

![DayZ Inventory 画面](docs/screenshots/ui-example.png)

---

## 対応バージョン

| ローダー | Minecraft |
| :--- | :--- |
| **Fabric** | 1.20.1 – 26.3 |
| **NeoForge** | 1.20.6 – 26.3 |
| **Forge** | 1.20.1, 1.20.6 – 1.21.11 |

23 バージョン・54 ビルド。お使いの Minecraft バージョンとローダーの両方に合う jar を選んでください。

---

## 主な機能

### Vicinity（周辺）
- **ドロップアイテム＆コンテナ：** 周囲 3 ブロック（設定可）の落ちているアイテムやチェスト・樽・シュルカーボックスなどを表示します。
- **手動ピックアップ：** サバイバルでは歩くだけの自動回収がオフになり、ドラッグまたは Shift+クリックで拾います。
- **インラインドロワー：** コンテナをクリックするとグリッド下に中身が開き、整理して `x` で閉じられます。鍵付きや塞がったチェストもバニラ通りに動作します。

### Survivor ＆ インベントリ
- **装備・効果・バイタル：** 防具・オフハンドスロット、ポーション効果アイコン（ホバーで詳細、残り 10 秒で点滅）、体力・満腹度・水分バー（**Tough As Nails** / **Thirst Was Taken** / **Dehydration** 導入時）。
- **Hands ＆ クラフト：** 選択中ホットバーに連動する 2 倍表示の手持ちスロットと 2x2 クラフト。
- **ソート＆ Shift クリック：** ホットバーを残したままメインインベントリや開いたコンテナを整理・スタック結合します。

### 連携 Mod（すべて任意）
- **JEI / REI / EMI：** ヘッダーのボタンでレシピ一覧を表示/非表示。
- **Curios / Accessories / Trinkets：** Survivor パネルから直接開けるボタンを追加。
- **Mouse Tweaks：** ドラッグやホイール移動に対応。

---

## 導入方法

1. [Modrinth](https://modrinth.com/mod/dayz-inventory/versions) または [CurseForge](https://www.curseforge.com/minecraft/mc-mods/dayz-inventory/files) からバージョンとローダーに合う jar をダウンロードします。
2. **クライアントとサーバーの両方**の `mods/` に入れます。
3. **Fabric** では [Fabric API](https://modrinth.com/mod/fabric-api)（Fabric Loader 0.15+）も必要です。

---

## 設定

初回起動時に `config/dayz_inventory.properties` が作成されます（再起動で反映）：

| 項目 | 初期値 | 内容 |
| :--- | :--- | :--- |
| `autoPickup` | `false` | バニラ同様の自動回収（クリエイティブでは常に有効） |
| `openContainersInDayZScreen` | `true` | チェストや樽を右クリックしたとき DayZ 画面で開く |
| `vicinityRadius` | `3` | 周辺の探索半径（1–8 ブロック、サーバーと合わせる） |
| `replaceInventoryScreen` | `true` | インベントリキーで DayZ 画面を開く |
| `showStatusEffects` | `true` | Survivor パネルにステータス効果を表示 |
| `showVitals` | `true` | Survivor パネルに体力・満腹度・水分バーを表示 |
| `dragAndDrop` | `true` | クリック＆リリース移動（Mouse Tweaks 導入時は無効） |

---

## ビルド方法

Gradle 実行に **JDK 25** が必要です（Java 17/21 は自動取得）。詳細は [docs/BUILDING.md](docs/BUILDING.md) を参照してください：

```bash
./gradlew chiseledBuild                      # 全バージョン＆ローダーをビルド
./gradlew buildMods -Pdayz.versions=1.21.1   # 指定 MC バージョンをビルド
./gradlew :forge:1.20.1:build                # 単一ターゲットをビルド
```

## ライセンス

Apache License 2.0 — [LICENSE](LICENSE) を参照してください。
