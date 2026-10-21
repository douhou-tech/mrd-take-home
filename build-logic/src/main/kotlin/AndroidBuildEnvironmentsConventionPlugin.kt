import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidBuildEnvironmentsConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val hasApp = pluginManager.hasPlugin("com.android.application")
            val hasLib = pluginManager.hasPlugin("com.android.library")

            if (hasApp) {
                extensions.configure<ApplicationExtension> {
                    buildFeatures {
                        resValues = true
                    }
                    flavorDimensions += "environment"
                    productFlavors {
                        create("dev") {
                            dimension = "environment"
                            applicationIdSuffix = ".dev"
                            versionNameSuffix = "-dev"
                            resValue("string", "app_name", "\"LikeABite (dev)\"")
                        }
                        create("staging") {
                            dimension = "environment"
                            applicationIdSuffix = ".staging"
                            versionNameSuffix = "-staging"
                            resValue("string", "app_name", "\"LikeABite (staging)\"")
                        }
                        create("prod") {
                            dimension = "environment"
                            resValue("string", "app_name", "\"LikeABite (prod)\"")
                        }
                    }
                }
            } else if (hasLib) {
                extensions.configure<LibraryExtension> {
                    flavorDimensions += "environment"
                    productFlavors {
                        create("dev") {
                            dimension = "environment"
                        }
                        create("staging") {
                            dimension = "environment"
                        }
                        create("prod") {
                            dimension = "environment"
                        }
                    }
                }
            }
        }
    }
}
