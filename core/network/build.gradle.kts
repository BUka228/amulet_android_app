plugins {
    id("amulet.android.core")
}

android {
    namespace = "com.example.amulet.core.network"
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.retrofit)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlin.result)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotlin.result)
    testImplementation(libs.okhttp)
}
