pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "amulet_android_app"
includeBuild("build-logic")

include(":app")
include(":shared")
include(":detekt-rules")
include(":architecture-test")

include(
    ":core:network",
    ":core:database",
    ":core:sync",
    ":core:crypto",
    ":core:auth",
    ":core:supabase",
    ":core:ble",
    ":core:telemetry",
    ":core:design",
    ":core:config",
    ":core:notifications",
    ":core:turnstile"
)

include(
    ":data:user",
    ":data:devices",
    ":data:hugs",
    ":data:patterns",
    ":data:practices",
    ":data:courses",
    ":data:rules",
    ":data:privacy",
    ":data:auth",
    ":data:telemetry"
)

include(
    ":feature:dashboard",
    ":feature:hugs",
    ":feature:patterns",
    ":feature:practices",
    ":feature:sessions",
    ":feature:devices",
    ":feature:settings",
    ":feature:profile",
    ":feature:onboarding",
    ":feature:pairing",
    ":feature:control-center",
    ":feature:auth"
)

project(":shared").projectDir = file("shared")
project(":detekt-rules").projectDir = file("detekt-rules")
project(":architecture-test").projectDir = file("architecture-test")

project(":core:network").projectDir = file("core/network")
project(":core:database").projectDir = file("core/database")
project(":core:sync").projectDir = file("core/sync")
project(":core:crypto").projectDir = file("core/crypto")
project(":core:auth").projectDir = file("core/auth")
project(":core:supabase").projectDir = file("core/supabase")
project(":core:ble").projectDir = file("core/ble")
project(":core:telemetry").projectDir = file("core/telemetry")
project(":core:design").projectDir = file("core/design")
project(":core:config").projectDir = file("core/config")
project(":core:turnstile").projectDir = file("core/turnstile")
project(":core:notifications").projectDir = file("core/notifications")

project(":data:user").projectDir = file("data/user")
project(":data:devices").projectDir = file("data/devices")
project(":data:hugs").projectDir = file("data/hugs")
project(":data:patterns").projectDir = file("data/patterns")
project(":data:practices").projectDir = file("data/practices")
project(":data:courses").projectDir = file("data/courses")
project(":data:rules").projectDir = file("data/rules")
project(":data:privacy").projectDir = file("data/privacy")
project(":data:auth").projectDir = file("data/auth")
project(":data:telemetry").projectDir = file("data/telemetry")

project(":feature:dashboard").projectDir = file("feature/dashboard")
project(":feature:hugs").projectDir = file("feature/hugs")
project(":feature:patterns").projectDir = file("feature/patterns")
project(":feature:practices").projectDir = file("feature/practices")
project(":feature:sessions").projectDir = file("feature/sessions")
project(":feature:devices").projectDir = file("feature/devices")
project(":feature:settings").projectDir = file("feature/settings")
project(":feature:profile").projectDir = file("feature/profile")
project(":feature:onboarding").projectDir = file("feature/onboarding")
project(":feature:pairing").projectDir = file("feature/pairing")
project(":feature:control-center").projectDir = file("feature/control-center")
project(":feature:auth").projectDir = file("feature/auth")
