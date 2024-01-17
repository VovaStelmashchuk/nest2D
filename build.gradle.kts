plugins {
    application
    kotlin("jvm")
    id("io.ktor.plugin") version "2.3.7"
}

group = "com.nestapp"
version = "0.0.1"
description = "NestApp"

application {
    mainClass.set("com.nestapp.ApplicationKt")
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
    implementation("io.ktor:ktor-server-netty:2.3.7")

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
    jvmToolchain(8)
}

