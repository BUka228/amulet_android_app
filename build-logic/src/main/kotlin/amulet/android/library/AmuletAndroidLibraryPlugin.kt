package amulet.android.library

import amulet.android.common.configureKotlinAndroid
import amulet.android.common.configureUnitTestDependencies
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AmuletAndroidLibraryPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")
        pluginManager.apply("org.jetbrains.kotlin.android")

        extensions.configure<LibraryExtension> {
            configureKotlinAndroid(this, enableCompose = false)
        }

        configureUnitTestDependencies()
    }
}
