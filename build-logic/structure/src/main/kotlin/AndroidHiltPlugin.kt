import com.leos.droidify.getLibrary
import com.leos.droidify.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidHiltPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.google.dagger.hilt.android")
                apply("com.google.devtools.ksp")
            }

            dependencies {
                add("implementation", libs.getLibrary("hilt.android"))
                add("ksp", libs.getLibrary("hilt.compiler"))
            }
        }
    }
}
