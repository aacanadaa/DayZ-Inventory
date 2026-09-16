/*
 * DayZ Inventory - NeoForge module
 * Copyright 2026 suoim
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * This build script runs once per Minecraft version supported on NeoForge.
 *
 * No refmap handling is needed here: NeoForge has run on official Mojang names
 * since 1.20.2, so mixin selectors written in official names resolve as written.
 */

plugins {
    id("dayz-loader")
    id("net.neoforged.moddev")
}

neoForge {
    version = prop("deps.neoforge")

    // Run configurations are only generated for the node the tree is currently
    // chiselled to; several nodes cannot own the same IDE run config.
    if (isActiveNode) {
        runs {
            register("client") {
                client()
                gameDirectory = rootProject.file("run")
                ideName = "NeoForge Client ($mc)"
                programArgument("--username=Dev")
            }
            register("server") {
                server()
                gameDirectory = rootProject.file("run")
                ideName = "NeoForge Server ($mc)"
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
    // NeoForge itself comes from the moddev plugin - no coordinate needed.
}

tasks.named("createMinecraftArtifacts") {
    dependsOn(":common:$mcNode:classes")
}
