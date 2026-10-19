plugins { id("net.fabricmc.fabric-loom-remap") }
apply(from = rootProject.file("gradle/shared.gradle.kts"))

dependencies {
    minecraft("com.mojang:minecraft:1.21.11")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:0.19.5")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.141.6+1.21.11")
}

java { toolchain { languageVersion.set(JavaLanguageVersion.of(21)) } }
