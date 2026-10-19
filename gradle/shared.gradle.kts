// Applied by each version project: shared sources, archive name, fabric.mod.json expansion.
val mc = project.name
extensions.configure<BasePluginExtension> { archivesName.set("duelkit") }
extensions.configure<SourceSetContainer> {
    named("main") {
        java.srcDir(rootProject.file("src/main/java"))
        resources.srcDir(rootProject.file("src/main/resources"))
    }
}
tasks.named<ProcessResources>("processResources") {
    val props = mapOf("version" to project.version, "minecraft" to mc)
    inputs.properties(props)
    filesMatching("fabric.mod.json") { expand(props) }
}

repositories {
    maven("https://maven.isxander.dev/releases")
    maven("https://maven.terraformersmc.com/releases")
}
