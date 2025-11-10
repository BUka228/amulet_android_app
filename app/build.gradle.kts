import java.util.Properties

plugins {
    id("amulet.android.application")
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use(::load)
    }
}

fun prop(key: String, default: String = ""): String =
    (localProperties.getProperty(key)?.takeIf { it.isNotBlank() } ?: default)
        .replace("\"", "\\\"")

android {
    namespace = "com.example.amulet_android_app"

    defaultConfig {
        applicationId = "com.example.amulet_android_app"
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "SUPABASE_URL", "\"${prop("SUPABASE_URL")}\"")
        buildConfigField("String", "SUPABASE_REST_URL", "\"${prop("SUPABASE_REST_URL", "https://api.amulet.app/v2")}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${prop("SUPABASE_ANON_KEY")}\"")
        buildConfigField("String", "TURNSTILE_SITE_KEY", "\"${prop("TURNSTILE_SITE_KEY")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(project(":shared"))
    implementation(project(":core:design"))
    implementation(project(":core:auth"))
    implementation(project(":core:telemetry"))
    implementation(project(":core:supabase"))
    implementation(project(":core:turnstile"))
    implementation(project(":data:auth"))
    implementation(project(":data:devices"))
    implementation(project(":data:hugs"))
    implementation(project(":data:patterns"))
    implementation(project(":data:practices"))
    implementation(project(":data:privacy"))
    implementation(project(":data:rules"))
    implementation(project(":data:user"))
    implementation(project(":data:telemetry"))
    implementation(project(":feature:auth"))
    implementation(project(":feature:dashboard"))
    implementation(project(":feature:devices"))
    implementation(project(":feature:patterns"))
    implementation(project(":feature:onboarding"))
    implementation(libs.koin.android)
    implementation(libs.kotlinx.datetime)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.core)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}