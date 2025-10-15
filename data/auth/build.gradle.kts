plugins {
    id("amulet.android.data")
}

android {
    namespace = "com.example.amulet.data.auth"
}

dependencies {
    implementation(project(":shared"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))
    implementation(project(":core:auth"))
    implementation(project(":core:supabase"))
    implementation(libs.kotlin.result)
    implementation(libs.androidx.room.runtime)
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.kotlin.result)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
}
