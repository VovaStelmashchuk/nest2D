plugins {
    application
    kotlin("jvm")
    id("io.ktor.plugin") version "2.3.7"
    kotlin("plugin.serialization") version "1.9.21"
}

group = "com.nestapp"
version = "0.0.6"

application {
    mainClass.set("com.nestapp.Main")
}

ktor {
    docker {
        jreVersion.set(JavaVersion.VERSION_17)
        localImageName.set("nest2d")
        imageTag.set("0.0.6")
        externalRegistry.set(
            io.ktor.plugin.features.DockerImageRegistry.dockerHub(
                appName = provider { "nest2d" },
                username = providers.environmentVariable("DOCKER_HUB_USERNAME"),
                password = providers.environmentVariable("DOCKER_HUB_PASSWORD")
            )
        )
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

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")

    implementation("com.xhiteam.dxf:dxf:1.0.0")

    implementation("com.github.jchamlin:clipper-java:b4dcd50c51")
    implementation("com.google.code.gson:gson:2.10")
    implementation("org.uncommons.watchmaker:watchmaker-framework:0.7.1")
    implementation("org.apache.xmlgraphics:batik-svg-dom:1.14")
    implementation("org.apache.xmlgraphics:batik-swing:1.14")
    implementation("org.dom4j:dom4j:2.1.3")
    implementation("org.uncommons.watchmaker:watchmaker-swing:0.7.1")
    implementation("io.jenetics:jenetics:5.2.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.1")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.1")
    implementation(kotlin("stdlib-jdk8"))
}

kotlin {
    jvmToolchain(17)
}

