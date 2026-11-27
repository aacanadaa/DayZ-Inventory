# DayZ Inventory

[English](README.md) · [中文](README.zh.md) · [Français](README.fr.md) · **日本語** · [한국어](README.ko.md)

[![Modrinth](https://img.shields.io/modrinth/v/dayz-inventory?label=Modrinth&logo=modrinth)](https://modrinth.com/mod/dayz-inventory)
[![CurseForge](https://img.shields.io/curseforge/v/1596267?label=CurseForge&logo=curseforge&color=F16436)](https://www.curseforge.com/minecraft/mc-mods/dayz-inventory)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Ko-fi](https://img.shields.io/badge/Ko--fi-Support%20me-ff5e5b?logo=kofi&logoColor=white)](https://ko-fi.com/suoim)

DayZ のインベントリの見た目・操作感・仕組みを Minecraft に持ち込む、インベントリ UI の全面改修です。
近くの落ちているアイテムとコンテナを並べる **周辺（Vicinity）** グリッド、選択中のホットバー枠に連動する
**手持ち（Hands）** スロット、装備・ステータス効果・体調を表示する **サバイバー（Survivor）** パネル、
そして組み込みの **2x2 クラフトグリッド** を備えています。

**[DayZ Hotbar](https://modrinth.com/mod/dayz-hotbar) との併用を強くおすすめします。** 2 つはセットで
作られており、HUD とインベントリ画面の見た目が揃います。

## 対応バージョン

| ローダー | Minecraft |
| :--- | :--- |
| **Fabric** | 1.20.1 – 26.3 |
| **NeoForge** | 1.20.6 – 26.3 |
| **Forge** | 1.20.1、1.20.6 – 1.21.11 |

**23 の Minecraft バージョン、54 ファイル**をひとつのソースから作っています。ファイル名には Minecraft
バージョンとローダーが入っています。バージョンごとの一覧は[英語版 README の表](README.md#full-version-table)
を参照してください。

![DayZ Inventory の画面](docs/screenshots/ui-example.png)

## 機能

- **周辺グリッド**：半径 3 ブロック（変更可）以内の落ちているアイテムと、インベントリを持つすべてのブロック
  （チェスト、樽、シュルカーボックスなど）をひとつのグリッドに表示し、近いコンテナから並べます。
  コンテナをクリックするとグリッドの下にスロットが開き、整理も `x` で閉じることもできます。
  鍵・ふさがれたチェスト・ルートテーブルはブロックを右クリックしたときと同じように扱われます。
- **手で拾う**：歩いて拾う動作はオフです。アイテムをスロットへドラッグ、サバイバーパネルへドロップして装備、
  または Shift+クリックで取り込みます。
- **ステータス効果**：サバイバーパネルにアイコンで表示。カーソルを合わせると名前・レベル・残り時間が出て、
  残り 10 秒で点滅します。
- **体調**：体力・満腹度・水分のバー。水分は **Tough As Nails**、**Thirst Was Taken**、
  **Dehydration** のいずれかがあると表示されます。
- **整理**：半端なスタックをまとめてメインインベントリを並べ替えます（ホットバーはそのまま）。
  ストレージ系コンテナにも整理ボタンがあります。
- **Shift+クリック**：メインインベントリとホットバーの間、開いているコンテナ、または装備欄へ移動します。
- **連携**：JEI / REI / EMI の表示切り替えボタン、Curios・Accessories・Trinkets の各ボタン、
  Mouse Tweaks 対応。すべて任意で、なくても完全に動作します。

## インストール

1. [対応表](README.md#full-version-table)で自分の Minecraft バージョンとローダーを確認します。
2. [Modrinth](https://modrinth.com/mod/dayz-inventory/versions) または
   [CurseForge](https://www.curseforge.com/minecraft/mc-mods/dayz-inventory/files) から、**両方**に一致する
   ファイルをダウンロードします。
3. `mods/` に入れます。Fabric では [Fabric API](https://modrinth.com/mod/fabric-api) も必要です。

**サーバーと各クライアントの両方に**入れてください。画面・拾得・コンテナの開閉は一部サーバー側で動きます。

> Fabric 版は Forge / NeoForge では読み込めず、NeoForge 版は Forge では読み込めません。
> 異なる Minecraft バージョンのファイルも互換性はありません。

## 設定

初回起動時に `config/dayz_inventory.properties` が作られます。変更は次回起動時に反映されます。

| 項目 | 既定値 | 説明 |
| :--- | :--- | :--- |
| `autoPickup` | `false` | バニラと同じく歩いて拾う。クリエイティブでは常に有効。 |
| `openContainersInDayZScreen` | `true` | チェストや樽を右クリックすると DayZ 画面で開く。 |
| `vicinityRadius` | `3` | 周辺パネルの範囲（1–8 ブロック）。サーバーとクライアントで同じ値にしてください。 |
| `replaceInventoryScreen` | `true` | インベントリキーで DayZ 画面を開く。 |
| `showStatusEffects` | `true` | サバイバーパネルにステータス効果を表示。 |
| `showVitals` | `true` | 体力・満腹度・水分のバーを表示。 |
| `dragAndDrop` | `true` | アイテムを押して別のスロットで離すと移動。Mouse Tweaks があると自動でオフ。 |

## ソースからのビルド

**JDK 25** が必要です。`./gradlew chiseledBuild` で全バージョン、
`./gradlew buildMods -Pdayz.versions=1.21.1` で 1 バージョンだけをビルドします。
ビルドの仕組みは **[docs/BUILDING.md](docs/BUILDING.md)**（英語）にまとめています。

## ライセンス

Apache License 2.0 — [LICENSE](LICENSE) を参照してください。
