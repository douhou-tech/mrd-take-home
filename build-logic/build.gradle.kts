plugins {
    `kotlin-dsl`
    id("java-gradle-plugin")
}

group = "com.mrd.buildlogic"

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
}

gradlePlugin {
    plugins {
        create("androidFlavors") {
            id = "mrd.android.build.environments"
            implementationClass = "AndroidBuildEnvironmentsConventionPlugin"
        }
    }
}
