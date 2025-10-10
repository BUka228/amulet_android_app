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
    fun `модули feature зависят только от shared-абстракций и core design`() {
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
    fun `модули feature не зависят друг от друга`() {
        slices()
            .matching("com.example.amulet.feature.(*)..")
            .should().notDependOnEachOther()
            .check(importedClasses)
    }

    @Test
    fun `модули data не протекают в презентационный слой`() {
        classes()
            .that().resideInAPackage("..data..")
            .should().onlyDependOnClassesThat().resideInAnyPackage(
                "..data..",
                "..shared..",
                "..core..",
                "com.github.michaelbull.result..",
                "com.google.firebase..",
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
    fun `модуль shared платформенно-агностичен`() {
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
                "io.github.aakira.napier..",
                "java.."
            )
            .check(importedClasses)
    }

    @Test
    fun `зависимости модулей остаются ацикличными`() {
        slices()
            .matching("com.example.amulet.(*)..")
            .should().beFreeOfCycles()
            .check(importedClasses)
    }
}
