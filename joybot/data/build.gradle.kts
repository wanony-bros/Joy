val kotlinVersion: String by project
val exposedVersion: String by project

plugins {
    kotlin("jvm")
    id("java-library")
}

group = "com.wanony.joy"
version = "1.0-SNAPSHOT"

dependencies {
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
}