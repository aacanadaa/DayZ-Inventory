/*
 * DayZ Inventory - `dayz-loader` convention plugin
 * Copyright 2026 suoim
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Applied to the loader branches (fabric / forge / neoforge).
 *
 * Two jobs:
 *   1. compile the matching `:common:<mc>` node's processed sources straight
 *      into this jar, so there is exactly one copy of every shared class and
 *      one copy of every mixin config on the mod classpath;
 *   2. publish the resulting jar to Modrinth and CurseForge with the correct
 *      game version + loader tags - no manual tagging per release.
 */

import me.modmuss50.mpp.ModPublishExtension
import me.modmuss50.mpp.ReleaseType
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register

plugins {
    id("dayz-common")
    id("me.modmuss50.mod-publish-plugin")
}

// ---------------------------------------------------------------------------
// Share the `common` node's sources
// ---------------------------------------------------------------------------
// The loader jar compiles the common node's sources directly rather than
// depending on its jar. That matters for more than tidiness: on the obfuscated
// Minecraft releases (< 26.1) Fabric and Forge need a *mixin refmap*, and a
// refmap can only be produced by running the Mixin annotation processor over the
// annotated sources. Handing this project a pre-compiled `common.jar` would give
// it classes Mixin can never remap, and every mixin would fail at runtime.
//
// The source directories are read from the common node once it has finished
// evaluating, because Stonecutter only finalises them (pointing at the
// version-processed copies for non-active nodes) at that point.
val commonPath = ":common:$mcNode"
val commonProject = project(commonPath)

// The common node writes its version-processed tree to
// `:common:<mc>` -> build/generated/stonecutter/main/{java,resources}.
// Compiling those directories directly is what keeps a single copy of every
// shared class (and of every shared mixin config) in the loader jar.
val commonGeneratedJava = commonProject.layout.buildDirectory.dir("generated/stonecutter/main/java")
val commonGeneratedResources = commonProject.layout.buildDirectory.dir("generated/stonecutter/main/resources")

tasks.named<JavaCompile>("compileJava") {
    // Added to, rather than replacing, the source list that `dayz-common` set to
    // this node's own generated tree.
    source(commonGeneratedJava)
    dependsOn("$commonPath:stonecutterGenerate")
}

tasks.named<ProcessResources>("processResources") {
    from(commonGeneratedResources)
    dependsOn("$commonPath:stonecutterGenerate")
}

// ---------------------------------------------------------------------------
// Publishing
// ---------------------------------------------------------------------------
val loaderName = when (branch) {
    "neoforge" -> "neoforge"
    "forge" -> "forge"
    else -> "fabric"
}

// Fabric is remapped below 26.1 and used as-is from 26.1 on, where Mojang
// stopped obfuscating and Loom dropped `remapJar` entirely. NeoForge and Forge
// both take the plain `jar`.
val modJarTaskName = if (tasks.names.contains("remapJar")) "remapJar" else "jar"

val modrinthToken = firstEnv("MODRINTH_TOKEN", "MODRINTH_PAT")
val curseforgeToken = firstEnv("CURSEFORGE_API_KEY", "CURSEFORGE_TOKEN")

// A real publish with no token fails deep inside the plugin with an auth error
// that does not say which variable is missing. Say it here instead, once per
// node, before anything is uploaded.
val publishingForReal = (propOrNull("publish.dry_run")?.toBoolean() ?: true).not()
if (publishingForReal) {
    if (!modrinthToken.isPresent) {
        logger.warn("[dayz] publish.dry_run=false but neither MODRINTH_TOKEN nor MODRINTH_PAT is set - Modrinth uploads will fail")
    }
    if (!curseforgeToken.isPresent) {
        logger.warn("[dayz] publish.dry_run=false but neither CURSEFORGE_API_KEY nor CURSEFORGE_TOKEN is set - CurseForge uploads will fail")
    }
}

extensions.configure<ModPublishExtension>("publishMods") {
    // The Minecraft version is part of the version number on purpose: Modrinth
    // keys a version on its number, and the same mod version ships for several
    // game versions into the same project.
    version.set("${prop("mod.version")}+$mc")
    displayName.set("${prop("mod.name")} ${prop("mod.version")} for MC $mc")
    changelog.set(rootProject.file("CHANGELOG.md").let { if (it.exists()) it.readText() else "" })
    file.set(tasks.named<Jar>(modJarTaskName).flatMap { it.archiveFile })

    type.set(ReleaseType.STABLE)
    // CurseForge puts every upload through human review, so a publish there is
    // fire-and-forget: the API accepts the file and never replies with a URL.
    // Keeping the default as a dry run means a stray `publishMods` during
    // development cannot accidentally submit a review.
    dryRun.set(propOrNull("publish.dry_run")?.toBoolean() ?: true)

    modrinth {
        projectId.set(prop("modrinth.id"))
        // `MODRINTH_PAT` is accepted as an alias because the CI secret has been
        // named both ways.
        accessToken.set(modrinthToken)
        minecraftVersions.add(mc)
        modLoaders.add(loaderName)
        if (loaderName == "fabric") requires("fabric-api")
        optional("jei")
        optional("rei")
        optional("emi")
        if (loaderName == "fabric") {
            optional("trinkets")
            optional("modmenu")
        } else {
            optional("curios")
        }
    }

    curseforge {
        projectId.set(prop("curseforge.id"))
        projectSlug.set(prop("curseforge.slug"))
        // `CURSEFORGE_TOKEN` is kept as a fallback for the pre-existing CI
        // secret; `CURSEFORGE_API_KEY` is the documented name.
        accessToken.set(curseforgeToken)
        minecraftVersions.add(mc)
        modLoaders.add(loaderName)
        if (loaderName == "fabric") requires("fabric-api")
        optional("jei")
        if (loaderName == "fabric") optional("trinkets") else optional("curios")
        client.set(true)
        server.set(true)
    }
}

tasks.register("publishMod") {
    group = "publishing"
    description = "Publishes this node to Modrinth and CurseForge"
    dependsOn("publishMods")
}
