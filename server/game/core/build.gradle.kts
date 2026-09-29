plugins {
    kotlin("plugin.serialization")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:_")
}

// Package the existing spawn survey once, without a second checked-in copy.
tasks.processResources {
    from(rootProject.file("toolbox/src/main/resources/npc_spawns.json")) {
        into("data")
    }
}
