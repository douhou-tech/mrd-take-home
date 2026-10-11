import org.gradle.api.Plugin
import org.gradle.api.Project

class DatabaseConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.library")
            pluginManager.apply("com.google.devtools.ksp")
        }
        target
            .implementation(
                "androidx-room-runtime",
                "androidx-room-ktx"
            )
            .ksp("androidx-room-compiler")
            .testImplementation("androidx-room-testing")
    }

}

