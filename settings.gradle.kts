/*
 * DayZ Inventory - Gradle settings
 * Copyright 2026 suoim
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Multi-version / multi-loader project powered by Stonecutter.
 *
 * The build is structured as a Stonecutter *tree* with one *branch* per source
 * module. Each branch owns its own shared `src/` directory and gets one node
 * (a real Gradle subproject) per Minecraft version it supports:
 *
 *     :common:1.21.1     ->  common/versions/1.21.1 , sources from common/src
 *     :fabric:1.21.1     ->  fabric/versions/1.21.1 , sources from fabric/src
 *     :neoforge:26.2     ->  neoforge/versions/26.2 , sources from neoforge/src
 *
 * Version-independent dependency coordinates live in `versions/<mc>/gradle.properties`.
 */

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        maven("https://maven.neoforged.net/releases/") { name = "NeoForged" }
        maven("https://maven.minecraftforge.net/") { name = "MinecraftForge" }
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
        maven("https://maven.parchmentmc.org") { name = "ParchmentMC" }
    }

    // Plugin versions are pinned here so the branch build scripts stay free of
    // version literals and every module always agrees on a toolchain.
    //
    // The two Loom ids are listed even though no build script asks for them by
    // name: `dev.kikugie.loom-back-compat` applies one of them programmatically
    // depending on whether the node's Minecraft release is obfuscated, and a
    // programmatic `pluginManager.apply(id)` can only be resolved this way.
    val loomVersion = providers.gradleProperty("loomx.loom_version").getOrElse("1.18.1")
    plugins {
        id("net.fabricmc.fabric-loom") version loomVersion
        id("net.fabricmc.fabric-loom-remap") version loomVersion
        id("dev.kikugie.loom-back-compat") version "0.4.2"
        id("net.neoforged.moddev") version "2.0.147"
        id("net.neoforged.moddev.legacyforge") version "2.0.147"
        // ForgeGradle 7 is `net.minecraftforge:forgegradle`, published on the
        // Gradle Plugin Portal as well as Forge's maven.
        id("net.minecraftforge.gradle") version "7.0.40"
        id("me.modmuss50.mod-publish-plugin") version "2.1.1"
    }

    includeBuild("build-logic")
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("dev.kikugie.stonecutter") version "0.9.8"
    // Applied at the settings level so it can decide which flavour of Fabric
    // Loom a node needs before the branch build script runs.
    id("dev.kikugie.loom-back-compat") version "0.4.2"
}

// Stonecutter node projects are not ordinary subprojects: their buildscript
// classpath is assembled by the controller, and `loom-back-compat` contributes
// the Loom flavour it selected as a *buildscript dependency*. Those are resolved
// against the project's own buildscript repositories, which would otherwise be
// empty, so populate them before any project is configured.
gradle.beforeProject {
    buildscript.repositories.apply {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        maven("https://maven.neoforged.net/releases/") { name = "NeoForged" }
        maven("https://maven.minecraftforge.net/") { name = "MinecraftForge" }
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
    }
}

// ---------------------------------------------------------------------------
// Supported version matrix
// ---------------------------------------------------------------------------
// Every entry below is a Minecraft version the project is *declared* to target.
// A branch only receives the versions its loader actually exists for, which is
// why the lists differ:
//
//   * Fabric    - every version.
//   * NeoForge  - 1.20.6 and newer. 1.20.1 predates NeoForge entirely, 1.20.2
//                 still uses the SimpleChannel stack, 1.20.3 has no NeoForge
//                 release at all, and 1.20.4's registrar API predates
//                 `StreamCodec` so it would need a payload type of its own.
//   * Forge     - see "Not enabled yet" below.
//
// Adding a version is a one-line change here plus a `versions/<mc>/gradle.properties`
// file - see docs/BUILDING.md ("Adding a Minecraft version").

val fabricVersions = listOf("1.20.1", "1.20.2", "1.20.3", "1.20.4", "1.20.5", "1.20.6", "1.21", "1.21.1", "1.21.2", "1.21.3", "1.21.4", "1.21.5", "1.21.6", "1.21.7", "1.21.8", "1.21.9", "1.21.10", "1.21.11", "26.1", "26.1.1", "26.1.2", "26.2", "26.3")
// 1.20.5 is Fabric-only: NeoForge published that release without a
// `moddev-config.json` (only an installer), which ModDevGradle needs, and Forge
// has no 1.20.5 release at all.
val neoforgeVersions = listOf("1.20.6", "1.21", "1.21.1", "1.21.2", "1.21.3", "1.21.4", "1.21.5", "1.21.6", "1.21.7", "1.21.8", "1.21.9", "1.21.10", "1.21.11", "26.1", "26.1.1", "26.1.2", "26.2", "26.3")
// Forge stopped being a first-class target after 1.20.x and the ecosystem moved
// to NeoForge. The list below is every version Forge published that this module
// builds for; 1.21.2 is absent because Forge skipped that release.
//
// 1.20.1 is deliberately absent. Forge 1.20.1 runs on SRG names, so it needs both
// reobfuscation and a Searge mixin refmap; ForgeGradle 7 has neither (it asks for a
// companion "Renamer Gradle" for the first and has no mixin support at all), and
// ForgeGradle 6 - which does have both - is Gradle 8 only, while Loom 1.18.1 needs
// Gradle 9. Adding it means a nested Gradle 8 build, not another node. 1.20.1 still
// ships for Fabric, where it needs none of this.
//
// 1.21.6 moved to EventBus 7, which split `net.minecraftforge.eventbus.api` into
// `bus` and `listener` subpackages and replaced
// FMLJavaModLoadingContext#getModEventBus() with getModBusGroup(), which returns a
// BusGroup. Both halves are handled in DayZInventoryForge. Note that the BusGroup is
// not an IEventBus and IEventBus does not exist in EventBus 7 at all, so the
// bus/register *statement* is conditional as well as the import block.
val forgeVersions = listOf("1.20.6", "1.21", "1.21.1", "1.21.3", "1.21.4", "1.21.5", "1.21.6", "1.21.7", "1.21.8", "1.21.9", "1.21.10", "1.21.11")
val commonVersions = (fabricVersions + neoforgeVersions + forgeVersions).distinct()

// ---------------------------------------------------------------------------
// Not enabled yet
// ---------------------------------------------------------------------------
// These are the only target combinations the matrix above does not cover. Each
// one is missing source-level port work, not build wiring - except Forge 1.20.1,
// which is a toolchain problem. See docs/BUILDING.md section 7 for the detail.
//
//   * 1.20.2 - 1.20.4 (Fabric, NeoForge) - these have `CustomPacketPayload` but
//     not `StreamCodec`, so they sit between the two networking implementations
//     and need a third one. Everything else on those versions is already
//     expressed by the existing conditions.
//
//   * Forge 1.20.1 - Forge runs on SRG names there, so it needs reobfuscation
//     plus a Searge mixin refmap. ForgeGradle 7 has neither, and ForgeGradle 6 -
//     which has both - is Gradle 8 only while Loom 1.18.1 needs Gradle 9.
//     `versions/1.20.1/gradle.properties` already carries the coordinates, so
//     adding "1.20.1" to `forgeVersions` is the build wiring; the toolchain is
//     the blocker.
//
//   * 1.21.2 (Forge) - Forge never published a 1.21.2 release.
//
//   * 26.x (Forge) - Forge's own 26.x line has no EventBus-compatible release
//     this module can target; NeoForge is the supported route there.
//
// Fabric and NeoForge between them cover every version from 1.20.6 upward.

stonecutter {
    create(rootProject) {
        // Only named branches carry nodes here. The root branch is left empty on
        // purpose: it would otherwise create a project per version with no build
        // script and no artifacts, which the controller cannot order publishing
        // tasks for.
        //
        // Per-version dependency coordinates still live in
        // `versions/<mc>/gradle.properties`; the `prop` helpers in build-logic
        // read those files directly.
        branch("common") { versions(*commonVersions.toTypedArray()) }
        branch("fabric") { versions(*fabricVersions.toTypedArray()) }
        branch("forge") { versions(*forgeVersions.toTypedArray()) }
        branch("neoforge") { versions(*neoforgeVersions.toTypedArray()) }
    }
}

rootProject.name = "dayz-inventory"
