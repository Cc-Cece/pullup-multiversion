import groovy.json.JsonSlurper

data class StonecutterTarget(
    val id: String,
    val minecraftVersion: String,
)

fun parseStonecutterTargets(file: File): List<StonecutterTarget> {
    val rawTargets = JsonSlurper().parse(file) as List<Map<String, Any?>>
    return rawTargets.map { rawTarget ->
        StonecutterTarget(
            id = rawTarget.getValue("id").toString(),
            minecraftVersion = rawTarget.getValue("minecraftVersion").toString(),
        )
    }
}

pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.kikugie.dev/releases")
        maven("https://maven.kikugie.dev/snapshots")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev")
        maven("https://maven.minecraftforge.net")
        maven("https://maven.neoforged.net/releases/")
    }
}
plugins {
    id("gg.meza.stonecraft") version "1.10.+"
    id("dev.kikugie.stonecutter") version "0.9.+"
}

val allTargets = parseStonecutterTargets(file("versions/targets.json"))

stonecutter {
    centralScript = "build.gradle.kts"
    kotlinController = true
    shared {
        val targetProjects = providers.gradleProperty("targetProjects")
            .orNull
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?.toSet()

        val selectedTargets = allTargets.filter { targetProjects == null || targetProjects.contains(it.id) }
        require(selectedTargets.isNotEmpty()) {
            "No Stonecutter targets matched -PtargetProjects=${targetProjects?.joinToString(",") ?: "<all>"}"
        }

        selectedTargets.forEach { target ->
            version(target.id, target.minecraftVersion)
        }

        val defaultVcsVersion = "1.20.1-fabric"
        vcsVersion = selectedTargets.firstOrNull { it.id == defaultVcsVersion }?.id ?: selectedTargets.first().id
    }
    create(rootProject)
}

rootProject.name = "pullup-multiversion"
