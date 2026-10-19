plugins { id("net.fabricmc.fabric-loom") }
apply(from = rootProject.file("gradle/shared.gradle.kts"))

dependencies {
    minecraft("com.mojang:minecraft:26.3")
    implementation("net.fabricmc:fabric-loader:0.19.5")
    implementation("net.fabricmc.fabric-api:fabric-api:0.160.7+26.3")
}

java { toolchain { languageVersion.set(JavaLanguageVersion.of(25)) } }
