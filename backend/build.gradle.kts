plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.9.0"
    application
}


dependencies {
    implementation("org.json:json:20240303")
    implementation("io.ktor:ktor-server-core:2.3.12")
    implementation("io.ktor:ktor-server-netty:2.3.12")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.12")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.12")

    implementation("com.google.firebase:firebase-admin:9.3.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-guava:1.8.1")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("ch.qos.logback:logback-classic:1.4.14")
}

application {
    mainClass.set("com.example.moneybuddy.backend.ApplicationKt")
}