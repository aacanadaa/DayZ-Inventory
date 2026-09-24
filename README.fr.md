# DayZ Inventory

[English](README.md) · [中文](README.zh.md) · **Français** · [日本語](README.ja.md) · [한국어](README.ko.md)

[![Modrinth](https://img.shields.io/modrinth/v/dayz-inventory?label=Modrinth&logo=modrinth)](https://modrinth.com/mod/dayz-inventory)
[![CurseForge](https://img.shields.io/curseforge/v/1596267?label=CurseForge&logo=curseforge&color=F16436)](https://www.curseforge.com/minecraft/mc-mods/dayz-inventory)
[![Minecraft](https://img.shields.io/modrinth/game-versions/dayz-inventory?label=Minecraft)](https://modrinth.com/mod/dayz-inventory/versions)
[![Downloads](https://img.shields.io/modrinth/dt/dayz-inventory?label=Downloads&logo=modrinth)](https://modrinth.com/mod/dayz-inventory)
[![CurseForge Downloads](https://img.shields.io/curseforge/dt/1596267?label=Downloads&logo=curseforge&color=F16436)](https://www.curseforge.com/minecraft/mc-mods/dayz-inventory)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Ko-fi](https://img.shields.io/badge/Ko--fi-Support%20me-ff5e5b?logo=kofi&logoColor=white)](https://ko-fi.com/suoim)

Refonte complète de l'interface d'inventaire de Minecraft, qui transplante l'aspect, la sensation
et les mécaniques du système d'inventaire de DayZ dans Minecraft : une grille **Vicinity**
(environnement) des objets au sol et des conteneurs proches, un emplacement d'attachment **Hands**
(mains) dynamique lié au slot de la barre d'accès actif, une **grille de crafting 2x2** intégrée,
et l'équipement par glisser-déposer sur le panneau **Survivor** (survivant).

**Fortement recommandé avec [DayZ Hotbar](https://modrinth.com/mod/dayz-hotbar)** — intégration
transparente. Les deux sont conçus comme un duo, donc le HUD et l'écran d'inventaire partagent un
même style.

## Versions prises en charge

| Loader | Minecraft |
| :--- | :--- |
| **Fabric** | 1.20.1 – 26.3 |
| **NeoForge** | 1.20.6 – 26.3 |
| **Forge** | 1.20.6 – 1.21.11 |

**23 versions de Minecraft, 53 téléchargements**, tous issus d'une seule arborescence source. Chaque
fichier est étiqueté avec sa version de Minecraft et son loader. La [matrice complète des
versions](#matrice-complète-des-versions) ci-dessous liste chaque version, et
[Dépendances](#dépendances) donne les versions de loader requises.

![L'écran DayZ Inventory : une grille VICINITY des objets proches, un tiroir CHEST ouvert avec des
chambres, le panneau SURVIVOR avec le joueur en équipement, les boutons CURIOS et JEI dans
l'en-tête, un M1014 Battle Shotgun dans l'emplacement HANDS à 2.0x, et la grille de CRAFTING
2x2](docs/screenshots/ui-example.png)

---

## Fonctionnalités

### Grille Vicinity unifiée et tiroirs dépliables
- **Scanner de proximité** : rescanne toutes les 10 ticks les objets au sol et les blocs conteneurs
  (coffres, tonneaux, boîtes de Shulker) dans un rayon de 3 blocs.
- **Colonne unifiée** : les objets au sol et les blocs de stockage proches partagent une seule
  grille défilable sous l'en-tête VICINITY.
- **Sélecteurs de conteneur** : les conteneurs sont dessinés en icônes d'emplacement avec info-bulles
  de coordonnées et de distance.
- **Tiroirs en ligne** : cliquer sur un sélecteur de conteneur déplie ses emplacements en ligne, sous
  la grille.

### Emplacement d'attachment Hands dynamique
- **Liaison à la barre d'accès active** : reflette le slot de la hotbar actuellement sélectionné.
- **Grand emplacement d'attachment** : un corps de panneau entièrement translucide remplace
  l'emplacement par défaut.
- **Rendu à double échelle** : l'objet tenu est dessiné à **2.0x** (32x32 px) centré dans le panneau.
- **Bannière en majuscules** : affiche le nom de l'objet (ex. `HUNTING KNIFE`) sous l'en-tête.

### Crafting et changement d'équipement
- **Grille vanilla 2x2** : la grille de crafting et l'emplacement de résultat vivent dans l'écran
  personnalisé ; le résultat est recalculé côté serveur.
- **Équipement par glisser-déposer** : glissez une armure ou des vêtements sur le panneau Survivor
  central pour l'équiper ou le remplacer.

### Intégrations de mods optionnelles
- **Visionneuses de recettes (JEI / REI / EMI)** : un bouton d'activation assorti au thème dans
  l'en-tête, dessiné uniquement quand une visionneuse prise en charge est installée.
- **Curios API** : ajoute un bouton CURIOS à l'en-tête du Survivor.
- **Trinkets** : ajoute un bouton TRINKETS à l'en-tête du Survivor.

Chaque intégration est détectée à l'exécution avec `isModLoaded`. Le mod démarre, ouvre son écran et
fonctionne pleinement avec **aucune** d'elles installée.

---

## Installation

1. Tant que votre version de Minecraft figure dans le tableau ci-dessus, vous êtes couvert :
   **Fabric** couvre les 23 versions, **NeoForge** à partir de 1.20.6, et **Forge** de 1.20.6 à
   1.21.11.
2. Téléchargez le fichier dont le nom correspond à votre version de Minecraft *et* à votre loader
   depuis [Modrinth](https://modrinth.com/mod/dayz-inventory/versions) ou
   [CurseForge](https://www.curseforge.com/minecraft/mc-mods/dayz-inventory/files).
3. Déposez-le dans `mods/`.

> **Choisissez attentivement.** Un build Fabric ne se chargera pas sur Forge ni sur NeoForge, un
> build NeoForge ne se chargera pas sur Forge, et les builds pour différentes versions de Minecraft
> ne sont pas interchangeables. Chaque nom de fichier porte sa version de Minecraft et son loader.

---

## Matrice complète des versions

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

Soit **23 versions de Minecraft et 53 jars publiables** : 23 Fabric, 18 NeoForge et 12 Forge. Fabric
et NeoForge couvrent chaque version à partir de 1.20.6. Les seuls trous de la matrice sont ceux-ci :

- **1.20.1–1.20.4** ne sont disponibles que sur Fabric. 1.20.1 précède entièrement NeoForge ; la
  version NeoForge de 1.20.2 utilise encore l'ancienne pile réseau `SimpleChannel` ; 1.20.3 n'avait
  aucune release NeoForge ; et l'API payload de 1.20.4 est arrivée avant l'existence de
  `StreamCodec`, elle aurait donc nécessité son propre type de payload. Forge 1.20.1 requiert en
  outre la réobfuscation SRG et un refmap de mixin Searge, que la chaîne d'outils Forge actuelle ne
  peut pas produire.
- **1.20.5** n'est disponible que sur Fabric : NeoForge a publié cette version sans les métadonnées
  nécessaires à la compilation, et Forge n'a pas de release 1.20.5.
- **1.21.2** n'a pas de build Forge parce que Forge a sauté cette release.
- **26.x n'a pas de build Forge** : la ligne 26.x de Forge n'est pas une cible pour laquelle ce mod
  se compile ; NeoForge est la voie prise en charge sur cette ligne.

Chaque artefact est construit à partir d'**une seule arborescence source** :
[Stonecutter](https://stonecutter.kikugie.dev/) gère le prétraitement multi-versions et les modules
`common/` + par loader gèrent l'abstraction des loaders. La matrice est déclarée dans
`settings.gradle.kts` ; ajouter une version consiste à ajouter une ligne là-bas plus un fichier
`versions/<mc>/gradle.properties`.

---

## Dépendances

| Minecraft | Loader | Java | Requis | Optionnel |
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

Les entrées optionnelles sont détectées à l'exécution — le mod ne les requiert jamais et ne plantera
pas sans elles. Trinkets n'existe que pour Fabric ; sur NeoForge et Forge, c'est Curios qui joue ce
rôle.

---

## Compilation depuis les sources

Le lanceur Gradle a besoin d'un **JDK 25**. Les toolchains Java 17 / 21 dont le reste de la matrice
a besoin sont téléchargés automatiquement par le résolveur foojay, rien n'a donc besoin d'être
installé à la main.

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

Les artefacts sont nommés `dayz-inventory-<loader>-<minecraft>-<mod version>.jar`, par exemple
`dayz-inventory-fabric-26.2-1.8.0+mc26.2.jar`. La version de Minecraft fait volontairement partie du
nom de fichier : la même version du mod est publiée pour plusieurs versions du jeu, et CurseForge
rejette un second fichier dont le nom d'affichage entre en collision dans un projet.

L'architecture, les conventions de compilation conditionnelle et la procédure pour ajouter une
version sont documentées dans **[docs/BUILDING.en.md](docs/BUILDING.en.md)**.

### Publication sur CurseForge et Modrinth

```bash
MODRINTH_TOKEN=... CURSEFORGE_API_KEY=... \
  ./gradlew publishAll -Ppublish.dry_run=false
```

- Chaque nœud est étiqueté avec **sa propre** version de jeu et son loader, issus de
  `versions/<mc>/gradle.properties`, si bien qu'un jar ne peut pas être téléversé sous la mauvaise
  version de Minecraft.
- `publish.dry_run` vaut `true` par défaut ; sans le désactiver, `publishMods` se contente de
  journaliser.
- **Les publications CurseForge sont sans retour.** Chaque fichier passe en revue humaine et l'API
  l'accepte sans renvoyer d'URL : une tâche CurseForge verte signifie « soumis », pas « en ligne ».
- Une plateforme à la fois : `:fabric:26.2:publishModrinth` / `:fabric:26.2:publishCurseforge`.

La CI (`.github/workflows/build.yml`) compile toute la matrice sur une étiquette `v*`, joint chaque
jar à la GitHub Release et publie sur les deux plateformes.

## Numérotation des versions

`mod.version` dans `gradle.properties` est la source unique de vérité. Il est développé dans
`fabric.mod.json`, `neoforge.mods.toml`, `mods.toml` et `pack.mcmeta`. Incrémentez-le, puis mettez à
jour [CHANGELOG.md](CHANGELOG.md).

## Licence

Apache License 2.0 — voir [LICENSE](LICENSE).
