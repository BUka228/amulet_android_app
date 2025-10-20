package amulet.android.feature

import amulet.android.common.configureKotlinAndroid
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class AmuletAndroidFeaturePlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("amulet.android.library")
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
        pluginManager.apply("com.google.devtools.ksp")

        extensions.configure<LibraryExtension> {
            configureKotlinAndroid(this, enableCompose = true)
        }

        val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

        dependencies {
            // Core dependencies
            add("implementation", project(":shared"))
            add("implementation", project(":core:design"))

            // Compose
            add("implementation", platform(libs.findLibrary("androidx.compose.bom").get()))
            add("implementation", libs.findLibrary("androidx.compose.material3").get())
            add("implementation", libs.findLibrary("androidx.compose.ui").get())
            add("implementation", libs.findLibrary("androidx.compose.ui.tooling.preview").get())
            add("implementation", libs.findLibrary("androidx.compose.foundation").get())
            add("implementation", libs.findLibrary("androidx.compose.material.icons.extended").get())
            add("implementation", libs.findLibrary("androidx.compose.animation").get())

            // Navigation
            add("implementation", libs.findLibrary("androidx.navigation.compose").get())

            // Hilt
            add("implementation", libs.findLibrary("hilt.android").get())
            add("ksp", libs.findLibrary("hilt.compiler").get())
            add("implementation", libs.findLibrary("androidx.hilt.navigation.compose").get())

            // Lifecycle
            add("implementation", libs.findLibrary("androidx.lifecycle.runtime.ktx").get())
            add("implementation", libs.findLibrary("androidx.lifecycle.runtime.compose").get())
            add("implementation", libs.findLibrary("androidx.lifecycle.viewmodel.compose").get())

            // Activity Compose
            add("implementation", libs.findLibrary("androidx.activity.compose").get())

            // Debug
            add("debugImplementation", libs.findLibrary("androidx.compose.ui.tooling").get())
        }
    }
}
