pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0" }
rootProject.name = "duelkit"

// One shared source tree (src/), one Gradle project per Minecraft version.
for (mc in listOf("1.21.11", "26.3")) {
    include(":$mc")
    project(":$mc").projectDir = file("versions/$mc")
}
