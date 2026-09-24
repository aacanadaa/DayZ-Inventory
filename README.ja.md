# DayZ Inventory

[English](README.md) · [中文](README.zh.md) · [Français](README.fr.md) · **日本語** · [한국어](README.ko.md)

[![Modrinth](https://img.shields.io/modrinth/v/dayz-inventory?label=Modrinth&logo=modrinth)](https://modrinth.com/mod/dayz-inventory)
[![CurseForge](https://img.shields.io/curseforge/v/1596267?label=CurseForge&logo=curseforge&color=F16436)](https://www.curseforge.com/minecraft/mc-mods/dayz-inventory)
[![Minecraft](https://img.shields.io/modrinth/game-versions/dayz-inventory?label=Minecraft)](https://modrinth.com/mod/dayz-inventory/versions)
[![Downloads](https://img.shields.io/modrinth/dt/dayz-inventory?label=Downloads&logo=modrinth)](https://modrinth.com/mod/dayz-inventory)
[![CurseForge Downloads](https://img.shields.io/curseforge/dt/1596267?label=Downloads&logo=curseforge&color=F16436)](https://www.curseforge.com/minecraft/mc-mods/dayz-inventory)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Ko-fi](https://img.shields.io/badge/Ko--fi-Support%20me-ff5e5b?logo=kofi&logoColor=white)](https://ko-fi.com/suoim)

Minecraft のインベントリ UI を完全に作り替えて、DayZ のインベントリシステムの見た目・操作感・
メカニクスを Minecraft に持ち込む：近くの地面のアイテムとコンテナを並べる **Vicinity** グリッド、
アクティブなホットバー スロットに連動する動的な **Hands** アタッチメント スロット、組み込みの
**2x2 クラフトング グリッド**、そして **Survivor** パネルへのドラッグでの装備。

**[DayZ Hotbar](https://modrinth.com/mod/dayz-hotbar) との併用を強く推奨** — シームレスに統合
されます。2 つはペアとして作られているため、HUD とインベントリ画面は一つの見た目を共有します。

## 対応バージョン

| ローダー | Minecraft |
| :--- | :--- |
| **Fabric** | 1.20.1 – 26.3 |
| **NeoForge** | 1.20.6 – 26.3 |
| **Forge** | 1.20.6 – 1.21.11 |

**23 の Minecraft バージョン、53 のダウンロード**、すべて単一のソースツリーから。各ファイル名には
対応する Minecraft バージョンとローダーが明記されています。下の[完全なバージョン
マトリクス](#完全なバージョンマトリクス)に全バージョンを、[依存関係](#依存関係)に必要なローダー
バージョンを記載しています。

![DayZ Inventory 画面：近くのアイテムを並べた VICINITY グリッド、シェル入りで開いた CHEST ドロワー、
装備したプレイヤーが写る SURVIVOR パネル、ヘッダーの CURIOS と JEI ボタン、2.0x の HANDS スロット
に入った M1014 Battle Shotgun、そして 2x2 の CRAFTING グリッド](docs/screenshots/ui-example.png)

---

## 機能

### 統合 Vicinity グリッドと展開可能なドロワー
- **近接スキャナー**：10 ティックごとに、半径 3 ブロック以内の地面のアイテムとコンテナブロック
  （チェスト、樽、シャルカー ボックス）を再スキャンします。
- **単一カラム**：近くの地面のアイテムと収納ブロックを、VICINITY ヘッダーの下の一つのスクロール
  可能なグリッドにまとめます。
- **コンテナ セレクター**：コンテナは座標と距離のツールチップ付きのスロット アイコンとして描画
  されます。
- **インライン ドロワー グリッド**：コンテナ セレクターをクリックすると、グリッドの下にスロットが
  インラインで展開されます。

### 動的 Hands アタッチメント スロット
- **アクティブ ホットバー連動**：現在選択中のホットバー スロットをミラーします。
- **大型アタッチメント スロット**：デフォルト スロットの代わりに、完全半透明のパネル本体。
- **2 倍スケール描画**：手持ちアイテムを **2.0x**（32x32 px）でパネル中央に描画します。
- **大文字名バナー**：ヘッダーの下にアイテム名（例：`HUNTING KNIFE`）を表示します。

### クラフトと装備の交換
- **バニラ 2x2 グリッド**：クラフトング グリッドと結果スロットはカスタム画面内にあり、結果は
  サーバー側で再計算されます。
- **ドラッグで装備**：中央の Survivor パネルに防具や衣服をドラッグすると、装備または入れ替えが
  行われます。

### オプションのモッド連携
- **レシピ ビューア（JEI / REI / EMI）**：テーマに合ったトグル ボタンをヘッダーに表示します。
  対応ビューアがインストールされている場合のみ描画されます。
- **Curios API**：Survivor ヘッダーに CURIOS ボタンを追加します。
- **Trinkets**：Survivor ヘッダーに TRINKETS ボタンを追加します。

すべての連携は実行時に `isModLoaded` で検出されます。どれも**ひとつも**インストールされていない
状態でも、モッドは起動し、画面を開き、完全に動作します。

---

## インストール

1. 上の表にあなたの Minecraft バージョンがあれば対応しています：**Fabric** は全 23 バージョン、
   **NeoForge** は 1.20.6 以降、**Forge** は 1.20.6 から 1.21.11 までカバーします。
2. Minecraft バージョン*と*ローダーの両方が一致するファイル名の jar を
   [Modrinth](https://modrinth.com/mod/dayz-inventory/versions) または
   [CurseForge](https://www.curseforge.com/minecraft/mc-mods/dayz-inventory/files) から
   ダウンロードします。
3. `mods/` フォルダに入れてください。

> **選ぶミスに注意。** Fabric ビルドは Forge や NeoForge ではロードされず、NeoForge ビルドは
> Forge ではロードされず、異なる Minecraft バージョン向けのビルドも互換性がありません。各
> ファイル名には Minecraft バージョンとローダーが入っています。

---

## 完全なバージョンマトリクス

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

合計 **23 の Minecraft バージョンと 53 の配布可能な jar**：Fabric 23、NeoForge 18、Forge 12 です。
Fabric と NeoForge は 1.20.6 以降のすべてのバージョンをカバーしています。マトリクスの穴は以下の
とおりのみです：

- **1.20.1–1.20.4** は Fabric のみ。1.20.1 は NeoForge より前に存在しました。NeoForge の 1.20.2
  リリースはまだ旧来の `SimpleChannel` ネットワークスタックを使用しています。1.20.3 には
  NeoForge リリースが一切ありません。1.20.4 の payload API は `StreamCodec` の存在より前に
  登場したため、独自の payload 型が必要になります。Forge 1.20.1 はさらに SRG 再オブフゾケーションと
  Searge mixin refmap を必要とし、現在の Forge ツールチェーンでは生成できません。
- **1.20.5** は Fabric のみ：NeoForge はビルドに必要なメタデータなしで当該リリースを公開し、
  Forge には 1.20.5 リリースがありません。
- **1.21.2** に Forge ビルドがないのは、Forge がそのリリースをスキップしたためです。
- **26.x に Forge ビルドはありません**：Forge の 26.x ラインはこのモッドがビルド対象としていない
  もので、そちらでは NeoForge がサポートされるルートです。

すべての成果物は**単一のソースツリー**からビルドされます：[Stonecutter](https://stonecutter.kikugie.dev/)
がマルチバージョンの前処理を、`common/` とローダーごとのモジュールがローダー抽象化を担当します。
マトリクスは `settings.gradle.kts` で宣言されており、バージョンを追加するにはそこに行を 1 行足して
`versions/<mc>/gradle.properties` ファイルを足すだけです。

---

## 依存関係

| Minecraft | ローダー | Java | 必須 | 任意 |
| :--- | :--- | :---: | :--- | :--- |
| **1.20.1** | Fabric | 17 | Fabric Loader `>=0.16.14`, Fabric API `0.92.12+1.20.1` | JEI / REI / EMI, Trinkets (Fabric), Curios |
| **1.20.2** | Fabric | 17 | Fabric Loader `>=0.16.14`, Fabric API `0.91.6+1.20.2` | JEI / REI / EMI, Trinkets (Fabric), Curios |
| **1.20.3** | Fabric | 17 | Fabric Loader `>=0.16.14`, Fabric API `0.91.1+1.20.3` | JEI / REI / EMI, Trinkets (Fabric), Curios |
| **1.20.4** | Fabric | 17 | Fabric Loader `>=0.16.14`, Fabric API `0.97.3+1.20.4` | JEI / REI / EMI, Trinkets (Fabric), Curios |
| **1.20.5** | Fabric | 21 | Fabric Loader `>=0.16.14`, Fabric API `0.97.8+1.20.5` | JEI / REI / EMI, Trinkets (Fabric), Curios |
| **1.20.6** | Fabric / NeoForge / Forge | 21 | Fabric Loader `>=0.16.14`, Fabric API `0.100.8+1.20.6`; NeoForge `20.6.141`; Forge `50.2.10` | JEI / REI / EMI, Trinkets (Fabric), Curios |
| **1.21** | Fabric / NeoForge / Forge | 21 | Fabric Loader `>=0.16.14`, Fabric API `0.102.0+1.21`; NeoForge `21.0.167`; Forge `51.0.33` | JEI / REI / EMI, Trinkets (Fabric), Curios |
| **1.21.1** | Fabric / NeoForge / Forge | 21 | Fabric Loader `>=0.16.14`, Fabric API `0.116.17+1.21.1`; NeoForge `21.1.250`; Forge `52.1.12` | JEI / REI / EMI, Trinkets (Fabric), Curios |
| **1.21.2** | Fabric / NeoForge | 21 | Fabric Loader `>=0.16.14`, Fabric API `0.106.1+1.21.2`; NeoForge `21.2.1-beta` | JEI / REI / EMI, Trinkets (Fabric), Curios |
| **1.21.3** | Fabric / NeoForge / Forge | 21 | Fabric Loader `>=0.16.14`, Fabric API `0.114.1+1.21.3`; NeoForge `21.3.97`; Forge `53.1.12` | JEI / REI / EMI, Trinkets (Fabric), Curios |
| **1.21.4** | Fabric / NeoForge / Forge | 21 | Fabric Loader `>=0.16.14`, Fabric API `0.119.4+1.21.4`; NeoForge `21.4.157`; Forge `54.1.18` | JEI / REI / EMI, Trinkets (Fabric), Curios |
| **1.21.5** | Fabric / NeoForge / Forge | 21 | Fabric Loader `>=0.19.5`, Fabric API `0.128.2+1.21.5`; NeoForge `21.5.98`; Forge `55.1.13` | JEI / REI / EMI, Trinkets (Fabric), Curios |
| **1.21.6** | Fabric / NeoForge / Forge | 21 | Fabric Loader `>=0.19.5`, Fabric API `0.128.2+1.21.6`; NeoForge `21.6.20-beta`; Forge `56.0.9` | JEI / REI / EMI, Trinkets (Fabric), Curios |
| **1.21.7** | Fabric / NeoForge / Forge | 21 | Fabric Loader `>=0.19.5`, Fabric API `0.129.0+1.21.7`; NeoForge `21.7.25-beta`; Forge `57.0.3` | JEI / REI / EMI, Trinkets (Fabric), Curios |
| **1.21.8** | Fabric / NeoForge / Forge | 21 | Fabric Loader `>=0.19.5`, Fabric API `0.136.1+1.21.8`; NeoForge `21.8.54`; Forge `58.1.22` | JEI / REI / EMI, Trinkets (Fabric), Curios |
| **1.21.9** | Fabric / NeoForge / Forge | 21 | Fabric Loader `>=0.19.5`, Fabric API `0.134.1+1.21.9`; NeoForge `21.9.16-beta`; Forge `59.0.5` | JEI / REI / EMI, Trinkets (Fabric), Curios |
| **1.21.10** | Fabric / NeoForge / Forge | 21 | Fabric Loader `>=0.19.5`, Fabric API `0.138.4+1.21.10`; NeoForge `21.10.64`; Forge `60.1.15` | JEI / REI / EMI, Trinkets (Fabric), Curios |
| **1.21.11** | Fabric / NeoForge / Forge | 21 | Fabric Loader `>=0.19.5`, Fabric API `0.141.6+1.21.11`; NeoForge `21.11.45`; Forge `61.2.1` | JEI / REI / EMI, Trinkets (Fabric), Curios |
| **26.1** | Fabric / NeoForge | 25 | Fabric Loader `>=0.19.5`, Fabric API `0.145.1+26.1`; NeoForge `26.1.0.19-beta` | JEI / REI / EMI, Trinkets (Fabric), Curios |
| **26.1.1** | Fabric / NeoForge | 25 | Fabric Loader `>=0.19.5`, Fabric API `0.145.4+26.1.1`; NeoForge `26.1.1.15-beta` | JEI / REI / EMI, Trinkets (Fabric), Curios |
| **26.1.2** | Fabric / NeoForge | 25 | Fabric Loader `>=0.19.5`, Fabric API `0.155.3+26.1.2`; NeoForge `26.1.2.109` | JEI / REI / EMI, Trinkets (Fabric), Curios |
| **26.2** | Fabric / NeoForge | 25 | Fabric Loader `>=0.19.5`, Fabric API `0.160.0+26.2`; NeoForge `26.2.0.88` | JEI / REI / EMI, Trinkets (Fabric), Curios |
| **26.3** | Fabric / NeoForge | 25 | Fabric Loader `>=0.19.5`, Fabric API `0.160.6+26.3`; NeoForge `26.3.0.1-beta` | JEI / REI / EMI, Trinkets (Fabric), Curios |

任意の項目は実行時に検出されます — このモッドがそれらを必須にすることはなく、欠けていてもクラッシュ
しません。Trinkets は Fabric 専用で、NeoForge と Forge ではその役割を Curios が担います。

---

## ソースからビルド

Gradle ランチャーには **JDK 25** が必要です。マトリクスの残りが要する Java 17 / 21 ツールチェーンは
foojay リゾルバーが自動的にダウンロードするため、手動でインストールする必要はありません。

```bash
# Builds every Minecraft version x every mod loader in the matrix.
./gradlew chiseledBuild

# A single target (artifacts land in <loader>/versions/<mc>/build/libs/).
./gradlew :fabric:26.2:build
./gradlew :neoforge:1.21.11:build
./gradlew :forge:1.21.11:build

# List every node in the matrix.
./gradlew matrix
```

成果物の名前は `dayz-inventory-<loader>-<minecraft>-<mod version>.jar` で、例えば
`dayz-inventory-fabric-26.2-1.8.0+mc26.2.jar` です。Minecraft バージョンをファイル名に入れるのは
意図的です：同じモッド バージョンが複数のゲーム バージョン向けにリリースされ、CurseForge は同じ
プロジェクト内で表示名が重複する 2 つ目のファイルを拒否するためです。

アーキテクチャ、条件付きコンパイルの慣習、および「バージョンの追加方法」は
**[docs/BUILDING.en.md](docs/BUILDING.en.md)** にドキュメントされています。

### CurseForge と Modrinth への公開

```bash
MODRINTH_TOKEN=... CURSEFORGE_API_KEY=... \
  ./gradlew publishAll -Ppublish.dry_run=false
```

- 各ノードには `versions/<mc>/gradle.properties` に由来する**そのノード自身の**ゲーム バージョンと
  ローダーのタグが付くため、誤った Minecraft バージョンで jar がアップロードされることはありません。
- `publish.dry_run` のデフォルトは `true` です。明示的にオフにしない限り、`publishMods` はログを
  出すだけです。
- **CurseForge の公開は「投げっぱなし」です。** 各ファイルは人間のレビューを通じ、API は URL を
  返さずに受け付けます。そのため、緑になった CurseForge タスクは「公開済み」ではなく「提出済み」を
  意味します。
- 一度に 1 プラットフォーム：`:fabric:26.2:publishModrinth` / `:fabric:26.2:publishCurseforge`。

CI（`.github/workflows/build.yml`）は `v*` タグでマトリクス全体をビルドし、すべての jar を
GitHub Release に添付し、両方のプラットフォームに公開します。

## バージョニング

`gradle.properties` の `mod.version` が唯一の真実の源です。`fabric.mod.json`、`neoforge.mods.toml`、
`mods.toml`、`pack.mcmeta` に展開されます。更新したら [CHANGELOG.md](CHANGELOG.md) を更新して
ください。

## ライセンス

Apache License 2.0 — [LICENSE](LICENSE) を参照してください。
