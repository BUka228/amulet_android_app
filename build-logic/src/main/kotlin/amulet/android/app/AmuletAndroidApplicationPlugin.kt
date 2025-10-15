package amulet.android.app

import amulet.android.common.ANDROID_COMPILE_SDK
import amulet.android.common.configureKotlinAndroid
import amulet.android.common.configureUnitTestDependencies
import com.android.build.api.dsl.ApplicationExtension
import dagger.hilt.android.plugin.HiltExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class AmuletAndroidApplicationPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.application")
        pluginManager.apply("org.jetbrains.kotlin.android")
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
        pluginManager.apply("com.google.dagger.hilt.android")
        pluginManager.apply("com.google.devtools.ksp")

        extensions.configure<HiltExtension> {
            enableAggregatingTask = true
        }

        extensions.configure<ApplicationExtension> {
            configureKotlinAndroid(this, enableCompose = true)
            buildFeatures {
                buildConfig = true
            }
            defaultConfig {
                targetSdk = ANDROID_COMPILE_SDK
            }
        }

        val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

        dependencies {
            add("implementation", libs.findLibrary("hilt-android").get())
            add("ksp", libs.findLibrary("hilt-compiler").get())
        }

        configureUnitTestDependencies()
    }
}
