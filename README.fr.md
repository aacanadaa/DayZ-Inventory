# DayZ Inventory

[English](README.md) Â· [ä¸­æ–‡](README.zh.md) Â· **FranÃ§ais** Â· [æ—¥æœ¬èªž](README.ja.md) Â· [í•œêµ­ì–´](README.ko.md)

[![Modrinth](https://img.shields.io/modrinth/v/dayz-inventory?label=Modrinth&logo=modrinth)](https://modrinth.com/mod/dayz-inventory)
[![CurseForge](https://img.shields.io/curseforge/v/1596267?label=CurseForge&logo=curseforge&color=F16436)](https://www.curseforge.com/minecraft/mc-mods/dayz-inventory)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1%20%E2%80%93%2026.3-62b47a.svg)](https://modrinth.com/mod/dayz-inventory/versions)
[![Modrinth Downloads](https://img.shields.io/modrinth/dt/dayz-inventory?label=Downloads&logo=modrinth)](https://modrinth.com/mod/dayz-inventory)
[![CurseForge Downloads](https://img.shields.io/curseforge/dt/1596267?label=Downloads&logo=curseforge&color=F16436)](https://www.curseforge.com/minecraft/mc-mods/dayz-inventory)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Issues](https://img.shields.io/github/issues/aacanadaa/DayZ-Inventory?color=red)](https://github.com/aacanadaa/DayZ-Inventory/issues)
[![Ko-fi](https://img.shields.io/badge/Ko--fi-Support%20me-ff5e5b?logo=kofi&logoColor=white)](https://ko-fi.com/suoim)

Remplace l'Ã©cran d'inventaire de Minecraft par une interface faÃ§on DayZ : grille **ProximitÃ© (Vicinity)** pour les objets au sol et conteneurs voisins, panneau **Survivant** avec Ã©quipement, effets et jauges vitales, emplacement **Mains** 2x et grille d'**artisanat 2x2**.

Se marie avec [DayZ Hotbar](https://modrinth.com/mod/dayz-hotbar), qui partage le mÃªme style pour le HUD.

![Capture d'Ã©cran DayZ Inventory](docs/screenshots/ui-example.png)

---

## Versions supportÃ©es

| Chargeur | Minecraft |
| :--- | :--- |
| **Fabric** | 1.20.1 â€“ 26.3 |
| **NeoForge** | 1.20.6 â€“ 26.3 |
| **Forge** | 1.20.1, 1.20.6 â€“ 1.21.11 |

23 versions de Minecraft, 54 fichiers. TÃ©lÃ©chargez le jar correspondant Ã  votre version et Ã  votre chargeur de mods.

---

## FonctionnalitÃ©s

### ProximitÃ© (Vicinity)
- **Objets au sol et conteneurs :** Affiche les objets lÃ¢chÃ©s ainsi que les coffres, tonneaux, boÃ®tes de Shulker et autres conteneurs dans un rayon de 3 blocs (configurable).
- **Ramassage manuel :** Le ramassage automatique en marchant dessus est dÃ©sactivÃ© en Survie. Glissez ou Shift-cliquez pour rÃ©cupÃ©rer les objets.
- **Tiroirs intÃ©grÃ©s :** Cliquez sur un conteneur pour ouvrir ses emplacements sous la grille, les trier et les fermer avec `x`. Verrous, coffres bloquÃ©s et tables de butin fonctionnent comme en vanilla.

### Survivant & Inventaire
- **Ã‰quipement, effets et jauges :** Armure, main secondaire, icÃ´nes d'effets avec infobulle (clignotant les 10 derniÃ¨res secondes) et barres de vie, faim et soif (avec **Tough As Nails**, **Thirst Was Taken** ou **Dehydration**).
- **Mains et artisanat :** AperÃ§u 2x de l'objet tenu et grille 2x2.
- **Tri et Shift-clic :** Bouton de tri pour fusionner les piles et trier l'inventaire principal (sans toucher Ã  la barre d'accÃ¨s rapide) ou un conteneur ouvert.

### CompatibilitÃ©s (optionnelles)
- **JEI / REI / EMI :** Bouton d'en-tÃªte pour afficher/masquer la liste d'objets.
- **Curios / Accessories / Trinkets :** Bouton d'accÃ¨s rapide sur le panneau Survivant.
- **Mouse Tweaks :** Glisser-dÃ©poser et molette pris en charge.

---

## Installation

1. TÃ©lÃ©chargez le jar correspondant Ã  votre version de Minecraft et Ã  votre chargeur sur [Modrinth](https://modrinth.com/mod/dayz-inventory/versions) ou [CurseForge](https://www.curseforge.com/minecraft/mc-mods/dayz-inventory/files).
2. Placez-le dans `mods/` cÃ´tÃ© **client et serveur**.
3. Sur **Fabric**, installez aussi [Fabric API](https://modrinth.com/mod/fabric-api) (Fabric Loader 0.15+).

---

## Configuration

`config/dayz_inventory.properties` est gÃ©nÃ©rÃ© au premier lancement (redÃ©marrage requis) :

| Option | DÃ©faut | Description |
| :--- | :--- | :--- |
| `autoPickup` | `false` | Ramassage automatique comme en vanilla (toujours actif en CrÃ©atif) |
| `openContainersInDayZScreen` | `true` | Ouvrir coffres et tonneaux dans l'Ã©cran DayZ |
| `vicinityRadius` | `3` | Rayon de dÃ©tection en blocs (1â€“8 ; identique client/serveur) |
| `replaceInventoryScreen` | `true` | La touche d'inventaire ouvre l'Ã©cran DayZ |
| `showStatusEffects` | `true` | Afficher les effets sur le panneau Survivant |
| `showVitals` | `true` | Afficher vie, faim et soif sur le panneau Survivant |
| `dragAndDrop` | `true` | Cliquer-relÃ¢cher entre cases (dÃ©sactivÃ© avec Mouse Tweaks) |

---

## Compiler depuis les sources

NÃ©cessite **JDK 25** pour Gradle (Java 17/21 tÃ©lÃ©chargÃ©s automatiquement), voir [docs/BUILDING.md](docs/BUILDING.md) :

```bash
./gradlew chiseledBuild                      # Tout compiler
./gradlew buildMods -Pdayz.versions=1.21.1   # Compiler une version MC
./gradlew :forge:1.20.1:build                # Compiler une cible prÃ©cise
```

## Licence

Apache License 2.0 â€” voir [LICENSE](LICENSE).
