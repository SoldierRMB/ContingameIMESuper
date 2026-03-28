plugins {
    `maven-publish`
}

architectury {
    val enabledPlatforms = rootProject.property("enabled_platforms").toString()
    common(enabledPlatforms.split(","))
}

loom {
    accessWidenerPath.set(layout.projectDirectory.file("src/main/resources/ingameime.accesswidener"))
}

dependencies {
    // We depend on fabric loader here to use the fabric @Environment annotations and get the mixin dependencies
    // Do NOT use other classes from fabric loader
    modImplementation("net.fabricmc:fabric-loader:${rootProject.property("loader_version")}")
    //Kotlin
    modImplementation("net.fabricmc:fabric-language-kotlin:${rootProject.property("fabric_language_kotlin_version")}")
    //Architectury API
    modApi("dev.architectury:architectury:${rootProject.property("architectury_version")}")
    //Cloth Config
    modImplementation("me.shedaniel.cloth:cloth-config:${rootProject.property("cloth_config_version")}") {
        exclude("net.fabricmc.fabric-api")
    }
    implementation(kotlin("stdlib-jdk8"))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = rootProject.property("archives_base_name").toString()
            from(components.getByName("java"))
        }
    }

    // See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
    repositories {
        // Add repositories to publish to here.
    }
}