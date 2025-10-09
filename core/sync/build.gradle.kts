plugins {
    id("amulet.android.core")
}

android {
    namespace = "com.example.amulet.core.sync"
}

dependencies {
    implementation(project(":shared"))
    implementation(project(":core:database"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlin.result)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    implementation(libs.androidx.room.ktx)

    ksp(libs.androidx.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotlin.result)
    testImplementation(libs.mockk)
    testImplementation(libs.androidx.work.testing)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.robolectric)
    testImplementation(kotlin("test"))
}
