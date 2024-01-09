plugins {
    id("backend.kotlin-library-conventions")
}


dependencies {
    val kodeinVersion = "7.9.0"
    implementation(project(":db-service"))
    implementation(project(mapOf("path" to ":tinkoff")))
    implementation("ru.tinkoff.piapi:java-sdk-core:1.0-M3")
    implementation("ru.tinkoff.piapi:java-sdk:1.0-M3")

    implementation("org.kodein.di:kodein-di-framework-ktor-server-jvm:$kodeinVersion")
}