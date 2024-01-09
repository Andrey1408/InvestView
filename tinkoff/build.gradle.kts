plugins {
    id("backend.kotlin-library-conventions")
}

dependencies {
    val ktorVersion = "1.6.7"
    val exposedVersion = "0.37.3"
    implementation(project(":common"))
    implementation(project(":db-service"))

    implementation("org.slf4j:slf4j-api:2.0.3")
    implementation("org.slf4j:slf4j-simple:2.0.3")
    implementation("ru.tinkoff.piapi:java-sdk-core:1.0-M3")
    implementation("ru.tinkoff.piapi:java-sdk:1.0-M3")
    implementation("io.ktor:ktor-server-netty:$ktorVersion") // ktor netty server
    implementation("io.ktor:ktor-server-core:$ktorVersion") // ktor server
    implementation("io.ktor:ktor-jackson:$ktorVersion") // jackson for ktor
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")

}