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
    api(project(":shared"))
    api(project(":core:network"))
    implementation(libs.kotlinx.datetime)
    implementation(libs.androidx.datastore)
    implementation(libs.protobuf.javalite)
    api(project(":core:supabase"))
    implementation(libs.supabase.auth)
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
