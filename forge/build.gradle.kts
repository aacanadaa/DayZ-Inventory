/*
 * DayZ Inventory - Forge module
 * Copyright 2026 suoim
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * This build script runs once per Minecraft version that has a Forge node.
 * Two toolchains are needed, split at the point where Forge stopped running on
 * SRG names:
 *
 *   * 1.20.1 - ModDevGradle `legacyforge`. Forge 1.20.1 runs on SRG names, so
 *     the jar has to be reobfuscated and the mixins need a Searge refmap. MDG
 *     Legacy does both on Gradle 9: `reobfJar` writes the shippable jar to
 *     build/libs (the official-named one moves to build/devlibs), and the Mixin
 *     annotation processor writes the refmap.
 *
 *   * 1.20.6 and newer - ForgeGradle 7. Forge runs on official Mojang names
 *     there, so there is no reobfuscation and no refmap. (MDG Legacy cannot
 *     build these: it asks for the `universal-srg` classifier that only the
 *     pre-1.20.2 SRG layout publishes.)
 *
 * Both plugins are declared `apply false` so that their classes are on this
 * script's classpath, and exactly one of them is applied below.
 */

import net.minecraftforge.gradle.MinecraftExtensionForProject
import net.neoforged.moddevgradle.legacyforge.dsl.LegacyForgeExtension
import net.neoforged.moddevgradle.legacyforge.dsl.MixinExtension

plugins {
    id("dayz-loader")
    id("net.minecraftforge.gradle") apply false
    id("net.neoforged.moddev.legacyforge") apply false
}

val forgeVersion = prop("deps.forge")
val mixinConfigs = listOf(
    "dayz-inventory.mixins.json",
    "dayz-inventory.client.mixins.json",
    "dayz-inventory.compat.mixins.json",
)

repositories {
    // Mojang's own library repository (`com.mojang:patchy`, `text2speech`, ...),
    // which both toolchains' Minecraft dependencies reference.
    maven("https://libraries.minecraft.net/") { name = "Minecraft libraries" }
}

if (isLegacyForge) {
    apply(plugin = "net.neoforged.moddev.legacyforge")

    extensions.configure<LegacyForgeExtension>("legacyForge") {
        // An explicit call: the extension has no `version` getter, so the
        // property syntax would silently resolve to Project#version instead.
        this.setVersion("$mc-$forgeVersion")
    }

    // The refmap name has to match the `"refmap"` entry `dayz-loader` writes
    // into the mixin configs for this node.
    extensions.configure<MixinExtension>("mixin") {
        add(sourceSets["main"], "dayz-inventory.refmap.json")
        mixinConfigs.forEach { config(it) }
    }

    dependencies {
        annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
    }
} else {
    apply(plugin = "net.minecraftforge.gradle")
    val minecraft = extensions.getByName("minecraft") as MinecraftExtensionForProject

    // ForgeGradle's mavenizer resolves Minecraft and Forge into a build-local
    // maven repository. ForgeGradle is stateless and does not add that
    // repository itself once another plugin has declared repositories (which
    // `dayz-common` has), so it is added here. Without it the Forge dependency
    // resolves to an empty module and every `net.minecraft.*` import fails,
    // which reads as a source problem and is not one.
    repositories {
        minecraft.mavenizer(this)
    }

    // The shared sources are written against official Mojang names, which is
    // what Forge has run on since 1.20.2.
    minecraft.mappings("official", mc)

    dependencies {
        implementation(minecraft.dependency("net.minecraftforge:forge:$mc-$forgeVersion"))
    }
}

// Forge finds mixin configs through the `MixinConfigs` manifest attribute on
// every version. It does not understand NeoForge's `[[mixins]]` blocks in
// mods.toml, which is why neoforge/ declares them there and this module cannot.
// MDG Legacy's `reobfJar` copies the manifest from this jar.
tasks.named<Jar>("jar") {
    manifest {
        attributes("MixinConfigs" to mixinConfigs.joinToString(","))
    }
}
