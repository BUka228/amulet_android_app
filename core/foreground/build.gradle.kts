plugins {
    id("amulet.android.core")
}

android {
    namespace = "com.example.amulet.core.foreground"
}

dependencies {
    implementation(project(":shared"))
    implementation(project(":core:notifications"))
    implementation(libs.kotlinx.coroutines.core)
}
