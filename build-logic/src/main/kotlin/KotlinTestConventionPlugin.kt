import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

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

internal fun Project.testImplementation(vararg libs: String) {
    val versionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")
    dependencies {
        libs.forEach {
            add("testImplementation", versionCatalog.findLibrary(it).get())
        }
    }
}