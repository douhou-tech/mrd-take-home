import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

internal fun Project.testImplementation(vararg libs: String) = addDependencies("testImplementation", *libs)
internal fun Project.implementation(vararg libs: String) = addDependencies("implementation", *libs)
internal fun Project.ksp(vararg libs: String) = addDependencies("ksp", *libs)

private fun Project.addDependencies(tag: String, vararg libs: String): Project {
    val versionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")
    dependencies {
        libs.forEach {
            add(tag, versionCatalog.findLibrary(it).get())
        }
    }
    return this
}
