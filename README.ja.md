# DayZ Inventory

[English](README.md) · [中文](README.zh.md) · [Français](README.fr.md) · **日本語** · [한국어](README.ko.md)

[![Modrinth](https://img.shields.io/modrinth/v/dayz-inventory?label=Modrinth&logo=modrinth)](https://modrinth.com/mod/dayz-inventory)
[![CurseForge](https://img.shields.io/curseforge/v/1596267?label=CurseForge&logo=curseforge&color=F16436)](https://www.curseforge.com/minecraft/mc-mods/dayz-inventory)
[![Minecraft](https://img.shields.io/modrinth/game-versions/dayz-inventory?label=Minecraft)](https://modrinth.com/mod/dayz-inventory/versions)
[![Downloads](https://img.shields.io/modrinth/dt/dayz-inventory?label=Downloads&logo=modrinth)](https://modrinth.com/mod/dayz-inventory)
[![CurseForge Downloads](https://img.shields.io/curseforge/dt/1596267?label=Downloads&logo=curseforge&color=F16436)](https://www.curseforge.com/minecraft/mc-mods/dayz-inventory)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Ko-fi](https://img.shields.io/badge/Ko--fi-Support%20me-ff5e5b?logo=kofi&logoColor=white)](https://ko-fi.com/suoim)

Minecraft のインベントリ UI を徹底的に刷新し、DayZ インベントリシステムの見た目・操作感・仕組みを
Minecraft に持ち込みます：近くの地面のアイテムとコンテナを並べる **Vicinity（周囲）** グリッド、
アクティブなホットバー スロットに連動する **Hands（手）** アタッチメント スロット、
統合された **2x2 クラフト グリッド**、そして **Survivor（サバイバー）** パネルへドラッグして
装備を着脱する操作。

**[DayZ Hotbar](https://modrinth.com/mod/dayz-hotbar) との併用を強く推めます** —
シームレスに統合されます。2 つはペアとして作られているため、HUD とインベントリ画面は同じ見た目を共有します。

## 対応バージョン

| ローダー | Minecraft |
| :--- | :--- |
| **Fabric** | 1.20.1 – 26.3 |
| **NeoForge** | 1.20.6 – 26.3 |
| **Forge** | 1.20.6 – 1.21.11 |

**Minecraft バージョン 23 種、ダウンロード 53 件**、すべて単一のソースツリーから生成されます。
各ファイルには Minecraft バージョンとローダーがラベル付けされています。
全バージョンの一覧は下の[完全なバージョン対応表](#完全なバージョン対応表)に、
ローダーの必要バージョンは[依存関係](#依存関係)にあります。

![DayZ Inventory 画面：近くのアイテムを並べた VICINITY グリッド、弾薬が入って開いた CHEST ドロワー、装備したプレイヤーの SURVIVOR パネル、ヘッダーの CURIOS と JEI ボタン、2.0x の HANDS スロットに入った M1014 Battle Shotgun、2x2 の CRAFTING グリッド](docs/screenshots/ui-example.png)

---

## 機能

### 統合 Vicinity グリッドと展開可能なドロワー
- **近接スキャナー**：10 ティックごとに、半径 3 ブロック以内の地面のアイテムとコンテナブロック（チェスト、樽、シャルカーボックス）を再スキャンします。
- **統合カラム**：近くの地面のアイテムと収納ブロックを、VICINITY 見出しの下にある同じスクロール可能なグリッドにまとめます。
- **コンテナセレクター**：コンテナをスロット アイコンとして描画し、ツールチップで座標と距離を表示します。
- **インライン ドロワー グリッド**：コンテナセレクターをクリックすると、そのスロットをグリッドの下にインラインで展開します。

### 動的 Hands アタッチメント スロット
- **アクティブ ホットバー連動**：現在選択中のホットバー スロットをミラーします。
- **大きなアタッチメント スロット**：デフォルト スロットの代わりに、完全な半透明のパネル本体を表示します。
- **2 倍スケール描画**：所持アイテムを **2.0x**（32x32 px）でパネル中央に描画します。
- **大文字アイテム名バナー**：見出しの下にアイテム名（例: `HUNTING KNIFE`）を表示します。

### クラフトと装備の交換
- **バニラ 2x2 グリッド**：クラフト グリッドと結果スロットはカスタム画面内にあり、結果はサーバー側で再計算されます。
- **ドラッグで装備**：中央の Survivor パネルに防具や衣服をドラッグすると、装備または交換されます。

### 任意のモッド連携
- **レシピビューア（JEI / REI / EMI）**：ヘッダーにテーマに合わせたトグル ボタンを表示し、対応するビューアがインストールされているときのみ描画します。
- **Curios API**：Survivor ヘッダーに CURIOS ボタンを追加します。
- **Trinkets**：Survivor ヘッダーに TRINKETS ボタンを追加します。

各連携は実行時に `isModLoaded` で検出されます。これらが**一つも**インストールされていなくても、
モッドは起動し、画面を開き、完全に動作します。

---

## インストール

1. 上の表に自分の Minecraft バージョンが載っていれば対応しています：**Fabric** は 23 バージョンすべて、**NeoForge** は 1.20.6 以降、**Forge** は 1.20.6 から 1.21.11 です。
2. Minecraft バージョンとローダーの両方が一致するファイル名の jar を [Modrinth](https://modrinth.com/mod/dayz-inventory/versions) または [CurseForge](https://www.curseforge.com/minecraft/mc-mods/dayz-inventory/files) からダウンロードします。
3. `mods/` に入れます。

> **注意して選んでください。** Fabric ビルドは Forge や NeoForge では読み込まれず、NeoForge ビルドは Forge では読み込まれず、異なる Minecraft バージョン用のビルドは互換性がありません。各ファイル名には Minecraft バージョンとローダーが含まれています。

---

## 完全なバージョン対応表

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

つまり **Minecraft バージョン 23 種・出荷可能な jar 53 個**：Fabric 23、NeoForge 18、Forge 12 です。
Fabric と NeoForge は 1.20.6 以降のすべてのバージョンをカバーします。対応表で欠けているのは次のとおりです：

- **1.20.1–1.20.4** は Fabric のみです。1.20.1 は NeoForge より前で；NeoForge の 1.20.2 リリースは旧来の `SimpleChannel` ネットワーク スタックをそのまま使用；1.20.3 には NeoForge リリースが存在せず；1.20.4 の payload API は `StreamCodec` より前に登場したため、専用の payload 型が必要でした。Forge 1.20.1 はさらに SRG reobfuscation と Searge mixin refmap が必要ですが、現行の Forge ツールチェーンでは生成できません。
- **1.20.5** は Fabric のみです：NeoForge はビルドに必要なメタデータなしでそのリリースを公開し、Forge には 1.20.5 リリースがありません。
- **1.21.2** に Forge ビルドはありません：Forge はそのリリースを飛ばしたためです。
- **26.x に Forge ビルドはありません**：Forge の 26.x 系はこのモッドのビルド対象ではなく、サポートされる経路は NeoForge です。

すべての成果物は**単一のソースツリー**からビルドされます：マルチバージョンの前処理は [Stonecutter](https://stonecutter.kikugie.dev/) が、ローダー抽象化は `common/` とローダー別モジュールが担います。対応表は `settings.gradle.kts` で宣言されており、バージョンの追加はそこに 1 行と `versions/<mc>/gradle.properties` ファイルを足すだけです。

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

任意の項目は実行時に検出されます — このモッドは決してそれらを必要とせず、無くてもクラッシュしません。
Trinkets は Fabric 専用で、NeoForge と Forge では Curios がその役割を担います。

---

## ソースからビルド

Gradle ランチャーには **JDK 25** が必要です。対応表の残りが必要とする Java 17 / 21 のツールチェーンは foojay リゾルバーが自動的にダウンロードするため、手動でのインストールは不要です。

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

成果物の名前は `dayz-inventory-<loader>-<minecraft>-<mod version>.jar` で、たとえば `dayz-inventory-fabric-26.2-1.8.0+mc26.2.jar` です。Minecraft バージョンをファイル名に含めるのは意図的です：同じモッド バージョンが複数のゲーム バージョンで配布され、CurseForge はプロジェクト内で表示名が衝突する 2 つ目のファイルを拒否するためです。

アーキテクチャ、条件付きコンパイルの規約、バージョンの追加方法は **[docs/BUILDING.en.md](docs/BUILDING.en.md)** に記載されています。

### CurseForge と Modrinth への公開

```bash
MODRINTH_TOKEN=... CURSEFORGE_API_KEY=... \
  ./gradlew publishAll -Ppublish.dry_run=false
```

- 各ノードには `versions/<mc>/gradle.properties` から取得した**自身の**ゲーム バージョンとローダーのタグが付くため、ある jar が誤った Minecraft バージョンでアップロードされることはありません。
- `publish.dry_run` はデフォルトで `true` です。明示的にオフにしない限り、`publishMods` はログを出力するだけです。
- **CurseForge の公開は「送信して終わり」です。** すべてのファイルは人間のレビューを通過し、API は URL を返さずに受け付けます。そのため、成功した CurseForge タスクは「公開済み」ではなく「送信済み」を意味します。
- 一度に 1 つのプラットフォーム：`:fabric:26.2:publishModrinth` / `:fabric:26.2:publishCurseforge`。

CI（`.github/workflows/build.yml`）は `v*` タグで対応表全体をビルドし、すべての jar を GitHub Release に添付し、両プラットフォームに公開します。

## バージョニング

`gradle.properties` の `mod.version` が唯一の情報源です。`fabric.mod.json`、`neoforge.mods.toml`、`mods.toml`、`pack.mcmeta` に展開されます。更新したら、その後に [CHANGELOG.md](CHANGELOG.md) を更新してください。

## ライセンス

Apache License 2.0 — [LICENSE](LICENSE) を参照。
