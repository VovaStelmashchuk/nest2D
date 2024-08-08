plugins {
    application
    kotlin("jvm")
    id("io.ktor.plugin") version "2.3.7"
    kotlin("plugin.serialization") version "1.9.21"
}

group = "com.nestapp"
version = "0.6.5"

application {
    mainClass.set("com.nestapp.Main")
}

ktor {
    docker {
        jreVersion.set(JavaVersion.VERSION_17)
        imageTag.set("${project.version}")
        externalRegistry.set(
            io.ktor.plugin.features.DockerImageRegistry.dockerHub(
                appName = provider { "nest2d" },
                username = providers.environmentVariable("DOCKER_HUB_USERNAME"),
                password = providers.environmentVariable("DOCKER_HUB_PASSWORD")
            )
        )
        jib {
            from {
                image = "openjdk:17-jdk-alpine"
            }
            to {
                image = "vovochkastelmashchuk/nest2d"
                tags = setOf("${project.version}")
                auth {
                    setUsername(providers.environmentVariable("DOCKER_HUB_USERNAME"))
                    setPassword(providers.environmentVariable("DOCKER_HUB_PASSWORD"))
                }
            }
        }
    }
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://jitpack.io")
    }

    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
    mavenCentral()
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

dependencies {
    implementation("io.ktor:ktor-server-core:2.3.7")
    implementation("io.ktor:ktor-server-cio:2.3.7")
    implementation("io.ktor:ktor-server-status-pages:2.3.7")
    implementation("io.ktor:ktor-server-auto-head-response:2.3.7")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.7")
    implementation("io.ktor:ktor-server-cors:2.3.7")

    implementation("io.ktor:ktor-client-core:2.3.7")
    implementation("io.ktor:ktor-client-cio:2.3.7")
    implementation("io.ktor:ktor-client-json:2.3.7")
    implementation("io.ktor:ktor-client-serialization:2.3.7")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.7")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")

    implementation("io.minio:minio:8.5.11")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.mongodb:mongodb-driver-kotlin-coroutine:4.10.1")
}

kotlin {
    jvmToolchain(17)
}

