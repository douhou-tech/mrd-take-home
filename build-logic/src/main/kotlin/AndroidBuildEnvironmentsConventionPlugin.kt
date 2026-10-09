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
                    flavorDimensions += "environment"
                    productFlavors {
                        create("dev") {
                            dimension = "environment"
                            applicationIdSuffix = ".dev"
                            versionNameSuffix = "-dev"
                        }
                        create("staging") {
                            dimension = "environment"
                            applicationIdSuffix = ".staging"
                            versionNameSuffix = "-staging"
                        }
                        create("prod") {
                            dimension = "environment"
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
