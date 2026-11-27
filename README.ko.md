# DayZ Inventory

[English](README.md) · [中文](README.zh.md) · [Français](README.fr.md) · [日本語](README.ja.md) · **한국어**

[![Modrinth](https://img.shields.io/modrinth/v/dayz-inventory?label=Modrinth&logo=modrinth)](https://modrinth.com/mod/dayz-inventory)
[![CurseForge](https://img.shields.io/curseforge/v/1596267?label=CurseForge&logo=curseforge&color=F16436)](https://www.curseforge.com/minecraft/mc-mods/dayz-inventory)
[![Minecraft](https://img.shields.io/modrinth/game-versions/dayz-inventory?label=Minecraft)](https://modrinth.com/mod/dayz-inventory/versions)
[![Modrinth Downloads](https://img.shields.io/modrinth/dt/dayz-inventory?label=Downloads&logo=modrinth)](https://modrinth.com/mod/dayz-inventory)
[![CurseForge Downloads](https://img.shields.io/curseforge/dt/1596267?label=Downloads&logo=curseforge&color=F16436)](https://www.curseforge.com/minecraft/mc-mods/dayz-inventory)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Issues](https://img.shields.io/github/issues/aacanadaa/DayZ-Inventory?color=red)](https://github.com/aacanadaa/DayZ-Inventory/issues)
[![Ko-fi](https://img.shields.io/badge/Ko--fi-Support%20me-ff5e5b?logo=kofi&logoColor=white)](https://ko-fi.com/suoim)

Minecraft 인벤토리 화면을 DayZ 스타일로 바꿉니다. 주변 땅 아이템과 상자를 보여주는 **주변(Vicinity)** 그리드, 장비·상태 효과·바이탈을 표시하는 **생존자(Survivor)** 패널, **2x 손(Hands)** 슬롯, **2x2 제작**을 제공합니다.

같은 스타일의 HUD 모드인 [DayZ Hotbar](https://modrinth.com/mod/dayz-hotbar)와 함께 쓰면 잘 어울립니다.

![DayZ Inventory 화면](docs/screenshots/ui-example.png)

---

## 지원 버전

| 로더 | Minecraft |
| :--- | :--- |
| **Fabric** | 1.20.1 – 26.3 |
| **NeoForge** | 1.20.6 – 26.3 |
| **Forge** | 1.20.1, 1.20.6 – 1.21.11 |

23개 Minecraft 버전, 54개 빌드. 사용 중인 버전과 로더에 모두 맞는 jar를 받으세요.

---

## 기능

### 주변 (Vicinity)
- **땅 아이템 및 컨테이너:** 반경 3블록(설정 가능) 내 떨어진 아이템과 상자, 통, 셜커 상자 등을 가까운 순으로 보여줍니다.
- **수동 줍기:** 서바이벌에서는 걸어서 자동 줍기가 꺼지며, 드래그하거나 Shift+클릭으로 줍습니다.
- **인라인 서랍:** 컨테이너를 클릭하면 그리드 바로 아래에 슬롯이 열리고, 정렬 후 `x`로 닫을 수 있습니다. 잠금과 전리품 테이블도 바닐라처럼 동작합니다.

### 생존자 & 인벤토리
- **장비, 효과 및 바이탈:** 방어구·보조 손 슬롯, 상태 효과 아이콘(마우스를 올리면 상세 표시, 마지막 10초 동안 깜빡임), 체력·허기·갈증 바(**Tough As Nails**, **Thirst Was Taken**, **Dehydration** 설치 시).
- **손 & 제작:** 선택된 핫바 슬롯과 연동되는 2배 크기 미리보기와 2x2 제작 그리드.
- **정렬 & Shift 클릭:** 핫바는 건드리지 않고 메인 인벤토리나 열린 컨테이너의 아이템을 합치고 정렬합니다.

### 호환 모드 (선택 사항)
- **JEI / REI / EMI:** 상단 버튼으로 레시피 목록을 켜고 끌 수 있습니다.
- **Curios / Accessories / Trinkets:** 생존자 패널에 바로가기 버튼을 추가합니다.
- **Mouse Tweaks:** 드래그 및 마우스 휠 이동을 지원합니다.

---

## 설치

1. [Modrinth](https://modrinth.com/mod/dayz-inventory/versions) 또는 [CurseForge](https://www.curseforge.com/minecraft/mc-mods/dayz-inventory/files)에서 버전과 로더에 맞는 jar를 다운로드합니다.
2. **클라이언트와 서버 모두** `mods/` 폴더에 넣습니다.
3. **Fabric**은 [Fabric API](https://modrinth.com/mod/fabric-api)(Fabric Loader 0.15+)도 필요합니다.

---

## 설정

첫 실행 시 `config/dayz_inventory.properties`가 생성됩니다 (재시작 후 적용):

| 옵션 | 기본값 | 설명 |
| :--- | :--- | :--- |
| `autoPickup` | `false` | 바닐라처럼 자동 줍기 (크리에이티브에서는 항상 활성) |
| `openContainersInDayZScreen` | `true` | 상자/통 우클릭 시 DayZ 화면으로 열기 |
| `vicinityRadius` | `3` | 주변 탐색 반경 (1–8블록, 서버/클라이언트 동일 권장) |
| `replaceInventoryScreen` | `true` | 인벤토리 키로 DayZ 화면 열기 |
| `showStatusEffects` | `true` | 생존자 패널에 상태 효과 표시 |
| `showVitals` | `true` | 생존자 패널에 체력/허기/갈증 바 표시 |
| `dragAndDrop` | `true` | 클릭 후 놓기 이동 (Mouse Tweaks 사용 시 자동 비활성화) |

---

## 소스에서 빌드

Gradle 실행에 **JDK 25**가 필요합니다 (Java 17/21 툴체인은 자동 다운로드). 자세한 내용은 [docs/BUILDING.md](docs/BUILDING.md)를 참고하세요:

```bash
./gradlew chiseledBuild                      # 전체 버전 및 로더 빌드
./gradlew buildMods -Pdayz.versions=1.21.1   # 특정 MC 버전 빌드
./gradlew :forge:1.20.1:build                # 단일 타겟 빌드
```

## 라이선스

Apache License 2.0 — [LICENSE](LICENSE) 참고.
