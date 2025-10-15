plugins {
    `kotlin-dsl`
}

group = "com.example.amulet.buildlogic"

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("com.android.tools.build:gradle:8.13.0")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.20")
    implementation("com.google.dagger:hilt-android-gradle-plugin:2.56")
    implementation("com.google.devtools.ksp:symbol-processing-gradle-plugin:2.2.20-2.0.4")
}

gradlePlugin {
    plugins {
        register("amuletAndroidApplication") {
            id = "amulet.android.application"
            implementationClass = "amulet.android.app.AmuletAndroidApplicationPlugin"
        }
        register("amuletAndroidLibrary") {
            id = "amulet.android.library"
            implementationClass = "amulet.android.library.AmuletAndroidLibraryPlugin"
        }
        register("amuletAndroidFeature") {
            id = "amulet.android.feature"
            implementationClass = "amulet.android.feature.AmuletAndroidFeaturePlugin"
        }
        register("amuletAndroidCore") {
            id = "amulet.android.core"
            implementationClass = "amulet.android.core.AmuletAndroidCorePlugin"
        }
        register("amuletAndroidData") {
            id = "amulet.android.data"
            implementationClass = "amulet.android.data.AmuletAndroidDataPlugin"
        }
        register("amuletSharedMultiplatform") {
            id = "amulet.kotlin.multiplatform.shared"
            implementationClass = "amulet.kmp.AmuletSharedMultiplatformPlugin"
        }
        register("amuletDependencyRules") {
            id = "amulet.dependency.rules"
            implementationClass = "amulet.dependency.DependencyRulesPlugin"
        }
    }
}
