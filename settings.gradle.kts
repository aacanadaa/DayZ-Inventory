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
 * The version matrix itself is not written down here: it is read from the
 * `versions/<mc>/gradle.properties` files (see "The version matrix" below).
 */

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        maven("https://maven.neoforged.net/releases/") { name = "NeoForged" }
        maven("https://maven.minecraftforge.net/") { name = "MinecraftForge" }
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
    }

    // Plugin versions live in the root gradle.properties ("Toolchain" section) so
    // there is exactly one place to bump them.
    //
    // The two Loom ids are listed even though no build script asks for them by
    // name: `dev.kikugie.loom-back-compat` applies one of them programmatically
    // depending on whether the node's Minecraft release is obfuscated, and a
    // programmatic `pluginManager.apply(id)` can only be resolved this way.
    val loomVersion = providers.gradleProperty("loomx.loom_version").get()
    val moddevVersion = providers.gradleProperty("moddev.version").get()
    val forgeGradleVersion = providers.gradleProperty("forgegradle.version").get()
    plugins {
        id("net.fabricmc.fabric-loom") version loomVersion
        id("net.fabricmc.fabric-loom-remap") version loomVersion
        id("dev.kikugie.loom-back-compat") version "0.4.3"
        // `moddev` builds NeoForge; `moddev.legacyforge` builds Forge 1.20.1,
        // the last Forge release that still runs on SRG names.
        id("net.neoforged.moddev") version moddevVersion
        id("net.neoforged.moddev.legacyforge") version moddevVersion
        // ForgeGradle 7 builds every Forge node from 1.20.6 on.
        id("net.minecraftforge.gradle") version forgeGradleVersion
        id("me.modmuss50.mod-publish-plugin") version "2.2.1"
    }

    includeBuild("build-logic")
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("dev.kikugie.stonecutter") version "0.9.8"
    // Applied at the settings level so it can decide which flavour of Fabric
    // Loom a node needs before the branch build script runs.
    id("dev.kikugie.loom-back-compat") version "0.4.3"
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
// The version matrix
// ---------------------------------------------------------------------------
// Every Minecraft version is a directory under `versions/`, and its
// `gradle.properties` is the only place that version is described - including
// which loaders it is built for:
//
//     loaders=fabric,neoforge,forge
//
// Adding a Minecraft version is therefore one new directory, and CI discovers it
// the same way (it lists `versions/`). Why a loader is missing on a given
// version is recorded in docs/BUILDING.md, "Enabled targets".

/** Numeric, segment-by-segment order: 1.21.10 sorts after 1.21.9. */
val minecraftVersionOrder: Comparator<String> = Comparator { a, b ->
    val left = a.split('.').map { it.toIntOrNull() ?: 0 }
    val right = b.split('.').map { it.toIntOrNull() ?: 0 }
    (0 until maxOf(left.size, right.size))
        .map { i -> left.getOrElse(i) { 0 }.compareTo(right.getOrElse(i) { 0 }) }
        .firstOrNull { it != 0 } ?: 0
}

val knownLoaders = setOf("fabric", "neoforge", "forge")

/** Minecraft version -> the loaders it is built for, in version order. */
val matrix: Map<String, List<String>> = run {
    val dirs = settingsDir.resolve("versions").listFiles()
        ?.filter { java.io.File(it, "gradle.properties").isFile }
        ?: error("versions/ is missing - every Minecraft version needs versions/<mc>/gradle.properties")
    dirs.associate { dir ->
        val props = java.util.Properties().apply {
            java.io.File(dir, "gradle.properties").reader(Charsets.UTF_8).use { load(it) }
        }
        val loaders = props.getProperty("loaders")
            ?.split(',')?.map { it.trim() }?.filter { it.isNotEmpty() }
            ?: error("versions/${dir.name}/gradle.properties has no 'loaders=' entry")
        val unknown = loaders - knownLoaders
        require(unknown.isEmpty()) { "versions/${dir.name}: unknown loader(s) $unknown" }
        dir.name to loaders
    }.toSortedMap(minecraftVersionOrder)
}

// The version the shared sources are currently written for. It is read from the
// controller so it has a single source: Stonecutter rewrites that line when the
// active version is switched.
val activeVersion: String = Regex("""stonecutter\s+active\s+"([^"]+)"""")
    .find(settingsDir.resolve("stonecutter.gradle.kts").readText())
    ?.groupValues?.get(1)
    ?: error("stonecutter.gradle.kts does not declare `stonecutter active \"<version>\"`")
require(activeVersion in matrix) { "The active version $activeVersion has no versions/$activeVersion directory" }

// `-Pdayz.versions=1.21.1,26.2` limits the tree to those Minecraft versions.
// CI builds one version per job this way: configuring a node downloads and
// prepares that Minecraft release, so a job that configured all of them would
// spend most of its time and disk on versions it never builds.
val requested: Set<String>? = providers.gradleProperty("dayz.versions").orNull
    ?.split(',')?.map { it.trim() }?.filter { it.isNotEmpty() }?.toSet()
requested?.let { wanted ->
    val unknown = wanted - matrix.keys
    require(unknown.isEmpty()) { "dayz.versions names unknown Minecraft version(s): $unknown" }
}
val selected = if (requested == null) matrix else matrix.filterKeys { it in requested }

fun versionsFor(loader: String): List<String> = selected.filterValues { loader in it }.keys.toList()

// `common` gets every selected version, plus the active one: the controller's
// `stonecutter active` line has to name a version that exists in the tree even
// when only a subset is being built.
val commonVersions = (selected.keys + activeVersion).distinct().sortedWith(minecraftVersionOrder)

stonecutter {
    create(rootProject) {
        // Only named branches carry nodes here. The root branch is left empty on
        // purpose: it would otherwise create a project per version with no build
        // script and no artifacts, which the controller cannot order publishing
        // tasks for.
        branch("common") { versions(*commonVersions.toTypedArray()) }
        // A subset build can leave a loader with nothing to build (1.20.2 has no
        // NeoForge or Forge node), and an empty branch is left out entirely.
        for (loader in listOf("fabric", "forge", "neoforge")) {
            val loaderVersions = versionsFor(loader)
            if (loaderVersions.isNotEmpty()) {
                branch(loader) { versions(*loaderVersions.toTypedArray()) }
            }
        }
    }
}

rootProject.name = "dayz-inventory"
