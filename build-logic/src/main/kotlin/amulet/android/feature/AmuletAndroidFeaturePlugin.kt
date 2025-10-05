package amulet.android.feature

import amulet.android.common.configureKotlinAndroid
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AmuletAndroidFeaturePlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("amulet.android.library")
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
        pluginManager.apply("com.google.dagger.hilt.android")
        pluginManager.apply("com.google.devtools.ksp")

        extensions.configure<LibraryExtension> {
            configureKotlinAndroid(this, enableCompose = true)
        }
    }
}
