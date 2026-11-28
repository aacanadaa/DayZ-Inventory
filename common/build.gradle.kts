/*
 * DayZ Inventory - shared (loader-agnostic) module
 * Copyright 2026 suoim
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * This build script runs once per Minecraft version - once for `:common:1.20.1`,
 * once for `:common:1.21.1`, and so on. `mc` is the Minecraft version of the
 * node currently being configured, and `versions/<mc>/gradle.properties`
 * supplies the dependency coordinates for it.
 *
 * `dev.kikugie.loom-back-compat` picks the right flavour of Fabric Loom for the
 * node: `fabric-loom-remap` for the obfuscated releases (< 26.1) and
 * `fabric-loom` for the unobfuscated ones (>= 26.1). It also aliases the `mod*`
 * configurations that the unobfuscated Loom removed, so the same lines work on
 * both sides of that split.
 */

plugins {
    id("dayz-common")
    id("dev.kikugie.loom-back-compat")
}

dependencies {
    minecraft("com.mojang:minecraft:$mc")

    // A no-op on the unobfuscated Loom; below 26.1 this installs the official
    // Mojang mappings that every source file in this repo is written against.
    loomx.applyMojangMappings()

    // Loader-neutral code still needs these two on the compile classpath for
    // the handful of API types the platform abstraction mentions.
    modCompileOnly("net.fabricmc:fabric-loader:${prop("deps.fabric-loader")}")
    modCompileOnly("net.fabricmc.fabric-api:fabric-api:${prop("deps.fabric-api")}")
}

// ---------------------------------------------------------------------------
// Mixin refmaps
// ---------------------------------------------------------------------------
// Below 26.1 Loom's annotation processor generates a refmap that rewrites the
// official (Mojang) selectors in `@Mixin` into Fabric's intermediary names - the
// names Fabric actually runs with. The mixin configs declare
// "refmap": "dayz-inventory.refmap.json", so the generated file has to carry
// exactly that name or every mixin is silently left unmapped and fails.
//
// From 26.1 there is nothing to remap: Minecraft is unobfuscated, so no refmap
// is produced and the config's refmap entry is harmless.
if (sc.current.parsed < "26") {
    loom {
        mixin {
            // Loom stopped enabling the Mixin annotation processor by default;
            // without it no refmap is produced and every mixin would silently
            // keep its official-named selectors, which Fabric cannot resolve.
            useLegacyMixinAp.set(true)
            defaultRefmapName.set("dayz-inventory.refmap.json")
        }
    }
}

// Loom's bundled Mixin conflicts with the one each loader ships; the loaders
// provide it at runtime, so keep it off our runtime classpath.
configurations {
    named("runtimeClasspath") { exclude(group = "net.fabricmc", module = "sponge-mixin") }
    named("runtimeElements") { exclude(group = "net.fabricmc", module = "sponge-mixin") }
}

// ---------------------------------------------------------------------------
// Unit tests for the item size grid
// ---------------------------------------------------------------------------
// `com.suoim.dayzinventory.grid` is plain Java with no Minecraft types, so its
// tests compile that package alone instead of the whole of `common` (which CI
// never builds on its own: the loader nodes compile these sources themselves).
// They run once per build, on the active node - every CI job configures that
// node anyway - and `buildMods` depends on them.
if (isActiveNode) {
    val gridTestLibraries = configurations.create("gridTestLibraries")
    dependencies {
        gridTestLibraries(platform("org.junit:junit-bom:5.14.5"))
        gridTestLibraries("org.junit.jupiter:junit-jupiter")
        gridTestLibraries("org.junit.platform:junit-platform-launcher")
    }

    val gridTestSources = extensions.getByType<SourceSetContainer>().create("gridTest") {
        java.setSrcDirs(listOf(
            rootProject.file("common/src/main/java/com/suoim/dayzinventory/grid"),
            rootProject.file("common/src/gridTest/java"),
        ))
        compileClasspath = gridTestLibraries
        runtimeClasspath = output + gridTestLibraries
    }

    val gridTest = tasks.register<Test>("gridTest") {
        group = "verification"
        description = "Runs the unit tests of the item size grid"
        testClassesDirs = gridTestSources.output.classesDirs
        classpath = gridTestSources.runtimeClasspath
        useJUnitPlatform()
        testLogging {
            events("failed")
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
    }

    rootProject.tasks.named("buildMods") { dependsOn(gridTest) }
}
