import org.gradle.api.Plugin
import org.gradle.api.Project

class KotlinTestConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.testImplementation(
            "junit",
            "mockk",
            "kotest-assertions-core",
            "kotlinx-coroutines-test",
            "koin-test",
            "koin-test-junit4"
        )
    }

}