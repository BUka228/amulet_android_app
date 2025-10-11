import com.google.devtools.ksp.gradle.KspTaskJvm

plugins {
    id("amulet.android.core")
    alias(libs.plugins.ksp)
    alias(libs.plugins.protobuf)
}

tasks.withType<KspTaskJvm>().configureEach {
    dependsOn("generateDebugProto")
    dependsOn("generateReleaseProto")
}

android {
    namespace = "com.example.amulet.core.auth"
    sourceSets {
        getByName("main") {
            java.srcDir("build/generated/source/proto/main/java")
        }
        getByName("debug") {
            java.srcDir("build/generated/source/proto/debug/java")
        }
        getByName("release") {
            java.srcDir("build/generated/source/proto/release/java")
        }
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(project(":core:network"))
    implementation(libs.androidx.datastore)
    implementation(libs.protobuf.javalite)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.appcheck.ktx)
    implementation(libs.firebase.appcheck.debug)
    implementation(libs.firebase.appcheck.playintegrity)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}

protobuf {
    protoc {
        artifact = libs.protobuf.protoc.get().toString()
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                register("java") {
                    option("lite")
                }
            }
        }
    }
}
