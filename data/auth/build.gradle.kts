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
    implementation(libs.koin.core)
}
