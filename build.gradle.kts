plugins { id("net.fabricmc.fabric-loom-remap") version "1.17-SNAPSHOT" }

version = "0.1.0+1.21.11"
group = "io.github.pointofpressure.duelkit"
base { archivesName = "duelkit" }

dependencies {
    minecraft("com.mojang:minecraft:1.21.11")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:0.19.5")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.141.6+1.21.11")
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("fabric.mod.json") { expand("version" to project.version) }
}

java { toolchain { languageVersion.set(JavaLanguageVersion.of(21)) } }
