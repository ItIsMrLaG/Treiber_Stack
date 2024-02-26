plugins {
    kotlin("jvm") version "1.9.0"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    api("org.apache.commons:commons-math3:3.6.1")
    implementation("com.google.guava:guava:33.0.0-jre")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("MainKt")
}
