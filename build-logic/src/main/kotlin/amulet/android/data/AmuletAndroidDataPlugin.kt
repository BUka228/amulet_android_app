package amulet.android.data

import amulet.android.common.configureKotlinAndroid
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class AmuletAndroidDataPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("amulet.android.library")

        extensions.configure<LibraryExtension> {
            configureKotlinAndroid(this, enableCompose = false)
        }

        val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

        dependencies {
            add("implementation", libs.findLibrary("kotlin.result").get())
        }
    }
}
