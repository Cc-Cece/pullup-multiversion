import gg.meza.stonecraft.mod
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.process.JavaForkOptions

plugins {
    id("gg.meza.stonecraft")
}

repositories {
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

    if (project.name == "1.20.1-fabric") {
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

modSettings {
    clientOptions {
        fov = 90
        guiScale = 3
        narrator = false
        darkBackground = true
        musicVolume = 0.0
    }
}

if (project.name.startsWith("26.1-")) {
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

if (project.name == "1.20.2-neoforge") {
    tasks.withType<ProcessResources>().configureEach {
        doLast {
            val metaInfDir = destinationDir.resolve("META-INF")
            val neoForgeModsToml = metaInfDir.resolve("neoforge.mods.toml")
            val forgeStyleModsToml = metaInfDir.resolve("mods.toml")
            val packMcmeta = destinationDir.resolve("pack.mcmeta")

            // NeoForge 20.2 runtime in this environment still discovers mods via META-INF/mods.toml.
            // Keep neoforge.mods.toml, and add a compatibility copy to avoid "invalid mod file".
            if (neoForgeModsToml.exists() && !forgeStyleModsToml.exists()) {
                forgeStyleModsToml.writeText(neoForgeModsToml.readText())
            }

            // Ensure bundled mod assets have valid ResourcePackInfo to avoid NeoForge load warnings.
            if (!packMcmeta.exists()) {
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

if (project.name == "1.20.1-fabric") {
    configurations.named("modApi").configure {
        withDependencies {
            removeIf {
                it.group == "net.fabricmc.fabric-api" &&
                    (it.name == "fabric-api" || it.name == "fabric-gametest-api-v1")
            }
        }
    }

    tasks.matching { it.name == "runClient" }.configureEach {
        (this as? JavaForkOptions)?.jvmArgs(
            "-Dfabric.gameVersion=1.20.1",
        )
    }
}

if (project.name != "1.20.1-fabric") {
    tasks.withType<JavaCompile>().configureEach {
        exclude("cpw/mods/modlauncher/log/**")
        exclude("net/minecraftforge/fml/CrashReportCallables.java")
        exclude("net/minecraftforge/fml/ISystemReportExtender.java")
        exclude("net/minecraftforge/fml/loading/**")
        exclude("net/minecraftforge/forge/snapshots/ForgeSnapshotsMod.java")
    }
}

if (!project.name.startsWith("26.1-")) {
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
        if (!project.name.startsWith("26.1-")) {
            attributes(
                "MixinConfigs" to "pullup.mixins.json"
            )
        }
    }
}
