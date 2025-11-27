plugins {
    id("amulet.android.feature")
}

android {
    namespace = "com.example.amulet.feature.practices"
}

dependencies {
    implementation(project(":shared"))
    implementation(project(":core:design"))
    implementation(project(":core:auth"))
    implementation(project(":core:foreground"))
    implementation(project(":data:practices"))
    implementation(project(":data:courses"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material3)
    implementation(libs.kotlinx.datetime)
    implementation(libs.coil.compose)
}
