package com.example.amulet.architecture

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import org.junit.jupiter.api.Test

class LayerDependenciesTest {
    private val importedClasses = ClassFileImporter()
        .withImportOption(ImportOption.DoNotIncludeTests())
        .importPackages("com.example.amulet")

    @Test
    fun `feature modules depend only on shared abstractions and core design`() {
        classes()
            .that().resideInAPackage("..feature..")
            .should().onlyDependOnClassesThat().resideInAnyPackage(
                "..feature..",
                "..shared..",
                "..core.design..",
                "kotlin..",
                "kotlinx..",
                "java..",
                "javax..",
                "android..",
                "androidx..",
                "dagger..",
                "dagger.hilt..",
                "org.jetbrains.annotations.."
            )
            .check(importedClasses)
    }

    @Test
    fun `feature modules do not depend on other feature modules`() {
        slices()
            .matching("com.example.amulet.feature.(*)..")
            .should().notDependOnEachOther()
            .check(importedClasses)
    }

    @Test
    fun `data modules do not leak into presentation layer`() {
        classes()
            .that().resideInAPackage("..data..")
            .should().onlyDependOnClassesThat().resideInAnyPackage(
                "..data..",
                "..shared..",
                "..core..",
                "kotlin..",
                "kotlinx..",
                "java..",
                "javax..",
                "dagger..",
                "org.jetbrains.annotations..",
                "org.koin.."
            )
            .check(importedClasses)
    }

    @Test
    fun `shared module is platform agnostic`() {
        classes()
            .that().resideInAPackage("..shared..")
            .and().resideOutsideOfPackage("..shared.R")
            .and().resideOutsideOfPackage("..shared.BuildConfig")
            .should().onlyDependOnClassesThat().resideInAnyPackage(
                "..shared..",
                "kotlin..",
                "kotlinx..",
                "org.jetbrains.annotations..",
                "com.github.michaelbull.result..",
                "org.koin..",
                "java.."
            )
            .check(importedClasses)
    }

    @Test
    fun `modules remain acyclic`() {
        slices()
            .matching("com.example.amulet.(*)..")
            .should().beFreeOfCycles()
            .check(importedClasses)
    }
}
