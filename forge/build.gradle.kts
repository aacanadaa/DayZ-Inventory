/*
 * DayZ Inventory - Forge module
 * Copyright 2026 suoim
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * This build script runs once per Minecraft version that has a Forge node -
 * currently 1.21.1. 1.20.1 is a Forge target too but is not in the matrix yet;
 * see docs/BUILDING.en.md section 7.
 *
 * Built with ForgeGradle 7. The two alternatives were tried first and neither
 * works here:
 *
 *   * ModDevGradle's `legacyforge` asks for `net.minecraftforge:forge:<v>:universal-srg`.
 *     That classifier only exists for the pre-1.20.2 SRG layout, so it cannot
 *     build 1.21.1 at all.
 *   * ForgeGradle 6 only supports Gradle 8, and this build needs Gradle 9 for
 *     the Loom version the newer nodes use.
 *
 * ForgeGradle 7 is the rewrite that runs on Gradle 9. It resolves Forge through
 * its own mavenizer rather than publishing a `minecraft` configuration.
 *
 * No reobfuscation and no Searge refmap: Forge has run on official Mojang names
 * since 1.20.2, so the mixin selectors in the shared sources resolve as written.
 */

plugins {
    id("dayz-loader")
    id("net.minecraftforge.gradle")
}

// ForgeGradle's mavenizer resolves Minecraft and Forge into a build-local maven
// repository under `.gradle/mavenizer/repo`. That repository has to be on this
// project's list: ForgeGradle is stateless by default and does not add it once
// another plugin has already declared repositories, which `dayz-common` has.
// Without this the Forge dependency resolves to an empty module and every
// `net.minecraft.*` import fails.
repositories {
    minecraft.mavenizer(this)
    // Mojang's own library repository (`com.mojang:patchy`, `text2speech`, ...),
    // which the mavenized Forge POM depends on. ForgeGradle adds it itself under
    // "Magic", but Magic backs off once another plugin has declared repositories
    // and `dayz-common` has, so it is named here instead.
    maven("https://libraries.minecraft.net/") { name = "Minecraft libraries" }
}

minecraft {
    // The shared sources are written against official Mojang names, which is
    // what Forge has run on since 1.20.2.
    mappings("official", mc)
}

dependencies {
    // ForgeGradle 7 is stateless: `minecraft.dependency(...)` resolves the
    // Forge coordinate through its own mavenizer and hands back something
    // declarable as a normal dependency. Nothing is on the classpath until this
    // line exists - without it the plugin configures cleanly and every
    // `net.minecraft.*` import then fails to resolve.
    implementation(minecraft.dependency("net.minecraftforge:forge:$mc-${prop("deps.forge")}"))
}

// Forge finds mixin configs through the `MixinConfigs` manifest attribute. It
// does not understand NeoForge's `[[mixins]]` blocks in mods.toml, which is why
// neoforge/ declares them there and this module cannot.
tasks.named<Jar>("jar") {
    manifest {
        attributes(
            "MixinConfigs" to "dayz-inventory.mixins.json,dayz-inventory.client.mixins.json"
        )
    }
}
