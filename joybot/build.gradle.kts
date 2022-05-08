import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktorVersion: String by project
val kotlinVersion: String by project
val exposedVersion: String by project

plugins {
    kotlin("jvm") version "1.6.20"
    application
}

group = "com.wanony"
version = "1.0-SNAPSHOT"



repositories {
    mavenCentral()
}

dependencies {
    implementation("org.slf4j:slf4j-simple:2.0.0-alpha7")
    implementation("mysql:mysql-connector-java:8.0.28")
    implementation("net.dv8tion:JDA:5.0.0-alpha.11")
    implementation("com.zaxxer:HikariCP:4.0.3")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")

    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-freemarker:$ktorVersion")
    implementation("io.ktor:ktor-server-locations:$ktorVersion")
    implementation("io.ktor:ktor-server-conditional-headers:$ktorVersion")
    implementation("io.ktor:ktor-server-default-headers:$ktorVersion")
    implementation("io.ktor:ktor-server-partial-content:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging:$ktorVersion")

    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"

}

tasks.register<JavaExec>("createDatabase") {
    mainClass.set("com.wanony.utils.CreateDatabase")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("setupProperties") {
    mainClass.set("com.wanony.utils.SetupProperties")
    classpath = sourceSets["main"].runtimeClasspath
    standardInput = System.`in`
}

application {
    mainClass.set("com.wanony.JoyBotKt")
}