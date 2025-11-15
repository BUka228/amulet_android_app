plugins {
    id("amulet.android.data")
}

android {
    namespace = "com.example.amulet.data.courses"
}

dependencies {
    implementation(project(":shared"))
    implementation(project(":core:database"))
    implementation(project(":core:network"))
}
