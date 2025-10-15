plugins {
    id("amulet.android.data")
}

android {
    namespace = "com.example.amulet.data.notifications"
}

dependencies {
    implementation(project(":shared"))
    implementation(project(":core:network"))
    implementation(libs.kotlin.result)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.datetime)
}
