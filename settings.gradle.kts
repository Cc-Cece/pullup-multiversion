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

        fun mc(version: String, vararg loaders: String) {
            // Make the relevant version directories named "1.20.2-fabric", "1.20.2-forge", etc.
            for (it in loaders) {
                val projectName = "$version-$it"
                if (targetProjects == null || targetProjects.contains(projectName)) {
                    version(projectName, version)
                }
            }
        }

        mc("1.18.2", "fabric", "forge")
        mc("1.20.1", "fabric", "forge")
        mc("1.20.2", "fabric", "neoforge")
        mc("26.1", "fabric", "neoforge")

        val defaultVcsVersion = "1.20.1-fabric"
        vcsVersion = when {
            targetProjects == null -> defaultVcsVersion
            targetProjects.contains(defaultVcsVersion) -> defaultVcsVersion
            else -> targetProjects.first()
        }
    }
    create(rootProject)
}

rootProject.name = "pullup-multiversion"
