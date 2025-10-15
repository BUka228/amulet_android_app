plugins {
    id("amulet.android.core")
}

android {
    namespace = "com.example.amulet.core.turnstile"
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.kotlinx.coroutines.core)
}
