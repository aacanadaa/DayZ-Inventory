# DayZ Inventory

[English](README.md) · [中文](README.zh.md) · [Français](README.fr.md) · [日本語](README.ja.md) · **한국어**

[![Modrinth](https://img.shields.io/modrinth/v/dayz-inventory?label=Modrinth&logo=modrinth)](https://modrinth.com/mod/dayz-inventory)
[![CurseForge](https://img.shields.io/curseforge/v/1596267?label=CurseForge&logo=curseforge&color=F16436)](https://www.curseforge.com/minecraft/mc-mods/dayz-inventory)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Ko-fi](https://img.shields.io/badge/Ko--fi-Support%20me-ff5e5b?logo=kofi&logoColor=white)](https://ko-fi.com/suoim)

DayZ 인벤토리의 모습과 손맛, 동작 방식을 Minecraft로 그대로 옮긴 인벤토리 UI 전면 개편입니다.
주변 바닥 아이템과 보관함을 보여 주는 **주변(Vicinity)** 그리드, 선택한 핫바 칸과 연동되는 **손(Hands)**
슬롯, 장비·상태 효과·생명 수치를 보여 주는 **생존자(Survivor)** 패널, 그리고 내장 **2x2 제작 그리드**를
제공합니다.

**[DayZ Hotbar](https://modrinth.com/mod/dayz-hotbar)와 함께 쓰는 것을 강력히 추천합니다.** 두 모드는
한 쌍으로 만들어져 HUD와 인벤토리 화면의 스타일이 같습니다.

## 지원 버전

| 로더 | Minecraft |
| :--- | :--- |
| **Fabric** | 1.20.1 – 26.3 |
| **NeoForge** | 1.20.6 – 26.3 |
| **Forge** | 1.20.1, 1.20.6 – 1.21.11 |

**Minecraft 23개 버전, 54개 파일**을 하나의 소스에서 만듭니다. 파일 이름에 Minecraft 버전과 로더가
들어 있으며, 버전별 전체 표는 [영문 README](README.md#full-version-table)에 있습니다.

![DayZ Inventory 화면](docs/screenshots/ui-example.png)

## 기능

- **주변 그리드**: 반경 3블록(설정 가능) 안의 바닥 아이템과 인벤토리가 있는 모든 블록(상자, 통,
  셜커 상자 등)을 한 그리드에 보여 주며, 가까운 보관함부터 나열합니다. 보관함을 클릭하면 그리드 아래에
  칸이 펼쳐지고, 정리하거나 `x`로 닫을 수 있습니다. 잠금, 막힌 상자, 전리품 테이블은 블록을 우클릭할 때와
  똑같이 적용됩니다.
- **손으로 줍기**: 걸어가서 줍는 기능은 꺼져 있습니다. 아이템을 칸으로 끌어다 놓거나, 생존자 패널에
  놓아 바로 장착하거나, Shift+클릭으로 가져옵니다.
- **상태 효과**: 생존자 패널에 아이콘으로 표시하고, 마우스를 올리면 이름·레벨·남은 시간을 보여 줍니다.
  마지막 10초 동안 깜빡입니다.
- **생명 수치**: 체력·배고픔·갈증 막대. 갈증은 **Tough As Nails**, **Thirst Was Taken**,
  **Dehydration** 중 하나가 있을 때 표시됩니다.
- **정리**: 덜 찬 묶음을 합치고 기본 인벤토리를 정렬합니다(핫바는 그대로). 저장용 보관함에도 정리 버튼이
  있습니다.
- **Shift+클릭**: 기본 인벤토리와 핫바 사이, 열린 보관함, 또는 장비 칸으로 옮깁니다.
- **호환**: JEI / REI / EMI 표시 전환 버튼, Curios·Accessories·Trinkets 버튼, Mouse Tweaks 지원.
  모두 선택 사항이며, 없어도 완전히 동작합니다.

## 설치

1. [전체 표](README.md#full-version-table)에서 내 Minecraft 버전과 로더를 확인합니다.
2. [Modrinth](https://modrinth.com/mod/dayz-inventory/versions) 또는
   [CurseForge](https://www.curseforge.com/minecraft/mc-mods/dayz-inventory/files)에서 **둘 다** 맞는 파일을
   내려받습니다.
3. `mods/` 폴더에 넣습니다. Fabric에서는 [Fabric API](https://modrinth.com/mod/fabric-api)도 필요합니다.

**서버와 모든 클라이언트에** 설치하세요. 화면, 줍기, 보관함 열기가 일부 서버에서 동작합니다.

> Fabric 파일은 Forge / NeoForge에서, NeoForge 파일은 Forge에서 불러올 수 없으며,
> Minecraft 버전이 다른 파일끼리도 호환되지 않습니다.

## 설정

처음 실행하면 `config/dayz_inventory.properties`가 만들어집니다. 변경 사항은 다음 실행부터 적용됩니다.

| 옵션 | 기본값 | 설명 |
| :--- | :--- | :--- |
| `autoPickup` | `false` | 바닐라처럼 걸어가서 줍기. 크리에이티브에서는 항상 켜짐. |
| `openContainersInDayZScreen` | `true` | 상자나 통을 우클릭하면 DayZ 화면으로 엽니다. |
| `vicinityRadius` | `3` | 주변 패널 범위(1–8블록). 서버와 클라이언트를 같은 값으로 맞추세요. |
| `replaceInventoryScreen` | `true` | 인벤토리 키로 DayZ 화면을 엽니다. |
| `showStatusEffects` | `true` | 생존자 패널에 상태 효과 표시. |
| `showVitals` | `true` | 체력·배고픔·갈증 막대 표시. |
| `dragAndDrop` | `true` | 아이템을 누른 채 다른 칸에서 놓으면 이동. Mouse Tweaks가 있으면 자동으로 꺼짐. |

## 소스에서 빌드

**JDK 25**가 필요합니다. `./gradlew chiseledBuild`는 모든 버전을, `./gradlew buildMods -Pdayz.versions=1.21.1`은
한 버전만 빌드합니다. 빌드 구조는 **[docs/BUILDING.md](docs/BUILDING.md)**(영문)에 정리되어 있습니다.

## 라이선스

Apache License 2.0 — [LICENSE](LICENSE)를 참고하세요.
