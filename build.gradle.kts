import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    java
    id("architectury-plugin")
    id("dev.architectury.loom") apply false
    id("me.shedaniel.unified-publishing") apply false
    kotlin("jvm") apply false
}

architectury {
    minecraft = rootProject.property("minecraft_version").toString()
}

subprojects {
    apply(plugin = "dev.architectury.loom")
    apply(plugin = "me.shedaniel.unified-publishing")

    val loom = project.extensions.getByName<net.fabricmc.loom.api.LoomGradleExtensionAPI>("loom")

    loom.silentMojangMappingsLicense()

    dependencies {
        "minecraft"("com.mojang:minecraft:${project.property("minecraft_version")}")
        "mappings"(loom.officialMojangMappings())
    }
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "architectury-plugin")
    apply(plugin = "maven-publish")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    base.archivesName.set(rootProject.property("archives_base_name").toString())
    version = "${rootProject.property("mod_version")}-${project.property("minecraft_version")}"
    group = rootProject.property("maven_group").toString()

    dependencies {
        compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:${property("kotlin_version")}")
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(21)
    }

    java {
        withSourcesJar()
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }
}
