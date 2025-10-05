package amulet.android.common

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.getByType

internal const val ANDROID_COMPILE_SDK = 36
internal const val ANDROID_MIN_SDK = 26

internal fun Project.configureKotlinAndroid(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
    enableCompose: Boolean
) {
    commonExtension.apply {
        compileSdk = ANDROID_COMPILE_SDK
        defaultConfig {
            minSdk = ANDROID_MIN_SDK
        }
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
        packaging.resources.excludes.addAll(
            listOf("META-INF/AL2.0", "META-INF/LGPL2.1")
        )
        lint {
            disable.add("PropertyEscape")
        }
        if (enableCompose) {
            buildFeatures.compose = true
        }
    }

    extensions.findByType<KotlinAndroidProjectExtension>()?.apply {
        jvmToolchain(21)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            freeCompilerArgs.add("-Xjsr305=strict")
        }
    }

    val toolchainService = extensions.findByType<JavaToolchainService>()
    val toolchainCompiler = toolchainService?.compilerFor {
        languageVersion.set(JavaLanguageVersion.of(21))
    }

    if (toolchainCompiler != null) {
        tasks.withType(JavaCompile::class.java).configureEach {
            javaCompiler.set(toolchainCompiler)
        }
    }
}

internal fun Project.configureUnitTestDependencies() {
    val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

    dependencies {
        add("testImplementation", libs.findLibrary("junit.jupiter.api").get())
        add("testImplementation", libs.findLibrary("mockk").get())
        add("testImplementation", libs.findLibrary("turbine").get())
        add("testImplementation", libs.findLibrary("kotlinx.coroutines.test").get())
        add("testImplementation", libs.findLibrary("koin.test").get())
        add("testImplementation", libs.findLibrary("koin.test.junit5").get())
        add("testRuntimeOnly", libs.findLibrary("junit.jupiter.engine").get())
    }

    tasks.withType(Test::class.java).configureEach {
        useJUnitPlatform()
    }
}
