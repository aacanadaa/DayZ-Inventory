/*
 * DayZ Inventory - shared build-script helpers
 * Copyright 2026 suoim
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 */

import dev.kikugie.stonecutter.build.StonecutterBuildExtension
import java.util.concurrent.ConcurrentHashMap
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.maven

/** Stonecutter's per-node build extension. */
val Project.sc: StonecutterBuildExtension get() = extensions.getByType<StonecutterBuildExtension>()

/**
 * The Minecraft version this node builds for, e.g. `26.2`.
 *
 * Stonecutter calls this the node's *version* (the value used when evaluating
 * `//? if` comments), as opposed to [mcNode] which is the directory name under
 * `<branch>/versions/`.
 */
val Project.mc: String get() = sc.current.version

/** The node's project name / directory name, e.g. `26.2`. */
val Project.mcNode: String get() = sc.current.project

/** True when this node is the one the working tree is currently "chiselled" to. */
val Project.isActiveNode: Boolean get() = sc.current.isActive

/** The module this node belongs to: `common`, `fabric`, `forge` or `neoforge`. */
val Project.branch: String get() = sc.node.branch.id

/** The mod's user-facing slug, e.g. `dayz-inventory`. */
val Project.modSlug: String get() = prop("mod.id").replace('_', '-')

// ---------------------------------------------------------------------------
// Per-version properties
// ---------------------------------------------------------------------------
// Dependency coordinates live in one file per Minecraft version at
// `versions/<mc>/gradle.properties`, so adding a game version means adding one
// file rather than touching every branch.
//
// The Stonecutter tree is rooted at the root project, so those files sit next to
// the root `settings.gradle.kts`; a branch node's own project directory is
// `<branch>/versions/<mc>/`, which is *not* where they live. Gradle only merges
// a project's own `gradle.properties` with the root one, so the branch nodes
// would never see them. Reading the file explicitly - instead of relying on
// Gradle's property merging - keeps a single copy per version and makes the
// lookup identical for every branch.
private val versionPropertiesCache = ConcurrentHashMap<String, Map<String, String>>()

private fun Project.versionProperties(): Map<String, String> {
    val node = mcNode
    return versionPropertiesCache.getOrPut("${rootProject.projectDir}/$node") {
        val candidates = listOf(
            rootProject.file("versions/$node/gradle.properties"),
            file("versions/$node/gradle.properties"),
        )
        val source = candidates.firstOrNull { it.isFile } ?: return@getOrPut emptyMap()
        java.util.Properties().apply {
            source.reader(Charsets.UTF_8).use { load(it) }
        }.entries.associate { (key, value) -> key.toString() to value.toString() }
    }
}

fun Project.propOrNull(key: String): String? =
    findProperty(key)?.toString()?.takeIf { it.isNotBlank() }
        ?: versionProperties()[key]?.takeIf { it.isNotBlank() }

fun Project.prop(key: String): String =
    propOrNull(key) ?: error("Missing required property '$key' for project $path")

/** Adds a maven repository that may only serve the given groups. */
fun RepositoryHandler.strictMaven(url: String, alias: String, vararg groups: String) =
    exclusiveContent {
        forRepository { maven(url) { name = alias } }
        filter { groups.forEach { includeGroup(it) } }
    }

/**
 * The first non-blank environment variable among [names], or an absent provider.
 *
 * `providers.environmentVariable(name)` reports *present* for a variable that is
 * defined but empty, and `Provider.orElse` only falls back when the receiver is
 * absent - so an empty `CURSEFORGE_API_KEY` silently shadows a populated
 * `CURSEFORGE_TOKEN`. CI forwards every accepted name, which means the unset ones
 * arrive as empty strings and always win. This checks the values instead.
 */
fun Project.firstEnv(vararg names: String): Provider<String> =
    providers.provider {
        names.firstNotNullOfOrNull { name ->
            System.getenv(name)?.takeIf { it.isNotBlank() }
        }
    }
