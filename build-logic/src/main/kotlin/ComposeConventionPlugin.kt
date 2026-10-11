import org.gradle.api.Plugin
import org.gradle.api.Project

class ComposeConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target
            .platformImplementation("androidx-compose-bom")
            .implementation(
                "androidx-core-ktx",
                "androidx-lifecycle-runtime-ktx",
                "androidx-activity-compose",
                "androidx-compose-ui",
                "androidx-compose-ui-graphics",
                "androidx-compose-ui-tooling-preview",
                "androidx-compose-material3"
            )
    }

}
