package amulet.android.core

import amulet.android.common.configureKotlinAndroid
import amulet.android.common.configureUnitTestDependencies
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AmuletAndroidCorePlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("amulet.android.library")

        extensions.configure<LibraryExtension> {
            configureKotlinAndroid(this, enableCompose = false)
        }

        configureUnitTestDependencies()
    }
}
