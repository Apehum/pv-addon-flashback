plugins {
    kotlin("jvm") version "2.0.21"
    id("fabric-loom") version "1.10-SNAPSHOT"
    id("com.gradleup.shadow") version "8.3.5"
}

repositories {
    mavenCentral()

    exclusiveContent {
        forRepository {
            maven {
                name = "Modrinth"
                url = uri("https://api.modrinth.com/maven")
            }
        }
        filter {
            includeGroup("maven.modrinth")
        }
    }

    maven("https://repo.plasmoverse.com/releases")
//    maven("https://repo.plasmoverse.com/snapshots")
    maven("https://jitpack.io")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

loom {
    accessWidenerPath = file("src/main/resources/pvaddonflashback.accesswidener")
}

dependencies {
    compileOnly(kotlin("stdlib-jdk8"))

    minecraft("com.mojang:minecraft:1.21.1")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:0.16.10")

    modImplementation("net.fabricmc.fabric-api:fabric-api:0.115.3+1.21.1")

    modImplementation("maven.modrinth:plasmo-voice:fabric-1.21-2.1.4")
    compileOnly("su.plo.voice.api:client:2.1.4")
    compileOnly("su.plo.voice.api:server:2.1.4")

    modImplementation("maven.modrinth:flashback:0.20.1-fabric,1.21.1")
    modImplementation("maven.modrinth:modmenu:11.0.3")
    modImplementation("maven.modrinth:cloth-config:15.0.140+fabric")
}

@Suppress("UnstableApiUsage")
val runProdClient by tasks.registering(net.fabricmc.loom.task.prod.ClientProductionRunTask::class) {
    group = "fabric"

    mods.from(project.configurations.modImplementation.get())

    outputs.upToDateWhen { false }
}

tasks {
    shadowJar {
        configurations = listOf(project.configurations.shadow.get())

        relocate("kotlin", "su.plo.voice.libs.kotlin")
        relocate("kotlinx.coroutines", "su.plo.voice.libs.kotlinx.coroutines")
        relocate("kotlinx.serialization", "su.plo.voice.libs.kotlinx.serialization")
    }

    remapJar {
        dependsOn(shadowJar)
        inputFile.set(shadowJar.get().archiveFile)
    }

    processResources {
        filesMatching(mutableListOf("fabric.mod.json")) {
            expand(
                mutableMapOf(
                    "version" to project.version,
                ),
            )
        }
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 21
}
