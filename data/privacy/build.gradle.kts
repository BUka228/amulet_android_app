plugins {
    id("amulet.android.data")
}

android {
    namespace = "com.example.amulet.data.privacy"
}

dependencies {
    implementation(project(":shared"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))
    implementation(project(":core:config"))
}
