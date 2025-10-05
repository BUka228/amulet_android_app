plugins {
    id("amulet.android.data")
}

android {
    namespace = "com.example.amulet.data.patterns"
}

dependencies {
    implementation(project(":shared"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))
    implementation(project(":core:config"))
    implementation(libs.koin.core)
}
