plugins {
    id("com.gradleup.shadow") version "9.4.0"
    java
    id("architectury-plugin")
    id("dev.architectury.loom")
}

architectury {
    platformSetupLoomIde()
    fabric()
}

loom {
    accessWidenerPath.set(project(":common").layout.projectDirectory.file("src/main/resources/ingameime.accesswidener"))
}

val common: Configuration by configurations.creating
val shadowCommon: Configuration by configurations.creating // Don't use shadow from the shadow plugin because we don't want IDEA to index this.

configurations.getByName("compileClasspath").extendsFrom(common)
configurations.getByName("runtimeClasspath").extendsFrom(common)
configurations.getByName("developmentFabric").extendsFrom(common)

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
    named<ProcessResources>("processResources") {
        val modVersion = project.version.toString()

        inputs.property("version", modVersion)

        filesMatching("fabric.mod.json") {
            expand(
                mapOf(
                    "version" to modVersion,
                    "minecraft_version" to rootProject.property("minecraft_version")
                )
            )
        }
    }

    val shadowJarTask = named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
        exclude("architectury.common.json")
        configurations = listOf(project.configurations["shadowCommon"])
        archiveClassifier.set("dev-shadow")
    }

    named<net.fabricmc.loom.task.RemapJarTask>("remapJar") {
        injectAccessWidener.set(true)
        inputFile.set(shadowJarTask.flatMap { it.archiveFile })
        dependsOn(shadowJarTask)
        archiveClassifier.set("fabric")
    }

    named<Jar>("jar") {
        archiveClassifier.set("dev")
    }

    named<Jar>("sourcesJar") {
        val commonSources = project(":common").tasks.named<Jar>("sourcesJar")
        dependsOn(commonSources)
        from(zipTree(commonSources.flatMap { it.archiveFile }))
    }
}