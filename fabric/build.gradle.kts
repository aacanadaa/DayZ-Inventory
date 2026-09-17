/*
 * DayZ Inventory - Fabric module
 * Copyright 2026 suoim
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * This build script runs once per Minecraft version. `:common:<mc>`'s processed
 * sources are compiled straight into this project by `dayz-loader`, so this jar
 * is self-contained: one copy of every class and every mixin config.
 */

plugins {
    id("dayz-loader")
    id("dev.kikugie.loom-back-compat")
}

// The client entrypoint lives under `src/main/java`, NOT a separate `src/client`
// root. `dayz-loader` applies the `dayz-common` convention plugin *during plugin
// application*, which runs `sc.tasks.configureSource(mainSourceSet)` and then
// points `compileJava` at the generated tree only. A source root added here in
// the script body is therefore never preprocessed and never compiled - which is
// exactly how the client entrypoint went missing from every Fabric jar while
// `fabric.mod.json` still declared it, making the mod fail to load. Keeping every
// source under `src/main/java` means there is nothing to miss. Client-only safety
// comes from the `"environment": "client"` marker on the entrypoint, not from the
// source root.

dependencies {
    minecraft("com.mojang:minecraft:$mc")
    loomx.applyMojangMappings()

    modImplementation("net.fabricmc:fabric-loader:${prop("deps.fabric-loader")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${prop("deps.fabric-api")}")
}

if (sc.current.parsed < "26") {
    loom {
        mixin {
            useLegacyMixinAp.set(true)
            defaultRefmapName.set("dayz-inventory.refmap.json")
        }
    }
}

// Run configurations are only meaningful for the node the working tree is
// currently chiselled to; Gradle cannot generate IDE run configs for several
// versions of the same project at once.
if (isActiveNode) {
    loom {
        runs {
            named("client") {
                client()
                ideConfigGenerated(true)
                runDir("../../run")
                configName = "Fabric Client ($mc)"
            }
            named("server") {
                server()
                ideConfigGenerated(true)
                runDir("../../run")
                configName = "Fabric Server ($mc)"
            }
        }
    }
}

configurations {
    named("runtimeClasspath") { exclude(group = "net.fabricmc", module = "sponge-mixin") }
}
