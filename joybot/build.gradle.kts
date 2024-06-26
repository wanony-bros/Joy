import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktorVersion: String by project
val kotlinVersion: String by project
val exposedVersion: String by project

plugins {
    kotlin("jvm") version "1.6.20"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("java")
    application
}

group = "com.wanony"
version = "1.0-SNAPSHOT"

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
    implementation("net.dv8tion:JDA:5.0.0-beta.13")
    implementation("com.github.minndevelopment:jda-ktx:9370cb13cc64646862e6f885959d67eb4b157e4a")
    implementation("com.zaxxer:HikariCP:4.0.3")


    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")

    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-freemarker:$ktorVersion")
    implementation("io.ktor:ktor-server-locations:$ktorVersion")
    implementation("io.ktor:ktor-server-conditional-headers:$ktorVersion")
    implementation("io.ktor:ktor-server-default-headers:$ktorVersion")
    implementation("io.ktor:ktor-server-partial-content:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging:$ktorVersion")

    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")

    implementation("com.github.instagram4j:instagram4j:2.0.7")

    implementation("com.github.twitch4j:twitch4j:1.12.0")
    implementation(project("data"))
    implementation(project("reddit"))

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
    mainClass.set("com.wanony.joy.data.util.CreateDatabase")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("migrateFromOldDB") {
    group = "joyutils"
    mainClass.set("com.wanony.joy.data.util.MigrateFromOldDatabase")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.register<JavaExec>("setupProperties") {
    group = "joyutils"
    mainClass.set("com.wanony.joy.discord.util.SetupProperties")
    classpath = sourceSets["main"].runtimeClasspath
    standardInput = System.`in`
}

tasks.register<JavaExec>("populateDatabase") {
    group = "joyutils"
    mainClass.set("com.wanony.joy.util.PopulateDatabase")
    classpath = sourceSets["webscrape"].runtimeClasspath
}

application {
    mainClass.set("com.wanony.joy.discord.JoyBotKt")
}