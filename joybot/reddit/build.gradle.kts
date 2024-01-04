plugins {
    id("java-library")
}

group = "com.wanony.joy.reddit"
version = "1.0-SNAPSHOT"

dependencies {
    implementation("com.google.oauth-client:google-oauth-client:1.33.3")
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.33.3")

    // https://mvnrepository.com/artifact/org.jetbrains/annotations
    implementation("org.jetbrains:annotations:24.1.0")
}