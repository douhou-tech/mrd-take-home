plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.mrd.android.build.environments)
    alias(libs.plugins.mrd.android.database)
    alias(libs.plugins.mrd.kotlin.unit.tests)
}

android {
    namespace = "com.mrd.restaurant.discovery.data"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 28

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

}

dependencies {
    implementation(project(":discovery:core:primitives"))
    implementation(project(":discovery:domain"))
    implementation(project(":discovery:test"))
    implementation(libs.koin.android)
    implementation(libs.kotlinx.serialization.json)
}