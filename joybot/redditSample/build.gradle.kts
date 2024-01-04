plugins {
    java
    application
}

repositories {
    mavenCentral()
    maven("https://jitpack.io/")
}

dependencies {
    implementation(project(":reddit"))
    implementation("com.wanony:joybot:1.0-SNAPSHOT")

    // https://mvnrepository.com/artifact/org.jetbrains/annotations
    implementation("org.jetbrains:annotations:24.1.0")
}