plugins {
    id("amulet.android.core")
}

android {
    namespace = "com.example.amulet.core.network"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        buildConfigField("String", "API_BASE_URL", "\"https://api.amulet.app/v1/\"")
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.retrofit)
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:${libs.versions.kotlinxSerializationConverter.get()}")
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlin.result)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotlin.result)
    testImplementation(libs.okhttp)
    testImplementation(libs.mockwebserver)
}
