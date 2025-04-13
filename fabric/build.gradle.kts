plugins {
    id("com.github.johnrengelman.shadow") version "8.+"
}

architectury {
    platformSetupLoomIde()
    fabric()
}

loom {
    accessWidenerPath.set(project(":common").loom.accessWidenerPath)
}

val common: Configuration by configurations.creating
val shadowCommon: Configuration by configurations.creating // Don't use shadow from the shadow plugin because we don't want IDEA to index this.
val developmentFabric: Configuration = configurations.getByName("developmentFabric")
configurations {
    compileClasspath.get().extendsFrom(configurations["common"])
    runtimeClasspath.get().extendsFrom(configurations["common"])
    developmentFabric.extendsFrom(configurations["common"])
}

repositories {
    maven("https://maven.fabricmc.net")
    maven("https://maven.shedaniel.me/")
    maven("https://maven.terraformersmc.com/releases/")
    maven("https://maven.ladysnake.org/releases") {
        mavenContent {
            includeGroup("io.github.ladysnake")
            includeGroup("org.ladysnake")
            includeGroupByRegex("dev\\.onyxstudios.*")
        }
    }
}

dependencies {
    //Fabric
    modImplementation("net.fabricmc:fabric-loader:${rootProject.property("loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${rootProject.property("fabric_version")}")
    //Cloth Api? "The Cloth API has largely been replaced by the Architectury API."
    //Architectury API
    modApi("dev.architectury:architectury-fabric:${rootProject.property("architectury_fabric_version")}")
    //Kotlin
    modImplementation("net.fabricmc:fabric-language-kotlin:${rootProject.property("fabric_language_kotlin_version")}")
    //Cloth Config
    modImplementation("me.shedaniel.cloth:cloth-config-fabric:${rootProject.property("cloth_config_fabric_version")}") {
        exclude("net.fabricmc.fabric-api")
    }

    common(project(":common", configuration = "namedElements")) { isTransitive = false }
    shadowCommon(project(":common", configuration = "transformProductionFabric")) { isTransitive = false }
}

tasks {
    processResources {
        val version: String by project
        val minecraft_version: String by project

        inputs.property("version", version)
        inputs.property("minecraft_version", minecraft_version)

        filesMatching("fabric.mod.json") {
            expand(
                "version" to version,
                "minecraft_version" to minecraft_version
            )
        }
    }

    shadowJar {
        exclude("architectury.common.json")
        configurations = listOf(project.configurations["shadowCommon"])
        archiveClassifier.set("dev-shadow")
    }

    remapJar {
        injectAccessWidener.set(true)
        inputFile.set(shadowJar.flatMap { it.archiveFile })
        dependsOn(shadowJar)
        archiveClassifier.set("fabric")
    }

    jar {
        archiveClassifier.set("dev")
    }

    sourcesJar {
        val commonSources = project(":common").tasks.getByName<Jar>("sourcesJar")
        dependsOn(commonSources)
        from(commonSources.archiveFile.map { zipTree(it) })
    }
}