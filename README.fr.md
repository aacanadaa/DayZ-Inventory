# DayZ Inventory

[English](README.md) · [中文](README.zh.md) · **Français** · [日本語](README.ja.md) · [한국어](README.ko.md)

[![Modrinth](https://img.shields.io/modrinth/v/dayz-inventory?label=Modrinth&logo=modrinth)](https://modrinth.com/mod/dayz-inventory)
[![CurseForge](https://img.shields.io/curseforge/v/1596267?label=CurseForge&logo=curseforge&color=F16436)](https://www.curseforge.com/minecraft/mc-mods/dayz-inventory)
[![Minecraft](https://img.shields.io/modrinth/game-versions/dayz-inventory?label=Minecraft)](https://modrinth.com/mod/dayz-inventory/versions)
[![Modrinth Downloads](https://img.shields.io/modrinth/dt/dayz-inventory?label=Downloads&logo=modrinth)](https://modrinth.com/mod/dayz-inventory)
[![CurseForge Downloads](https://img.shields.io/curseforge/dt/1596267?label=Downloads&logo=curseforge&color=F16436)](https://www.curseforge.com/minecraft/mc-mods/dayz-inventory)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Issues](https://img.shields.io/github/issues/aacanadaa/DayZ-Inventory?color=red)](https://github.com/aacanadaa/DayZ-Inventory/issues)
[![Ko-fi](https://img.shields.io/badge/Ko--fi-Support%20me-ff5e5b?logo=kofi&logoColor=white)](https://ko-fi.com/suoim)

Remplace l'écran d'inventaire de Minecraft par une interface façon DayZ : grille **Proximité (Vicinity)** pour les objets au sol et conteneurs voisins, panneau **Survivant** avec équipement, effets et jauges vitales, emplacement **Mains** 2x et grille d'**artisanat 2x2**.

Se marie avec [DayZ Hotbar](https://modrinth.com/mod/dayz-hotbar), qui partage le même style pour le HUD.

![Capture d'écran DayZ Inventory](docs/screenshots/ui-example.png)

---

## Versions supportées

| Chargeur | Minecraft |
| :--- | :--- |
| **Fabric** | 1.20.1 – 26.3 |
| **NeoForge** | 1.20.6 – 26.3 |
| **Forge** | 1.20.1, 1.20.6 – 1.21.11 |

23 versions de Minecraft, 54 fichiers. Téléchargez le jar correspondant à votre version et à votre chargeur de mods.

---

## Fonctionnalités

### Proximité (Vicinity)
- **Objets au sol et conteneurs :** Affiche les objets lâchés ainsi que les coffres, tonneaux, boîtes de Shulker et autres conteneurs dans un rayon de 3 blocs (configurable).
- **Ramassage manuel :** Le ramassage automatique en marchant dessus est désactivé en Survie. Glissez ou Shift-cliquez pour récupérer les objets.
- **Tiroirs intégrés :** Cliquez sur un conteneur pour ouvrir ses emplacements sous la grille, les trier et les fermer avec `x`. Verrous, coffres bloqués et tables de butin fonctionnent comme en vanilla.

### Survivant & Inventaire
- **Équipement, effets et jauges :** Armure, main secondaire, icônes d'effets avec infobulle (clignotant les 10 dernières secondes) et barres de vie, faim et soif (avec **Tough As Nails**, **Thirst Was Taken** ou **Dehydration**).
- **Mains et artisanat :** Aperçu 2x de l'objet tenu et grille 2x2.
- **Tri et Shift-clic :** Bouton de tri pour fusionner les piles et trier l'inventaire principal (sans toucher à la barre d'accès rapide) ou un conteneur ouvert.

### Compatibilités (optionnelles)
- **JEI / REI / EMI :** Bouton d'en-tête pour afficher/masquer la liste d'objets.
- **Curios / Accessories / Trinkets :** Bouton d'accès rapide sur le panneau Survivant.
- **Mouse Tweaks :** Glisser-déposer et molette pris en charge.

---

## Installation

1. Téléchargez le jar correspondant à votre version de Minecraft et à votre chargeur sur [Modrinth](https://modrinth.com/mod/dayz-inventory/versions) ou [CurseForge](https://www.curseforge.com/minecraft/mc-mods/dayz-inventory/files).
2. Placez-le dans `mods/` côté **client et serveur**.
3. Sur **Fabric**, installez aussi [Fabric API](https://modrinth.com/mod/fabric-api) (Fabric Loader 0.15+).

---

## Configuration

`config/dayz_inventory.properties` est généré au premier lancement (redémarrage requis) :

| Option | Défaut | Description |
| :--- | :--- | :--- |
| `autoPickup` | `false` | Ramassage automatique comme en vanilla (toujours actif en Créatif) |
| `openContainersInDayZScreen` | `true` | Ouvrir coffres et tonneaux dans l'écran DayZ |
| `vicinityRadius` | `3` | Rayon de détection en blocs (1–8 ; identique client/serveur) |
| `replaceInventoryScreen` | `true` | La touche d'inventaire ouvre l'écran DayZ |
| `showStatusEffects` | `true` | Afficher les effets sur le panneau Survivant |
| `showVitals` | `true` | Afficher vie, faim et soif sur le panneau Survivant |
| `dragAndDrop` | `true` | Cliquer-relâcher entre cases (désactivé avec Mouse Tweaks) |

---

## Compiler depuis les sources

Nécessite **JDK 25** pour Gradle (Java 17/21 téléchargés automatiquement), voir [docs/BUILDING.md](docs/BUILDING.md) :

```bash
./gradlew chiseledBuild                      # Tout compiler
./gradlew buildMods -Pdayz.versions=1.21.1   # Compiler une version MC
./gradlew :forge:1.20.1:build                # Compiler une cible précise
```

## Licence

Apache License 2.0 — voir [LICENSE](LICENSE).
