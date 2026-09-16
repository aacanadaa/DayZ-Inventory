/*
 * DayZ Inventory - build logic
 * Copyright 2026 suoim
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://maven.fabricmc.net/") { name = "Fabric" }
    maven("https://maven.neoforged.net/releases/") { name = "NeoForged" }
    maven("https://maven.minecraftforge.net/") { name = "MinecraftForge" }
    maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
    maven("https://maven.parchmentmc.org") { name = "ParchmentMC" }
}

dependencies {
    // Stonecutter's Gradle API, so the convention plugins can read the current
    // node and emit swaps/constants.
    implementation("dev.kikugie:stonecutter:0.9.8")
    // us.modmuss50's Mod Publish Plugin (Modrinth + CurseForge + GitHub + Discord).
    implementation("me.modmuss50:mod-publish-plugin:2.1.1")
}
