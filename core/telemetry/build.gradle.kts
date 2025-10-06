plugins {
    id("amulet.android.core")
}

android {
    namespace = "com.example.amulet.core.telemetry"
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.napier)
    api(platform(libs.firebase.bom))
    api(libs.firebase.analytics.ktx)
    api(libs.firebase.crashlytics.ktx)
}
