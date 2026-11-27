# DayZ Inventory

[English](README.md) · [中文](README.zh.md) · **Français** · [日本語](README.ja.md) · [한국어](README.ko.md)

[![Modrinth](https://img.shields.io/modrinth/v/dayz-inventory?label=Modrinth&logo=modrinth)](https://modrinth.com/mod/dayz-inventory)
[![CurseForge](https://img.shields.io/curseforge/v/1596267?label=CurseForge&logo=curseforge&color=F16436)](https://www.curseforge.com/minecraft/mc-mods/dayz-inventory)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Ko-fi](https://img.shields.io/badge/Ko--fi-Support%20me-ff5e5b?logo=kofi&logoColor=white)](https://ko-fi.com/suoim)

Une refonte complète de l'inventaire de Minecraft, qui reprend l'apparence, les sensations et les
mécaniques de l'inventaire de DayZ : une grille **Alentours (Vicinity)** des objets au sol et des
conteneurs proches, un emplacement **Mains (Hands)** lié à l'emplacement actif de la barre rapide,
le panneau **Survivant (Survivor)** avec votre équipement, vos effets et vos constantes vitales, et
une **grille d'artisanat 2x2** intégrée.

**Vivement conseillé avec [DayZ Hotbar](https://modrinth.com/mod/dayz-hotbar)** : les deux mods sont
conçus ensemble, le HUD et l'inventaire partagent le même style.

## Versions prises en charge

| Chargeur | Minecraft |
| :--- | :--- |
| **Fabric** | 1.20.1 – 26.3 |
| **NeoForge** | 1.20.6 – 26.3 |
| **Forge** | 1.20.1, 1.20.6 – 1.21.11 |

**23 versions de Minecraft, 54 fichiers**, tous issus d'un seul code source. Chaque nom de fichier
indique sa version de Minecraft et son chargeur ; le [tableau complet](README.md#full-version-table)
(en anglais) détaille chaque version.

![L'écran DayZ Inventory](docs/screenshots/ui-example.png)

## Fonctionnalités

- **Alentours** : les objets au sol et tous les blocs à inventaire (coffres, tonneaux, boîtes de
  Shulker…) dans un rayon de 3 blocs (réglable) partagent une même grille, le conteneur le plus proche
  en premier. Un clic sur un conteneur affiche ses emplacements juste en dessous ; on peut le trier
  et le refermer avec le `x`. Verrous, coffres bloqués et tables de butin fonctionnent comme avec un
  clic droit sur le bloc.
- **Ramassage à la main** : le ramassage en marchant dessus est désactivé. Glissez un objet dans un
  emplacement, déposez-le sur le panneau Survivant pour l'équiper, ou faites Maj+clic pour le prendre.
- **Effets** : les icônes des effets actifs sur le panneau Survivant, avec nom, niveau et temps
  restant au survol ; elles clignotent pendant leurs dix dernières secondes.
- **Constantes vitales** : barres de santé, de nourriture et de soif. La soif s'affiche avec
  **Tough As Nails**, **Thirst Was Taken** ou **Dehydration**.
- **Tri** : fusionne les piles incomplètes et trie l'inventaire principal (la barre rapide ne bouge
  pas) ; les conteneurs de stockage ont leur propre bouton TRIER.
- **Maj+clic** : entre l'inventaire principal et la barre rapide, vers un conteneur ouvert, ou
  directement sur le personnage.
- **Compatibilité** : bouton pour afficher/masquer JEI / REI / EMI ; un bouton pour Curios,
  Accessories et Trinkets ; prise en charge de Mouse Tweaks. Tout est facultatif : le mod fonctionne
  entièrement sans eux.

## Installation

1. Trouvez votre version de Minecraft et votre chargeur dans le
   [tableau complet](README.md#full-version-table).
2. Téléchargez le fichier qui correspond **aux deux** sur
   [Modrinth](https://modrinth.com/mod/dayz-inventory/versions) ou
   [CurseForge](https://www.curseforge.com/minecraft/mc-mods/dayz-inventory/files).
3. Placez-le dans `mods/`. Sur Fabric, installez aussi [Fabric API](https://modrinth.com/mod/fabric-api).

Installez-le **sur le serveur et sur chaque client** : l'écran, le ramassage et l'ouverture des
conteneurs passent en partie par le serveur.

> Une version Fabric ne se charge pas sur Forge ou NeoForge, une version NeoForge ne se charge pas sur
> Forge, et les fichiers de versions de Minecraft différentes ne sont pas interchangeables.

## Configuration

`config/dayz_inventory.properties` est créé au premier lancement ; les changements s'appliquent au
démarrage suivant.

| Option | Défaut | |
| :--- | :--- | :--- |
| `autoPickup` | `false` | Ramassage en marchant dessus, comme en vanilla. Toujours actif en créatif. |
| `openContainersInDayZScreen` | `true` | Un clic droit sur un coffre ou un tonneau l'ouvre dans l'écran DayZ. |
| `vicinityRadius` | `3` | Portée du panneau Alentours, de 1 à 8 blocs. Même valeur sur le serveur et les clients. |
| `replaceInventoryScreen` | `true` | La touche d'inventaire ouvre l'écran DayZ. |
| `showStatusEffects` | `true` | Effets sur le panneau Survivant. |
| `showVitals` | `true` | Barres de santé, de nourriture et de soif. |
| `dragAndDrop` | `true` | Appuyer sur un objet et le relâcher sur un autre emplacement pour le déplacer. Désactivé avec Mouse Tweaks. |

## Compilation

Il faut le **JDK 25** : `./gradlew chiseledBuild` compile toutes les versions,
`./gradlew buildMods -Pdayz.versions=1.21.1` une seule. Le fonctionnement du build est décrit dans
**[docs/BUILDING.md](docs/BUILDING.md)** (en anglais).

## Licence

Apache License 2.0 — voir [LICENSE](LICENSE).
