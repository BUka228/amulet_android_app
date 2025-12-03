plugins {
    id("amulet.android.feature")
}

android {
    namespace = "com.example.amulet.feature.hugs"
}

dependencies {
    implementation(project(":shared"))
    implementation(project(":core:design"))
    implementation(project(":feature:patterns"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.kotlinx.datetime)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.zxing.core)
}
