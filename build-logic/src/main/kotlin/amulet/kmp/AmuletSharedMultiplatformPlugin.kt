package amulet.kmp

import amulet.android.common.configureKotlinAndroid
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

class AmuletSharedMultiplatformPlugin : Plugin<Project> {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        pluginManager.apply("com.android.library")

        extensions.configure<LibraryExtension> {
            configureKotlinAndroid(this, enableCompose = false)
        }

        extensions.configure<KotlinMultiplatformExtension> {
            androidTarget() {
                compilations.configureEach {
                    compileTaskProvider.configure {
                        if (this is KotlinJvmCompile) {
                            compilerOptions {
                                jvmTarget.set(JvmTarget.JVM_17)
                            }
                        }
                    }
                }
            }
            jvm() {
                compilations.configureEach {
                    compileTaskProvider.configure {
                        if (this is KotlinJvmCompile) {
                            compilerOptions {
                                jvmTarget.set(JvmTarget.JVM_17)
                            }
                        }
                    }
                }
            }
            jvmToolchain(21)

            targetHierarchy.default()

            sourceSets.apply {
                maybeCreate("commonMain")
                maybeCreate("commonTest")
            }
        }
    }
}
