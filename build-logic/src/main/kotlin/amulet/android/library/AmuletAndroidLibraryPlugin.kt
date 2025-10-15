package amulet.android.library

import amulet.android.common.configureKotlinAndroid
import amulet.android.common.configureUnitTestDependencies
import com.android.build.api.dsl.LibraryExtension
import dagger.hilt.android.plugin.HiltExtension
import org.jetbrains.kotlin.gradle.plugin.KaptExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class AmuletAndroidLibraryPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")
        pluginManager.apply("org.jetbrains.kotlin.android")
        pluginManager.apply("com.google.dagger.hilt.android")
        pluginManager.apply("org.jetbrains.kotlin.kapt")
        pluginManager.apply("com.google.devtools.ksp")

        extensions.configure<HiltExtension> {
            enableAggregatingTask = true
        }

        extensions.configure<KaptExtension> {
            correctErrorTypes = true
        }

        extensions.configure<LibraryExtension> {
            configureKotlinAndroid(this, enableCompose = false)
        }

        val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

        dependencies {
            add("implementation", libs.findLibrary("hilt-android").get())
            add("kapt", libs.findLibrary("hilt-compiler").get())
        }

        configureUnitTestDependencies()
    }
}
