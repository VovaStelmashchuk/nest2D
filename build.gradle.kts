plugins {
    application
    kotlin("jvm")
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
    api("com.xhiteam.dxf:dxf:1.0.0")
    api("com.github.jchamlin:clipper-java:b4dcd50c51")
    api("com.google.code.gson:gson:2.10")
    api("org.uncommons.watchmaker:watchmaker-framework:0.7.1")
    api("org.apache.xmlgraphics:batik-svg-dom:1.14")
    api("org.apache.xmlgraphics:batik-swing:1.14")
    api("org.dom4j:dom4j:2.1.3")
    api("org.uncommons.watchmaker:watchmaker-swing:0.7.1")
    api("io.jenetics:jenetics:5.2.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.1")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.1")
    implementation(kotlin("stdlib-jdk8"))
}

group = "com.yisa.util"
version = "0.0.1"
description = "nest4J"

java {
    withSourcesJar()
    withJavadocJar()
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc>() {
    options.encoding = "UTF-8"
}
kotlin {
    jvmToolchain(8)
}

