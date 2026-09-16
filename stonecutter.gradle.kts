/*
 * DayZ Inventory - Stonecutter controller script
 * Copyright 2026 suoim
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * This file is the Stonecutter *controller*: it runs once for the whole tree,
 * unlike the per-branch `build.gradle.kts` files which run once per node.
 */

plugins {
    id("dev.kikugie.stonecutter")

    // Declared here with `apply false` so Gradle puts every mod-loader plugin on
    // the buildscript classpath that the Stonecutter node projects inherit.
    // Without this a node that applies Loom through `loom-back-compat` fails to
    // resolve it, because a programmatic `pluginManager.apply(id)` looks at the
    // project's buildscript repositories rather than at pluginManagement.
    id("net.fabricmc.fabric-loom") apply false
    id("net.fabricmc.fabric-loom-remap") apply false
    id("dev.kikugie.loom-back-compat") apply false
    id("net.neoforged.moddev") apply false
    id("net.neoforged.moddev.legacyforge") apply false
    id("net.minecraftforge.gradle") apply false
    id("me.modmuss50.mod-publish-plugin") apply false
}

// The version the shared `src/` directories are preprocessed for right now.
// Change it with `./gradlew "Set active project to <version>"`, or by running
// any task through the `stonecutter` task group.
stonecutter active "26.2"

// Every loader node, addressed by task *path* rather than by task provider.
// The controller script runs before the branch build scripts do, so the
// `publishMods` tasks it publishes do not exist yet at this point - only their
// paths can be named here, and Gradle resolves those when the graph is built.
// The `common` branch is skipped: it has nothing to publish.
val publishTargets = stonecutter.tree.branches
    .filter { it.id != "common" }
    .flatMap { branch -> branch.keys.map { version -> ":${branch.id}:$version:publishMods" } }

// ---------------------------------------------------------------------------
// Aggregate tasks
// ---------------------------------------------------------------------------

/**
 * Builds every loader for every version in the matrix in one invocation.
 *
 * This is the task to reach for in CI and in the README:
 * `./gradlew chiseledBuild`
 */
tasks.register("chiseledBuild") {
    group = "build"
    description = "Builds every Minecraft version and mod loader in the matrix"
    dependsOn(stonecutter.tasks.named("build").get().values)
}

/**
 * Publishes every built jar to Modrinth and CurseForge, tagging each artifact
 * with the exact game version and loader it was compiled for.
 *
 * Publishing is a dry run unless `-Ppublish.dry_run=false` (or the same property
 * in `gradle.properties`) is set, so this cannot submit anything by accident.
 */
tasks.register("publishAll") {
    group = "publishing"
    description = "Publishes every node in the matrix to Modrinth and CurseForge"
    dependsOn(publishTargets)
}

// Per-platform aggregates, so one platform can be re-run on its own. That
// matters after a partial upload: Modrinth rejects a second file with a name it
// already has, so re-running `publishAll` after a CurseForge-only failure would
// fail on every node that already went up.
fun platformTasks(suffix: String) = stonecutter.tree.branches
    .filter { it.id != "common" }
    .flatMap { branch -> branch.keys.map { version -> ":${branch.id}:$version:$suffix" } }

tasks.register("publishModrinthAll") {
    group = "publishing"
    description = "Publishes every node in the matrix to Modrinth"
    dependsOn(platformTasks("publishModrinth"))
}

tasks.register("publishCurseforgeAll") {
    group = "publishing"
    description = "Publishes every node in the matrix to CurseForge"
    dependsOn(platformTasks("publishCurseforge"))
}

// CurseForge accepts one upload at a time gracefully; a burst comes back as a
// rate-limit error rather than being queued, so the nodes are ordered.
//
// This is deliberately *not* inside the `publishAll` registration block:
// querying the task container (`tasks.matching { ... }.configureEach { ... }`)
// while another task is being created throws
// "DefaultTaskCollection#configureEach(Action) on task set cannot be executed in
// the current context". At the top level of the controller script the same call
// is fine, and `configureEach` still applies lazily to tasks created later.
publishTargets.zipWithNext { first, second ->
    tasks.configureEach {
        if (path == second) mustRunAfter(first)
    }
}

/** Prints the version/loader matrix, one `path = version` pair per line. */
tasks.register("matrix") {
    group = "help"
    description = "Prints every node in the Stonecutter tree"
    doLast {
        stonecutter.versions
            .sortedBy { it.project }
            .forEach { println("${it.project} = ${it.version}") }
    }
}
