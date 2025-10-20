plugins {
    id("amulet.android.feature")
}

android {
    namespace = "com.example.amulet.feature.auth"
}

dependencies {
    
    // Специфичные для Auth модуля
    implementation(libs.kotlin.result)
    implementation(libs.play.services.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.google.identity.googleid)
    
    // Tests
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
}
