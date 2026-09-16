/*
 * DayZ Inventory - `dayz-common` convention plugin
 * Copyright 2026 suoim
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Applied to every Stonecutter node (common / fabric / forge / neoforge).
 * It owns the parts that do not depend on which mod loader is being built:
 * version strings, the Java toolchain, repositories, resource expansion and the
 * licence jar.
 */

import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType

plugins {
    id("java")
    id("java-library")
    id("maven-publish")
}

val minecraftVersion = mc
val javaVersion = prop("deps.java").toInt()

group = prop("mod.group")
version = "${prop("mod.version")}+mc$minecraftVersion"

// `dayz-inventory-fabric-26.2`, `dayz-inventory-neoforge-1.21.1`, ...
// The Minecraft version is part of the filename on purpose: both CurseForge and
// Modrinth reject a second file whose display name collides inside a project,
// and this mod ships the same mod version for several game versions.
extensions.configure<BasePluginExtension> {
    archivesName.set("$modSlug-$branch-$minecraftVersion")
}

extensions.configure<JavaPluginExtension> {
    toolchain.languageVersion.set(JavaLanguageVersion.of(javaVersion))
    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(javaVersion)
    // Keep parameter names so Mixin can resolve `@Local`/`@Inject` signatures
    // that rely on them.
    options.compilerArgs.add("-parameters")
}

// ---------------------------------------------------------------------------
// Version renames
// ---------------------------------------------------------------------------
// 1.21.11 renamed several widely used members. The shared sources are written
// against the 26.2 names, so the older nodes get them rewritten backwards here
// instead of every affected line being duplicated behind a `//? if` block.
// `stonecutter { }` is only a DSL shorthand on scripts the plugin generates for;
// inside a precompiled convention plugin the extension has to be reached through
// the `sc` accessor from Utils.kt.
val versionRenames = sc.current.parsed < "26.1"

sc.replacements.string(sc.current.parsed < "1.21.11") {
    // net.minecraft.resources.Identifier (1.21.11+)
    replace("Identifier", "ResourceLocation")
    // Level#isClientSide became a method (1.21.11+)
    replace("isClientSide()", "isClientSide")
    // ServerPlayer#getServer was removed in favour of Level#getServer
    replace("player.level().getServer()", "player.getServer()")
    replace("serverPlayer.level().getServer()", "serverPlayer.getServer()")
    // 1.21.11 swapped GuiGraphics#pose() from a PoseStack to a Matrix3x2fStack:
    // pushPose/popPose became pushMatrix/popMatrix, and the 3-argument
    // translate/scale lost their z component.
    replace("pose().pushMatrix()", "pose().pushPose()")
    replace("pose().popMatrix()", "pose().popPose()")
    replace("pose().scale(scale, scale)", "pose().scale(scale, scale, 1.0f)")
    replace("pose().scale(2.0F, 2.0F)", "pose().scale(2.0F, 2.0F, 1.0F)")
    replace("pose().translate(middleColumnX + 65, bodyY + 5)", "pose().translate(middleColumnX + 65, bodyY + 5, 100)")
    replace("pose().translate(0, 0)", "pose().translate(0, 0, 200.0F)")
    // Inventory#getSelectedSlot is the 1.21.11 accessor for the old `selected` field.
    replace("getInventory().getSelectedSlot()", "getInventory().selected")
    // Window#handle renamed the raw GLFW handle accessor in 1.21.11.
    replace("this.minecraft.getWindow().handle()", "this.minecraft.getWindow().getWindow()")
    // The immediate tooltip calls were renamed when the GUI became a render-state
    // pipeline (and again in 26.2).
    replace("guiGraphics.setTooltipForNextFrame(", "guiGraphics.renderTooltip(")
    replace("guiGraphics.setComponentTooltipForNextFrame(", "guiGraphics.renderComponentTooltip(")
}

// 26.1 rewrote the GUI as a render-state pipeline: GuiGraphics became
// GuiGraphicsExtractor, every render* method became extract*, and a handful of
// ItemStack drawing helpers lost their prefixes. These are pure renames, so they
// are rewritten backwards for everything below 26.1. (26.1 is the first
// unobfuscated release and already has the new GUI, which is why this boundary is
// 26.1 and not 26.2.)
sc.replacements.string(versionRenames) {
    replace("GuiGraphicsExtractor", "GuiGraphics")
    replace("extractBackground", "renderBackground")
    replace("extractRenderState", "render")
    replace("extractLabels", "renderLabels")
    replace("extractTooltip", "renderTooltip")
    replace("extractEntityInInventoryFollowsMouse", "renderEntityInInventoryFollowsMouse")
    replace("guiGraphics.text(", "guiGraphics.drawString(")
    replace("guiGraphics.fakeItem(", "guiGraphics.renderFakeItem(")
    replace("guiGraphics.itemDecorations(", "guiGraphics.renderItemDecorations(")
    // ContainerInput is the 26.1+ name for ClickType.
    replace("ContainerInput", "ClickType")
    // 26.1 removed AbstractContainerScreen#renderBg, so the panel drawing became
    // a private helper. It is still called explicitly on both sides.
    replace("this.drawDayZPanels(", "this.renderBg(")
}

// Minecraft#setScreen and #setScreenAndShow were deleted in 26.2 - *not* 26.1, which
// still has them - so this one rename keeps the later boundary while the rest of
// the GUI rewrite moved to 26.1.
sc.replacements.string(sc.current.parsed < "26.2") {
    replace("this.minecraft.gui.setScreen(", "this.minecraft.setScreen(")
}

// Fabric API followed Mojang's screen-handler -> menu rename for 26.1, moving
// `fabric-screen-handler-api-v1` to `fabric-menu-api-v1` and renaming the types
// with it. `PayloadTypeRegistry#playC2S` became `serverboundPlay` in the same
// release.
sc.replacements.string(sc.current.parsed < "26.1") {
    replace("net.fabricmc.fabric.api.menu.v1", "net.fabricmc.fabric.api.screenhandler.v1")
    replace("ExtendedMenuProvider", "ExtendedScreenHandlerFactory")
    replace("ExtendedMenuType", "ExtendedScreenHandlerType")
    replace("PayloadTypeRegistry.serverboundPlay()", "PayloadTypeRegistry.playC2S()")
}

// 26.3 renamed InputConstants.Type.KEYSYM to KEYBOARD.
sc.replacements.string(sc.current.parsed >= "26.3") {
    replace("Type.KEYSYM", "Type.KEYBOARD")
}

// NeoForge 1.21.11 moved the client-side packet distributor into a client-only
// package; before that both sides share `neoforge.network.PacketDistributor`.
sc.replacements.string(sc.current.parsed < "1.21.11") {
    replace("net.neoforged.neoforge.client.network.ClientPacketDistributor", "net.neoforged.neoforge.network.PacketDistributor")
    replace("ClientPacketDistributor.sendToServer(", "PacketDistributor.sendToServer(")
}

// ---------------------------------------------------------------------------
// Repositories
// ---------------------------------------------------------------------------
repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/") { name = "Fabric" }
    maven("https://maven.neoforged.net/releases/") { name = "NeoForged" }
    maven("https://maven.minecraftforge.net/") { name = "MinecraftForge" }
    strictMaven("https://repo.spongepowered.org/repository/maven-public", "Sponge", "org.spongepowered")
    maven("https://maven.blamejared.com/") { name = "BlameJared" }
    maven("https://maven.theillusivec4.top/") { name = "Illusive Soulworks" }
    maven("https://maven.ladysnake.org/releases") { name = "Ladysnake Libs" }
    maven("https://maven.terraformersmc.com/releases/") { name = "TerraformersMC" }
    maven("https://api.modrinth.com/maven") {
        name = "Modrinth"
        content { includeGroup("maven.modrinth") }
    }
}

// ---------------------------------------------------------------------------
// Manifest expansion
// ---------------------------------------------------------------------------
// Every generated manifest (`fabric.mod.json`, both `mods.toml`s, `pack.mcmeta`
// and the mixin configs) is expanded from the same map, so a version bump or a
// new Minecraft target can never leave a stale hardcoded value behind. A stale
// `version` already shipped once: a 1.4.1 jar reported itself as 1.4.0 in crash
// reports because the toml hardcoded it.
val expandProps: Map<String, String> = buildMap {
    fun putIfPresent(key: String, value: String?) {
        if (!value.isNullOrBlank()) put(key, value)
    }

    put("version", prop("mod.version"))
    put("mod_id", prop("mod.id"))
    put("mod_id_slug", modSlug)
    put("mod_name", prop("mod.name"))
    put("mod_group", prop("mod.group"))
    put("mod_author", prop("mod.author"))
    put("mod_license", prop("mod.license"))
    put("mod_description", prop("mod.description"))
    put("mod_github", prop("mod.github"))
    put("mod_sources", prop("mod.sources"))
    put("mod_issues", prop("mod.issues"))
    put("minecraft_version", minecraftVersion)
    put("minecraft_range", prop("meta.minecraft-range"))
    put("java_version", javaVersion.toString())
    put("java_range", "[$javaVersion,)")
    put("mixin_compat", prop("deps.mixin-compat"))
    put("pack_format", prop("deps.pack-format"))

    // Loader ranges: an explicit `meta.<loader>-range` wins, otherwise the exact
    // dependency version is used as the floor. Deriving them here means a version
    // bump in `versions/<mc>/gradle.properties` cannot leave a stale range in a
    // generated manifest.
    putIfPresent("fabric_loader_version", propOrNull("deps.fabric-loader"))
    putIfPresent(
        "fabric_loader_range",
        propOrNull("meta.fabric-loader-range") ?: propOrNull("deps.fabric-loader")?.let { ">=$it" }
    )
    putIfPresent("fabric_api_version", propOrNull("deps.fabric-api"))
    putIfPresent("fabric_api_range", propOrNull("meta.fabric-api-range") ?: "*")
    putIfPresent("neoforge_version", propOrNull("deps.neoforge"))
    putIfPresent(
        "neoforge_range",
        propOrNull("meta.neoforge-range") ?: propOrNull("deps.neoforge")?.let { "[$it,)" }
    )
    putIfPresent("forge_version", propOrNull("deps.forge"))
    putIfPresent(
        "forge_range",
        propOrNull("meta.forge-range") ?: propOrNull("deps.forge")?.let { "[$it,)" }
    )
    // LegacyForge's loader version is the Forge major (e.g. 47 for 1.20.1).
    putIfPresent("forge_loader_range", propOrNull("deps.forge")?.substringBefore('.')?.let { "[$it,)" })
}

// `expand` only tolerates the tokens listed above; anything else aborts the
// build, which is the behaviour we want for a manifest placeholder.
val expandTargets = listOf(
    "fabric.mod.json",
    "META-INF/mods.toml",
    "META-INF/neoforge.mods.toml",
    "pack.mcmeta",
    "*.mixins.json",
    "*.client.mixins.json",
)

tasks.named<ProcessResources>("processResources") {
    inputs.properties(expandProps)
    filesMatching(expandTargets) {
        expand(expandProps)
    }
}

// ---------------------------------------------------------------------------
// Licence
// ---------------------------------------------------------------------------
// Ship the project's Apache-2.0 LICENSE inside every artifact, the way the mod
// has always done.
tasks.withType<Jar>().configureEach {
    from(rootProject.file("LICENSE")) {
        into("META-INF")
        rename { "LICENSE-${project.name}" }
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// Expose the (Stonecutter-processed) source directories of this node so that a
// loader branch can compile them straight into its own jar - see
// `dayz-loader.gradle.kts`. This keeps exactly one copy of every class, and one
// copy of every mixin config, on the mod classpath.
val sourceSetsContainer = extensions.getByType<SourceSetContainer>()
val mainSourceSet = sourceSetsContainer.getByName("main")

// Stonecutter's own source-set hook: wires the `stonecutterGenerate` /
// `stonecutterMerge` tasks into the build.
sc.tasks.configureSource(mainSourceSet)

// Where Stonecutter writes this node's version-processed copy of the shared
// sources - `build/generated/stonecutter/<sourceSet>/`.
val generatedRoot = sc.tasks.generatedSourcesDir
val generatedJava = generatedRoot.map { it.dir("${mainSourceSet.name}/java") }
val generatedResources = generatedRoot.map { it.dir("${mainSourceSet.name}/resources") }

// Compile the *processed* tree, never the raw one.
//
// The shared `src/` directories are the single source of truth and contain every
// version's code side by side behind `//? if` markers. Stonecutter only comments
// the inactive branches out in the copy it generates, so handing the raw tree to
// javac would compile every branch at once and could never work. Pointing the
// compile task at the generated tree also means the active node behaves exactly
// like every other node instead of depending on the working tree having been
// "chiselled" to it first.
tasks.named<JavaCompile>("compileJava") {
    dependsOn("stonecutterGenerate")
    setSource(generatedJava)
}

val commonJava: Configuration = configurations.create("commonJava") {
    isCanBeResolved = false
    isCanBeConsumed = true
    description = "Stonecutter-processed Java sources of this module"
}

val commonResources: Configuration = configurations.create("commonResources") {
    isCanBeResolved = false
    isCanBeConsumed = true
    description = "Stonecutter-processed resources of this module"
}

// `mainSourceSet.*.sourceDirectories` is only final once Stonecutter has
// finished wiring the node up, which happens after this script body has run, so
// the artifacts are registered from `afterEvaluate`. Registering them eagerly
// yields an empty configuration and the loader branch silently compiles without
// any of the shared sources.
afterEvaluate {
    artifacts.add(commonJava.name, generatedJava.get().asFile)
    artifacts.add(commonResources.name, generatedResources.get().asFile)
}
