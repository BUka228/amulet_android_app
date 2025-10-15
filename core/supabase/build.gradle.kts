plugins {
    id("amulet.android.core")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.amulet.core.supabase"
}

dependencies {
    implementation(project(":shared"))
    api(platform(libs.supabase.bom))
    api(libs.supabase.kt)
    api(libs.supabase.auth)
    api(libs.supabase.functions)
    api(libs.supabase.storage)
    implementation(libs.kotlinx.coroutines.core)
}
