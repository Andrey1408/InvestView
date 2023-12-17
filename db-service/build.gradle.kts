plugins {
    id("backend.kotlin-application-conventions")
}

repositories {
    mavenCentral()
}
val ktorVersion = "1.6.8"
dependencies {
    implementation("org.postgresql:postgresql:42.2.2")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation(project(mapOf("path" to ":tinkoff")))
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.exposed:exposed-core:0.37.3")
    implementation("org.jetbrains.exposed:exposed-dao:0.37.3")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.37.3")
    implementation("org.jetbrains.exposed:exposed-java-time:0.37.3")
    implementation("io.ktor:ktor-html-builder:$ktorVersion")
    implementation("io.ktor:ktor-serialization:$ktorVersion")
    implementation("io.ktor:ktor-auth:$ktorVersion")
    implementation("io.ktor:ktor-auth-jwt:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
}
kotlin {
    jvmToolchain(17)
}