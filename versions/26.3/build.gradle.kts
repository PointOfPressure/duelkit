plugins { id("net.fabricmc.fabric-loom") }
apply(from = rootProject.file("gradle/shared.gradle.kts"))

dependencies {
    minecraft("com.mojang:minecraft:26.3")
    implementation("net.fabricmc:fabric-loader:0.19.5")
    implementation("net.fabricmc.fabric-api:fabric-api:0.160.7+26.3")
    // optional: settings screen
    compileOnly("dev.isxander:yet-another-config-lib:3.9.7+26.3-fabric") { isTransitive = false }
    compileOnly("com.terraformersmc:modmenu:21.0.0") { isTransitive = false }
}

java { toolchain { languageVersion.set(JavaLanguageVersion.of(25)) } }
