/*
 * DayZ Inventory - Forge module
 * Copyright 2026 suoim
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * This build script runs for the Minecraft versions that still have a Forge
 * branch - 1.20.1 and 1.21.1. Forge carries no further than 1.20.x as a
 * first-class target and the ecosystem moved to NeoForge, which is why 1.21.2+
 * has no `forge/` node at all.
 *
 * Built with ModDevGradle's LegacyForge platform, which replaces the old
 * ForgeGradle + MixinGradle pair: it supplies the official mappings this code is
 * written against, reobfuscates the release jar to SRG, and generates the mixin
 * refmap that Forge needs.
 */

plugins {
    id("dayz-loader")
    id("net.neoforged.moddev.legacyforge")
}

legacyForge {
    // e.g. `1.20.1-47.2.0`
    version = "$mc-${prop("deps.forge")}"

    if (isActiveNode) {
        runs {
            register("client") {
                client()
                gameDirectory = rootProject.file("run")
                ideName = "Forge Client ($mc)"
                programArgument("--username=Dev")
            }
            register("server") {
                server()
                gameDirectory = rootProject.file("run")
                ideName = "Forge Server ($mc)"
            }
        }
    }

    mods {
        register(prop("mod.id")) {
            sourceSet(sourceSets["main"])
        }
    }
}

dependencies {
    // LegacyForge resolves members against SRG at runtime, so the mixin
    // annotation processor has to run to turn the official-named selectors in
    // this repo into a Searge refmap.
    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
}

tasks.named("createMinecraftArtifacts") {
    dependsOn(":common:$mcNode:classes")
}
