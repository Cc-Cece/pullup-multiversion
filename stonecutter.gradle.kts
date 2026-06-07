plugins {
    id("dev.kikugie.stonecutter")
    id("gg.meza.stonecraft")
}

val activeTarget = providers.gradleProperty("targetProjects")
    .orNull
    ?.split(",")
    ?.map { it.trim() }
    ?.firstOrNull { it.isNotEmpty() }
    ?: "1.20.1-fabric"

stonecutter active activeTarget /* [SC] DO NOT EDIT */
