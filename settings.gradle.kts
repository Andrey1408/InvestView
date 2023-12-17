plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
rootProject.name = "InvestView"
include("tinkoff")
include("serverAPI")
include("user-service")
include("common")
include("db-service")
