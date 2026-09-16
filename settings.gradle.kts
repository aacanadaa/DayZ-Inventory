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
//   * NeoForge  - 1.21.1 and newer (NeoForge did not exist for 1.20.1).
//   * Forge     - see "Not enabled yet" below.
//
// Adding a version is a one-line change here plus a `versions/<mc>/gradle.properties`
// file - see docs/BUILDING.md ("Adding a Minecraft version").

val fabricVersions = listOf("1.21.1", "1.21.11", "26.2")
val neoforgeVersions = listOf("1.21.1", "1.21.11", "26.2")
val commonVersions = (fabricVersions + neoforgeVersions).distinct()

// ---------------------------------------------------------------------------
// Not enabled yet
// ---------------------------------------------------------------------------
// These targets are still shipped from their own branches and are *not*
// regressed by this tree - they simply have not been folded in yet. Every one of
// them already has a `versions/<mc>/gradle.properties` and a branch build script;
// what is missing is source-level port work. See docs/BUILDING.md for the exact
// API breakpoints that remain.
//
//   * 1.20.1 (Fabric, Forge) - pre-1.20.5 networking: no custom payload records,
//     so `DayZInventoryPayload`/`DayZInventoryOpenData` do not exist and the
//     `ExtendedScreenHandlerType` opening-data codec has to be replaced by
//     `writeScreenOpeningData`. Still built from the `main` branch.
//
//   * Forge (1.20.1, 1.21.1) - `forge/` still holds the 1.20.1-era
//     `SimpleChannel`/`NetworkRegistry` networking. Porting it to the payload
//     model is the same work the NeoForge module already had done, against
//     `net.minecraftforge` rather than `net.neoforged` packages.
//
// Uncommenting either line below is all the build wiring that is needed.
//
// val oldFabricVersions = listOf("1.20.1")
// val forgeVersions = listOf("1.20.1", "1.21.1")

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
        branch("neoforge") { versions(*neoforgeVersions.toTypedArray()) }
    }
}

rootProject.name = "dayz-inventory"
