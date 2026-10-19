plugins {
    // 1.21.11 is obfuscated (Mojang mappings, remapped jar); 26.x ships unobfuscated.
    id("net.fabricmc.fabric-loom-remap") version "1.17-SNAPSHOT" apply false
    id("net.fabricmc.fabric-loom") version "1.17-SNAPSHOT" apply false
}

subprojects {
    version = "0.2.0+${name}"
    group = "io.github.pointofpressure.duelkit"
}
