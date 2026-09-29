plugins {
    kotlin("plugin.serialization")
}

dependencies {
    implementation(project(":server:common"))
    implementation(project(":server:cache"))
    implementation(project(":server:engine"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:_")
}