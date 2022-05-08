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
    maven("https://jitpack.io/")
}

val webscrape = sourceSets.create("webscrape") {
    compileClasspath += sourceSets["main"].output
    runtimeClasspath += sourceSets["main"].output
    compileClasspath += sourceSets["main"].compileClasspath
    runtimeClasspath += sourceSets["main"].runtimeClasspath
}

val webscrapeImplementation = configurations.getting {
    extendsFrom(configurations["implementation"])
}

dependencies {
    implementation("org.slf4j:slf4j-simple:2.0.0-alpha7")
    implementation("mysql:mysql-connector-java:8.0.28")
    implementation("net.dv8tion:JDA:5.0.0-alpha.11")
    implementation("com.github.minndevelopment:jda-ktx:78e74bc45b8d73a5d7974ef0d5f8efdd5d97910f")
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

    add("webscrapeImplementation", "org.jsoup:jsoup:1.14.3")
    add("webscrapeImplementation", "org.seleniumhq.selenium:selenium-java:2.41.0")
    add("webscrapeImplementation", "org.seleniumhq.selenium:selenium-firefox-driver:4.1.4")

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
    group = "joyutils"
    mainClass.set("com.wanony.utils.CreateDatabase")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("setupProperties") {
    group = "joyutils"
    mainClass.set("com.wanony.utils.SetupProperties")
    classpath = sourceSets["main"].runtimeClasspath
    standardInput = System.`in`
}

tasks.register<JavaExec>("populateDatabase") {
    group = "joyutils"
    mainClass.set("com.wanony.utils.PopulateDatabase")
    classpath = sourceSets["webscrape"].runtimeClasspath
}

application {
    mainClass.set("com.wanony.JoyBotKt")
}