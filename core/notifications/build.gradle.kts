plugins {
    id("amulet.android.core")
}

android {
    namespace = "com.example.amulet.core.notifications"
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.onesignal)
    implementation(libs.kotlinx.coroutines.core)
}
