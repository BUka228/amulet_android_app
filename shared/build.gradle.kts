plugins {
    id("amulet.kotlin.multiplatform.shared")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.koin.core)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
            }
        }
        val androidUnitTest by getting {
            dependencies {
                implementation(libs.junit.jupiter.api)
                implementation(libs.junit.jupiter.params)
                implementation(libs.mockk)
                implementation(libs.turbine)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }
}

android {
    namespace = "com.example.amulet.shared"
}
