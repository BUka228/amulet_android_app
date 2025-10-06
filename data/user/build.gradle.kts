plugins {
    id("amulet.android.data")
}

android {
    namespace = "com.example.amulet.data.user"
}

dependencies {
    implementation(project(":shared"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))
}
