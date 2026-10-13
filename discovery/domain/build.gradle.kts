plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.mrd.kotlin.unit.tests)
    alias(libs.plugins.kotlin.serialization)
}
java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}
dependencies {
    implementation(project(":discovery:core:primitives"))
    implementation(project(":discovery:test"))
    implementation(libs.koin.core)
    implementation(libs.kotlinx.serialization.json)
}

