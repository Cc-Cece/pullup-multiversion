import gg.meza.stonecraft.mod
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.gradle.api.file.Directory
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.process.JavaForkOptions
import java.io.File

data class TargetMetadata(
    val id: String,
    val minecraftVersion: String,
    val loader: String,
    val bandId: String,
    val releaseLineId: String,
    val dependencyKey: String,
    val javaVersion: Int,
    val metadataFlavor: String,
    val capabilities: Set<String>,
)

data class ReleaseLineMetadata(
    val id: String,
    val anchorTarget: String,
    val loader: String,
    val bandId: String,
    val exactGameVersions: List<String>,
    val fabricDependsRange: String?,
    val forgeDependsRange: String?,
    val neoForgeDependsRange: String?,
    val displayName: String,
    val publish: Boolean,
)

data class TargetQuirk(
    val targetId: String,
    val flags: Set<String>,
)

fun asStringList(value: Any?): List<String> = (value as? List<*>)?.map { it.toString() } ?: emptyList()

fun parseTargets(file: File): List<TargetMetadata> {
    val rawTargets = JsonSlurper().parse(file) as List<Map<String, Any?>>
    return rawTargets.map { rawTarget ->
        TargetMetadata(
            id = rawTarget.getValue("id").toString(),
            minecraftVersion = rawTarget.getValue("minecraftVersion").toString(),
            loader = rawTarget.getValue("loader").toString(),
            bandId = rawTarget.getValue("bandId").toString(),
            releaseLineId = rawTarget.getValue("releaseLineId").toString(),
            dependencyKey = rawTarget.getValue("dependencyKey").toString(),
            javaVersion = rawTarget.getValue("javaVersion").toString().toInt(),
            metadataFlavor = rawTarget.getValue("metadataFlavor").toString(),
            capabilities = asStringList(rawTarget["capabilities"]).toSet(),
        )
    }
}

fun parseReleaseLines(file: File): List<ReleaseLineMetadata> {
    val rawReleaseLines = JsonSlurper().parse(file) as List<Map<String, Any?>>
    return rawReleaseLines.map { rawReleaseLine ->
        ReleaseLineMetadata(
            id = rawReleaseLine.getValue("id").toString(),
            anchorTarget = rawReleaseLine.getValue("anchorTarget").toString(),
            loader = rawReleaseLine.getValue("loader").toString(),
            bandId = rawReleaseLine.getValue("bandId").toString(),
            exactGameVersions = asStringList(rawReleaseLine["exactGameVersions"]),
            fabricDependsRange = rawReleaseLine["fabricDependsRange"]?.toString(),
            forgeDependsRange = rawReleaseLine["forgeDependsRange"]?.toString(),
            neoForgeDependsRange = rawReleaseLine["neoForgeDependsRange"]?.toString(),
            displayName = rawReleaseLine.getValue("displayName").toString(),
            publish = rawReleaseLine.getValue("publish").toString().toBoolean(),
        )
    }
}

fun parseQuirks(file: File): List<TargetQuirk> {
    val rawQuirks = JsonSlurper().parse(file) as List<Map<String, Any?>>
    return rawQuirks.map { rawQuirk ->
        TargetQuirk(
            targetId = rawQuirk.getValue("targetId").toString(),
            flags = asStringList(rawQuirk["flags"]).toSet(),
        )
    }
}

val allTargets = parseTargets(rootProject.file("versions/targets.json"))
val targetsById = allTargets.associateBy { it.id }
val allReleaseLines = parseReleaseLines(rootProject.file("versions/release-lines.json"))
val releaseLinesById = allReleaseLines.associateBy { it.id }
val allQuirks = parseQuirks(rootProject.file("versions/quirks.json"))
val quirksByTargetId = allQuirks.associateBy { it.targetId }

val target = targetsById[project.name]
val releaseLine = target?.let { releaseLinesById.getValue(it.releaseLineId) }
val quirkFlags = quirksByTargetId[project.name]?.flags.orEmpty()

fun hasCapability(name: String): Boolean = target?.capabilities?.contains(name) == true
fun hasQuirk(name: String): Boolean = quirkFlags.contains(name)
fun projectProperty(name: String): String? = providers.gradleProperty(name).orNull ?: findProperty(name)?.toString()

val resourceTemplateValues = buildMap<String, String> {
    put("id", providers.gradleProperty("mod.id").get())
    put("name", providers.gradleProperty("mod.name").get())
    put("version", providers.gradleProperty("mod.version").get())
    put("group", providers.gradleProperty("mod.group").get())
    put("description", providers.gradleProperty("mod.description").get())
    projectProperty("minecraft_version")?.let { put("minecraftVersion", it) }
    projectProperty("fabric_version")?.let { put("fabricVersion", it) }
    projectProperty("loader_version")?.let { put("loaderVersion", it) }
    projectProperty("forge_version")?.let { put("forgeVersion", it) }
    projectProperty("neoforge_version")?.let { put("neoForgeVersion", it) }
    when (target?.loader) {
        "fabric" -> releaseLine?.fabricDependsRange?.let { put("minecraftDependencyRange", it) }
        "forge" -> releaseLine?.forgeDependsRange?.let { put("minecraftDependencyRange", it) }
        "neoforge" -> releaseLine?.neoForgeDependsRange?.let { put("minecraftDependencyRange", it) }
    }
    releaseLine?.displayName?.let { put("releaseLineDisplayName", it) }
    releaseLine?.exactGameVersions?.joinToString(", ")?.let { put("supportedMinecraftVersions", it) }
}

fun relativePathsUnder(directory: Directory, suffix: String): Set<String> {
    if (!directory.asFile.exists()) {
        return emptySet()
    }

    return directory.asFile
        .walkTopDown()
        .filter { it.isFile && it.name.endsWith(suffix) }
        .map { it.relativeTo(directory.asFile).invariantSeparatorsPath }
        .toSet()
}

plugins {
    id("gg.meza.stonecraft")
}

repositories {
    maven(rootProject.layout.projectDirectory.dir("gradle/local-repo"))
    maven("https://cursemaven.com") {
        content {
            includeGroup("curse.maven")
        }
    }
}

dependencies {
    val bundledLibs = listOf(
        "net.objecthunter:exp4j:0.4.8",
    )

    bundledLibs.forEach { coordinate ->
        implementation(coordinate)
        add("include", coordinate)
        if (!mod.isFabric) {
            configurations.findByName("forgeRuntimeLibrary")?.let { runtimeCfg ->
                add(runtimeCfg.name, coordinate)
            }
        }
    }

    // commons-io is provided by Minecraft, compileOnly is sufficient to avoid module conflict at runtime
    compileOnly("commons-io:commons-io:2.16.1")

    if (hasQuirk("fabric-split-api-deps")) {
        runtimeOnly("cpw.mods:modlauncher:10.0.10")
        add("modApi", "net.fabricmc.fabric-api:fabric-command-api-v2:2.2.14+1802ada577")
        add("modApi", "net.fabricmc.fabric-api:fabric-lifecycle-events-v1:2.2.23+1802ada577")
        add("modApi", "net.fabricmc.fabric-api:fabric-networking-api-v1:1.3.14+a158aa0477")
        add("modApi", "net.fabricmc.fabric-api:fabric-rendering-v1:3.0.9+1802ada577")
        add("modApi", "net.fabricmc.fabric-api:fabric-api-base:0.4.32+1802ada577")
        add("modApi", "net.fabricmc.fabric-api:fabric-resource-loader-v0:0.11.12+fb82e9d777")
        add("modApi", "net.fabricmc.fabric-api:fabric-resource-conditions-api-v1:2.3.9+1802ada577")
        add("modApi", "net.fabricmc.fabric-api:fabric-message-api-v1:5.1.10+1802ada577")
        add("modApi", "net.fabricmc.fabric-api:fabric-sound-api-v1:1.0.14+1802ada577")
    }

}

tasks.withType<ProcessResources>().configureEach {
    inputs.properties(resourceTemplateValues)
    filesMatching(
        listOf(
            "fabric.mod.json",
            "META-INF/mods.toml",
            "META-INF/neoforge.mods.toml",
        )
    ) {
        expand(resourceTemplateValues)
    }
}

val sourceSets = extensions.getByType<SourceSetContainer>()
val rootJavaDir = rootProject.layout.projectDirectory.dir("src/main/java")
val generatedJavaDir = layout.buildDirectory.dir("generated/stonecutter/main/java")
val rootResourcesDir = rootProject.layout.projectDirectory.dir("src/main/resources")
val generatedResourcesDir = layout.buildDirectory.dir("generated/stonecutter/main/resources")

tasks.withType<JavaCompile>().configureEach {
    dependsOn("stonecutterGenerate")

    setSource(
        provider {
            val generatedDir = generatedJavaDir.get()
            val generatedPaths = relativePathsUnder(generatedDir, ".java")

            val sharedSources = project.fileTree(rootJavaDir) {
                if (generatedPaths.isNotEmpty()) {
                    exclude(generatedPaths)
                }
            }
            val generatedSources = project.fileTree(generatedDir)

            files(sharedSources, generatedSources)
        }
    )
}

sourceSets.named("main") {
    resources.setSrcDirs(
        listOf(
            rootResourcesDir,
            generatedResourcesDir,
        )
    )
}

tasks.withType<ProcessResources>().configureEach {
    dependsOn("stonecutterGenerate")

    from(
        provider {
            val generatedDir = generatedResourcesDir.get()
            val generatedPaths = relativePathsUnder(generatedDir, "")

            project.fileTree(rootResourcesDir) {
                if (generatedPaths.isNotEmpty()) {
                    exclude(generatedPaths)
                }
            }
        }
    )
    from(generatedResourcesDir)
}

modSettings {
    clientOptions {
        fov = 90
        guiScale = 3
        narrator = false
        darkBackground = true
        musicVolume = 0.0
    }
}

if (hasCapability("v26")) {
    // Stonecutter injects generated sources into compileJava directly, so exclusions
    // must be applied on the compile task instead of sourceSets.
    tasks.withType<JavaCompile>().configureEach {
        exclude("cool/muyucloud/pullup/access/**")
        exclude("cool/muyucloud/pullup/mixin/**")
        exclude("cool/muyucloud/pullup/util/command/**")
        exclude("cool/muyucloud/pullup/util/network/**")
    }

    tasks.withType<ProcessResources>().configureEach {
        exclude("pullup.mixins.json")

        doLast {
            val fabricModJson = destinationDir.resolve("fabric.mod.json")
            if (fabricModJson.exists()) {
                val json = JsonSlurper().parse(fabricModJson) as MutableMap<String, Any?>
                json.remove("mixins")
                fabricModJson.writeText(JsonOutput.prettyPrint(JsonOutput.toJson(json)) + System.lineSeparator())
            }

            val neoForgeModsToml = destinationDir.resolve("META-INF/neoforge.mods.toml")
            if (neoForgeModsToml.exists()) {
                val toml = neoForgeModsToml.readText()
                val sanitized = toml.replace(
                    Regex("(?ms)\\n\\[\\[mixins]]\\s*\\nconfig\\s*=\\s*\"pullup\\.mixins\\.json\"\\s*\\n?"),
                    "\n",
                )
                neoForgeModsToml.writeText(sanitized)
            }
        }
    }
}

if (hasQuirk("copy-neoforge-mods-toml-to-mods-toml") || hasQuirk("inject-pack-mcmeta")) {
    tasks.withType<ProcessResources>().configureEach {
        doLast {
            val metaInfDir = destinationDir.resolve("META-INF")
            val neoForgeModsToml = metaInfDir.resolve("neoforge.mods.toml")
            val forgeStyleModsToml = metaInfDir.resolve("mods.toml")
            val packMcmeta = destinationDir.resolve("pack.mcmeta")

            // NeoForge 20.2 runtime in this environment still discovers mods via META-INF/mods.toml.
            // Keep neoforge.mods.toml, and add a compatibility copy to avoid "invalid mod file".
            if (hasQuirk("copy-neoforge-mods-toml-to-mods-toml") && neoForgeModsToml.exists() && !forgeStyleModsToml.exists()) {
                forgeStyleModsToml.writeText(neoForgeModsToml.readText())
            }

            // Ensure bundled mod assets have valid ResourcePackInfo to avoid NeoForge load warnings.
            if (hasQuirk("inject-pack-mcmeta") && !packMcmeta.exists()) {
                packMcmeta.writeText(
                    """
                    {
                      "pack": {
                        "pack_format": 8,
                        "description": "PullUp embedded resources"
                      }
                    }
                    """.trimIndent() + System.lineSeparator()
                )
            }
        }
    }
}

if (hasQuirk("fabric-split-api-deps")) {
    configurations.named("modApi").configure {
        withDependencies {
            removeIf {
                it.group == "net.fabricmc.fabric-api" &&
                    (it.name == "fabric-api" || it.name == "fabric-gametest-api-v1")
            }
        }
    }
}

if (hasQuirk("fabric-force-game-version")) {
    tasks.matching { it.name == "runClient" }.configureEach {
        (this as? JavaForkOptions)?.jvmArgs(
            "-Dfabric.gameVersion=1.20.1",
        )
    }
}

if (!hasQuirk("keep-forge-stubs")) {
    tasks.withType<JavaCompile>().configureEach {
        exclude("cpw/mods/modlauncher/log/**")
        exclude("net/minecraftforge/fml/CrashReportCallables.java")
        exclude("net/minecraftforge/fml/ISystemReportExtender.java")
        exclude("net/minecraftforge/fml/loading/**")
        exclude("net/minecraftforge/forge/snapshots/ForgeSnapshotsMod.java")
    }
}

if (!hasCapability("v26")) {
    tasks.withType<JavaCompile>().configureEach {
        exclude("cool/muyucloud/pullup/v26/**")
    }
}

// Example of overriding publishing settings
publishMods {
    modrinth {
        if (mod.isFabric) requires("fabric-api")
    }

    curseforge {
        clientRequired = true // Set as needed
        serverRequired = false // Set as needed
        if (mod.isFabric) requires("fabric-api")
    }
}

tasks.withType<Jar>().configureEach {
    manifest {
        if (!hasCapability("v26")) {
            attributes(
                "MixinConfigs" to "pullup.mixins.json"
            )
        }
    }
}

if (project == rootProject) {
    tasks.register("validateVersionMatrix") {
        group = "verification"
        description = "Validate D1 target, release-line and version coverage metadata."

        doLast {
            val releaseLinesByTarget = allReleaseLines.associateBy { it.anchorTarget }
            val missingReleaseLines = allTargets.filter { it.releaseLineId !in releaseLinesById.keys }.map { it.id }
            require(missingReleaseLines.isEmpty()) {
                "Targets missing release lines: ${missingReleaseLines.joinToString(", ")}"
            }

            val missingAnchorTargets = allReleaseLines.filter { it.anchorTarget !in targetsById.keys }.map { it.id }
            require(missingAnchorTargets.isEmpty()) {
                "Release lines missing anchor targets: ${missingAnchorTargets.joinToString(", ")}"
            }

            require(allReleaseLines.all { it.exactGameVersions.isNotEmpty() }) {
                "Each release line must declare at least one exact Minecraft version."
            }

            val duplicatedVersions = allReleaseLines
                .flatMap { line -> line.exactGameVersions.map { version -> version to line.id } }
                .groupBy({ it.first }, { it.second })
                .filterValues { it.size > 1 }
            require(duplicatedVersions.isEmpty()) {
                "Exact Minecraft versions must map to exactly one release line: $duplicatedVersions"
            }

            val coveredVersions = allReleaseLines.flatMap { it.exactGameVersions }.toSet()
            val expectedVersions = setOf(
                "1.18.2",
                "1.19",
                "1.19.1",
                "1.19.2",
                "1.19.3",
                "1.19.4",
                "1.20",
                "1.20.1",
                "1.20.2",
                "1.20.3",
                "1.20.4",
                "1.20.5",
                "1.20.6",
                "1.21",
                "1.21.1",
                "1.21.2",
                "1.21.3",
                "1.21.4",
                "1.21.5",
                "1.21.6",
                "1.21.7",
                "1.21.8",
                "1.21.9",
                "1.21.10",
                "1.21.11",
                "26.1",
                "26.1.1",
                "26.1.2",
            )
            require(coveredVersions == expectedVersions) {
                val missing = expectedVersions - coveredVersions
                val unexpected = coveredVersions - expectedVersions
                "Version coverage mismatch. Missing=${missing.joinToString(", ")} Unexpected=${unexpected.joinToString(", ")}"
            }

            val missingDependencyFiles = allTargets
                .map { it.dependencyKey }
                .distinct()
                .filterNot { rootProject.file("versions/dependencies/$it.properties").exists() }
            require(missingDependencyFiles.isEmpty()) {
                "Missing dependency anchor files: ${missingDependencyFiles.joinToString(", ")}"
            }
        }
    }
}
