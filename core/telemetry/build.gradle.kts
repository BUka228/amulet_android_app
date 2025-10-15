plugins {
    id("amulet.android.core")
}

android {
    namespace = "com.example.amulet.core.telemetry"
}

dependencies {
    implementation(project(":shared"))
    implementation(project(":core:network"))
    implementation(libs.napier)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
}
