package amulet.android.data

import amulet.android.common.configureKotlinAndroid
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AmuletAndroidDataPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("amulet.android.library")

        extensions.configure<LibraryExtension> {
            configureKotlinAndroid(this, enableCompose = false)
        }
    }
}
