package amulet.android.app

import amulet.android.common.ANDROID_COMPILE_SDK
import amulet.android.common.configureKotlinAndroid
import amulet.android.common.configureUnitTestDependencies
import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AmuletAndroidApplicationPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.application")
        pluginManager.apply("org.jetbrains.kotlin.android")
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
        pluginManager.apply("com.google.dagger.hilt.android")
        pluginManager.apply("com.google.devtools.ksp")

        extensions.configure<ApplicationExtension> {
            configureKotlinAndroid(this, enableCompose = true)
            buildFeatures {
                buildConfig = true
            }
            defaultConfig {
                targetSdk = ANDROID_COMPILE_SDK
            }
        }

        configureUnitTestDependencies()
    }
}
