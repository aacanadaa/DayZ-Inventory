# DayZ Inventory

[English](README.md) · [中文](README.zh.md) · [Français](README.fr.md) · [日本語](README.ja.md) · **한국어**

[![Modrinth](https://img.shields.io/modrinth/v/dayz-inventory?label=Modrinth&logo=modrinth)](https://modrinth.com/mod/dayz-inventory)
[![CurseForge](https://img.shields.io/curseforge/v/1596267?label=CurseForge&logo=curseforge&color=F16436)](https://www.curseforge.com/minecraft/mc-mods/dayz-inventory)
[![Minecraft](https://img.shields.io/modrinth/game-versions/dayz-inventory?label=Minecraft)](https://modrinth.com/mod/dayz-inventory/versions)
[![Downloads](https://img.shields.io/modrinth/dt/dayz-inventory?label=Downloads&logo=modrinth)](https://modrinth.com/mod/dayz-inventory)
[![CurseForge Downloads](https://img.shields.io/curseforge/dt/1596267?label=Downloads&logo=curseforge&color=F16436)](https://www.curseforge.com/minecraft/mc-mods/dayz-inventory)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Ko-fi](https://img.shields.io/badge/Ko--fi-Support%20me-ff5e5b?logo=kofi&logoColor=white)](https://ko-fi.com/suoim)

Minecraft 인벤토리 UI를 완전히 다시 만들어 DayZ 인벤토리 시스템의 look, feel, 메커닉을 Minecraft로
옮겨 왔습니다: 근처 지면 아이템과 컨테이너를 나열하는 **Vicinity** 그리드, 활성 핫바 슬롯에 연결된
동적 **Hands** 부착 슬롯, 내장 **2x2 제작 그리드**, 그리고 **Survivor** 패널에 드래그로 장착하는
인터랙션입니다.

**[DayZ Hotbar](https://modrinth.com/mod/dayz-hotbar) 와의 병용을 강력 추천** — 원활하게 통합됩니다.
두 모드는 한 쌍으로 설계되어 HUD와 인벤토리 화면이 하나의 스타일을 공유합니다.

## 지원 버전

| 로더 | Minecraft |
| :--- | :--- |
| **Fabric** | 1.20.1 – 26.3 |
| **NeoForge** | 1.20.6 – 26.3 |
| **Forge** | 1.20.6 – 1.21.11 |

**23개의 Minecraft 버전, 53개의 다운로드**, 모두 단일 소스 트리에서 빌드됩니다. 각 파일명에는 해당
Minecraft 버전과 로더가 표시되어 있습니다. 아래의 [전체 버전
매트릭스](#전체-버전-매트릭스)에 모든 버전이, [의존성](#의존성)에 필요한 로더 버전이 나와 있습니다.

![DayZ Inventory 화면: 근처 아이템의 VICINITY 그리드, 산탄이 담긴 채 열린 CHEST 서랍, 장비를 착용한
플레이어가 보이는 SURVIVOR 패널, 헤더의 CURIOS 및 JEI 버튼, 2.0x HANDS 슬롯의 M1014 Battle Shotgun,
그리고 2x2 CRAFTING 그리드](docs/screenshots/ui-example.png)

---

## 기능

### 통합 Vicinity 그리드 및 확장 가능한 서랍
- **근접 스캐너**: 10틱마다 반경 3블록 이내의 지면 아이템과 컨테이너 블록(상자, 나무 양동이,
  샐커 상자)을 다시 스캔합니다.
- **통합 컬럼**: 근처의 지면 아이템과 저장 블록이 VICINITY 헤더 아래 하나의 스크롤 가능한 그리드에서
  공유됩니다.
- **컨테이너 선택기**: 컨테이너가 좌표와 거리 툴팁이 있는 슬롯 아이콘으로 그려집니다.
- **인라인 서랍 그리드**: 컨테이너 선택기를 클릭하면 그리드 아래에 슬롯이 인라인으로 펼쳐집니다.

### 동적 Hands 부착 슬롯
- **활성 핫바 연결**: 현재 선택된 핫바 슬롯을 미러링합니다.
- **대형 부착 슬롯**: 기본 슬롯을 대체하는 완전 반투명 패널 본체.
- **2배 스케일 렌더**: 손에 든 아이템을 패널 중앙에 **2.0x**(32x32 px) 크기로 그립니다.
- **대문자 이름 배너**: 헤더 아래에 아이템 이름(예: `HUNTING KNIFE`)을 표시합니다.

### 제작 및 장비 교체
- **기본 2x2 그리드**: 제작 그리드와 결과 슬롯이 커스텀 화면 안에 있으며, 결과는 서버 측에서
  다시 계산됩니다.
- **드래그 장착**: 중앙 Survivor 패널에 방어구나 의류를 드래그하여 장착하거나 교체합니다.

### 선택적 모드 연동
- **레시피 뷰어(JEI / REI / EMI)**: 지원되는 뷰어가 설치된 경우에만 그려지는, 테마에 맞는 토글
  버튼을 헤더에 표시합니다.
- **Curios API**: Survivor 헤더에 CURIOS 버튼을 추가합니다.
- **Trinkets**: Survivor 헤더에 TRINKETS 버튼을 추가합니다.

모든 연동은 실행 시 `isModLoaded`로 탐지됩니다. 어느 것도 설치하지 **않은** 상태에서도 모드는
시작하고, 화면을 열며, 완전히 작동합니다.

---

## 설치

1. 위 표에 당신의 Minecraft 버전이 있으면 커버됩니다: **Fabric**는 23개 버전 전부, **NeoForge**는
   1.20.6 이상, **Forge**는 1.20.6부터 1.21.11까지입니다.
2. 파일명이 Minecraft 버전 *그리고* 로더와 일치하는 파일을
   [Modrinth](https://modrinth.com/mod/dayz-inventory/versions) 또는
   [CurseForge](https://www.curseforge.com/minecraft/mc-mods/dayz-inventory/files)에서
   다운로드하세요.
3. `mods/` 폴더에 넣으세요.

> **신중히 고르세요.** Fabric 빌드는 Forge나 NeoForge에서 로드되지 않고, NeoForge 빌드는 Forge에서
> 로드되지 않으며, 서로 다른 Minecraft 버전의 빌드도 호환되지 않습니다. 각 파일명에는 Minecraft
> 버전과 로더가 들어 있습니다.

---

## 전체 버전 매트릭스

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

즉, **23개의 Minecraft 버전과 53개의 배포 가능한 jar**: Fabric 23개, NeoForge 18개, Forge 12개입니다.
Fabric과 NeoForge는 1.20.6 이상의 모든 버전을 커버합니다. 매트릭스의 빈 곳은 오직 다음과 같습니다:

- **1.20.1–1.20.4**는 Fabric 전용입니다. 1.20.1은 NeoForge보다 전에 존재했습니다. NeoForge의 1.20.2
  릴리스는 여전히 구형 `SimpleChannel` 네트워킹 스택을 사용합니다. 1.20.3에는 NeoForge 릴리스가
  전혀 없었습니다. 1.20.4의 payload API는 `StreamCodec`이 존재하기 전에 도착했으므로 자체 payload
  타입이 필요합니다. Forge 1.20.1은 추가로 SRG 리오브fuscation과 Searge mixin refmap이 필요한데,
  현재 Forge 툴체인으로는 만들 수 없습니다.
- **1.20.5**는 Fabric 전용입니다: NeoForge는 빌드에 필요한 메타데이터 없이 해당 릴리스를 공개했고,
  Forge에는 1.20.5 릴리스가 없습니다.
- **1.21.2**에 Forge 빌드가 없는 이유는 Forge가 그 릴리스를 건너뛰었기 때문입니다.
- **26.x에 Forge 빌드가 없습니다**: Forge의 26.x 계열은 이 모드가 빌드하는 대상이 아니며, 그곳에서는
  NeoForge가 지원되는 경로입니다.

모든 아티팩트는 **단일 소스 트리**에서 빌드됩니다: [Stonecutter](https://stonecutter.kikugie.dev/)가
멀티 버전 전처리를, `common/` + 로더별 모듈이 로더 추상화를 담당합니다. 매트릭스는
`settings.gradle.kts`에 선언되어 있으며, 버전을 추가하려면 그곳에 한 줄과
`versions/<mc>/gradle.properties` 파일을 추가하면 됩니다.

---

## 의존성

| Minecraft | 로더 | Java | 필수 | 선택 |
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

선택 항목은 모두 실행 시에 탐지됩니다 — 이 모드는 절대 그것들을 요구하지 않으며, 없어도 크래시하지
않습니다. Trinkets는 Fabric 전용이고, NeoForge와 Forge에서는 그 역할을 Curios가 맡습니다.

---

## 소스에서 빌드

Gradle 런처에는 **JDK 25**가 필요합니다. 매트릭스의 나머지가 필요로 하는 Java 17 / 21 툴체인은
foojay 리졸버가 자동으로 다운로드하므로, 수동으로 설치할 필요가 없습니다.

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

아티팩트 이름은 `dayz-inventory-<loader>-<minecraft>-<mod version>.jar`이며, 예를 들어
`dayz-inventory-fabric-26.2-1.8.0+mc26.2.jar`입니다. Minecraft 버전이 파일명의 일부인 것은
의도적인 것입니다: 같은 모드 버전이 여러 게임 버전용으로 출시되고, CurseForge는 프로젝트 내에서
표시 이름이 충돌하는 두 번째 파일을 거부하기 때문입니다.

아키텍처, 조건부 컴파일 규약, 그리고 "버전 추가 방법"은
**[docs/BUILDING.en.md](docs/BUILDING.en.md)**에 문서화되어 있습니다.

### CurseForge 및 Modrinth에 게시

```bash
MODRINTH_TOKEN=... CURSEFORGE_API_KEY=... \
  ./gradlew publishAll -Ppublish.dry_run=false
```

- 모든 노드는 `versions/<mc>/gradle.properties`에서 가져온 **자신의** 게임 버전과 로더로 태그되므로,
  jar가 잘못된 Minecraft 버전 아래에 업로드될 수 없습니다.
- `publish.dry_run`의 기본값은 `true`입니다. 명시적으로 끄지 않으면 `publishMods`는 로그만 남깁니다.
- **CurseForge 게시는 "던지고 끝"입니다.** 모든 파일은 사람의 검토를 거치고 API는 URL을 반환하지 않고
  받아들이므로, 초록색 CurseForge 작업은 "라이브"가 아니라 "제출됨"을 의미합니다.
- 한 번에 한 플랫폼씩: `:fabric:26.2:publishModrinth` / `:fabric:26.2:publishCurseforge`.

CI(`.github/workflows/build.yml`)는 `v*` 태그에서 매트릭스 전체를 빌드하고, 모든 jar를 GitHub
Release에 첨부하며, 두 플랫폼 모두에 게시합니다.

## 버전 관리

`gradle.properties`의 `mod.version`이 단일 진실 공급원(single source of truth)입니다.
`fabric.mod.json`, `neoforge.mods.toml`, `mods.toml`, `pack.mcmeta`에 전개됩니다. 업데이트한 뒤
[CHANGELOG.md](CHANGELOG.md)를 갱신하세요.

## 라이선스

Apache License 2.0 — [LICENSE](LICENSE)를 참조하세요.
