plugins {
    id("amulet.android.library")
}

android {
    namespace = "com.example.amulet.architecture"
}

dependencies {
    testImplementation(libs.archunit.junit5)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)

    testImplementation(project(":shared"))

    testImplementation(project(":core:network"))
    testImplementation(project(":core:database"))
    testImplementation(project(":core:sync"))
    testImplementation(project(":core:crypto"))
    testImplementation(project(":core:auth"))
    testImplementation(project(":core:ble"))
    testImplementation(project(":core:telemetry"))
    testImplementation(project(":core:design"))
    testImplementation(project(":core:config"))

    testImplementation(project(":data:user"))
    testImplementation(project(":data:devices"))
    testImplementation(project(":data:hugs"))
    testImplementation(project(":data:patterns"))
    testImplementation(project(":data:practices"))
    testImplementation(project(":data:rules"))
    testImplementation(project(":data:privacy"))
    testImplementation(project(":data:auth"))

    testImplementation(project(":feature:dashboard"))
    testImplementation(project(":feature:hugs"))
    testImplementation(project(":feature:patterns"))
    testImplementation(project(":feature:sessions"))
    testImplementation(project(":feature:devices"))
    testImplementation(project(":feature:settings"))
    testImplementation(project(":feature:profile"))
    testImplementation(project(":feature:onboarding"))
    testImplementation(project(":feature:pairing"))
    testImplementation(project(":feature:control-center"))
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
