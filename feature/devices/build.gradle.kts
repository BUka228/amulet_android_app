plugins {
    id("amulet.android.feature")
}

android {
    namespace = "com.example.amulet.feature.devices"
}

dependencies {
    implementation(project(":shared"))
    implementation(project(":core:design"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    
    // CameraX для QR сканирования
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    
    // ML Kit Barcode Scanning
    implementation(libs.mlkit.barcode.scanning)
    
    // Accompanist для Compose разрешений
    implementation(libs.accompanist.permissions)
    
    // Coroutines для async операций
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.play.services)
    
    debugImplementation(libs.androidx.compose.ui.tooling)
}
