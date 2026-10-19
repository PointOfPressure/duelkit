plugins { id("net.fabricmc.fabric-loom-remap") }
apply(from = rootProject.file("gradle/shared.gradle.kts"))

dependencies {
    minecraft("com.mojang:minecraft:1.21.11")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:0.19.5")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.141.6+1.21.11")
    // optional: settings screen
    modCompileOnly("dev.isxander:yet-another-config-lib:3.8.1+1.21.11-fabric") { isTransitive = false }
    modCompileOnly("com.terraformersmc:modmenu:17.0.0") { isTransitive = false }
}

java { toolchain { languageVersion.set(JavaLanguageVersion.of(21)) } }
