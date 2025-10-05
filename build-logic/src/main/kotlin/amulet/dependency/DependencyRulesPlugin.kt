package amulet.dependency

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency

class DependencyRulesPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        require(target == target.rootProject) {
            "amulet.dependency.rules must be applied only to the root project"
        }

        target.gradle.projectsEvaluated {
            target.rootProject.subprojects.forEach { module ->
                module.configurations
                    .matching { it.name.endsWith("Implementation") || it.name.endsWith("Api") }
                    .forEach { configuration ->
                        configuration.dependencies
                            .withType(ProjectDependency::class.java)
                            .forEach { dependency ->
                                enforceDependencyRules(module, dependency.dependencyProject)
                            }
                    }
            }
        }
    }

    private fun enforceDependencyRules(from: Project, to: Project) {
        val violations = mutableListOf<String>()
        val fromPath = from.path
        val toPath = to.path

        if (fromPath.startsWith(":feature:")) {
            if (toPath.startsWith(":feature:")) {
                violations += "Feature modules must not depend on other feature modules ($fromPath -> $toPath)."
            }
            if (toPath.startsWith(":data:")) {
                violations += "Feature modules must depend on shared abstractions, not data modules directly ($fromPath -> $toPath)."
            }
        }

        if (fromPath.startsWith(":data:") && toPath.startsWith(":feature:")) {
            violations += "Data modules must not depend on feature modules ($fromPath -> $toPath)."
        }

        if (fromPath.startsWith(":data:") && toPath == ":app") {
            violations += "Data modules must not depend on :app module ($fromPath -> $toPath)."
        }

        if (fromPath == ":shared" && toPath.startsWith(":core:")) {
            violations += "Shared module must remain platform agnostic and cannot depend on core Android modules ($fromPath -> $toPath)."
        }

        if (fromPath == ":shared" && toPath.startsWith(":feature:")) {
            violations += "Shared module must not depend on feature modules ($fromPath -> $toPath)."
        }

        if (fromPath == ":shared" && toPath == ":app") {
            violations += "Shared module must not depend on :app ($fromPath -> $toPath)."
        }

        if (violations.isNotEmpty()) {
            error(violations.joinToString(System.lineSeparator()))
        }
    }
}
