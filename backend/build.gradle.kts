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

    implementation("ch.qos.logback:logback-classic:1.4.14")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")

    implementation("com.github.jchamlin:clipper-java:b4dcd50c51")
    implementation("com.google.code.gson:gson:2.10")
    implementation("org.uncommons.watchmaker:watchmaker-framework:0.7.1")
    implementation("org.apache.xmlgraphics:batik-svg-dom:1.17")
    implementation("org.apache.xmlgraphics:batik-swing:1.17")
    implementation("org.dom4j:dom4j:2.1.3")
    implementation("org.uncommons.watchmaker:watchmaker-swing:0.7.1")
    implementation("io.jenetics:jenetics:5.2.0")

    implementation("org.jetbrains.exposed", "exposed-core", "0.47.0")
    implementation("org.jetbrains.exposed", "exposed-dao", "0.47.0")
    implementation("org.jetbrains.exposed", "exposed-jdbc", "0.47.0")
    implementation("org.jetbrains.exposed", "exposed-kotlin-datetime", "0.47.0")
    implementation("com.h2database", "h2", "2.2.224")
    implementation("org.postgresql:postgresql:42.7.1")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.1")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.1")
    implementation(kotlin("stdlib-jdk8"))
}

kotlin {
    jvmToolchain(17)
}

