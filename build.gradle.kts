// Top-level build file where you can add configuration options common to all sub-projects/modules.
import io.gitlab.arturbosch.detekt.extensions.DetektExtension

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    id("amulet.dependency.rules")
}

configure<DetektExtension> {
    config.setFrom(files("${projectDir}/detekt.yml"))
    buildUponDefaultConfig = true
    allRules = false
    autoCorrect = false
    basePath = projectDir.toString()
    parallel = true
}

dependencies {
    detektPlugins(project(":detekt-rules"))
    detektPlugins(libs.detekt.formatting)
}