package com.example.amulet.data.user

import com.example.amulet.shared.domain.privacy.model.UserConsents
import kotlin.time.Instant
import org.junit.Test
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaType

/**
 * Тест для проверки типов на уровне bytecode
 */
class UserConsentsBytecodeTest {

    @Test
    fun `UserConsents updatedAt field has correct type`() {
        val consents = UserConsents(
            analytics = true,
            marketing = false,
            notifications = true,
            updatedAt = Instant.parse("2025-10-16T15:35:50.306508Z")
        )

        // Получаем рефлексию поля updatedAt
        val updatedAtProperty = UserConsents::class.memberProperties
            .first { it.name == "updatedAt" }

        println("Property name: ${updatedAtProperty.name}")
        println("Property type: ${updatedAtProperty.returnType}")
        println("Property javaType: ${updatedAtProperty.returnType.javaType}")
        
        // Проверяем значение
        val value = updatedAtProperty.get(consents)
        println("Value: $value")
        println("Value class: ${value?.javaClass}")
        println("Value class name: ${value?.javaClass?.name}")

        // Проверяем, что это kotlinx.datetime.Instant
        assert(value is Instant) { "Expected kotlinx.datetime.Instant, got ${value?.javaClass}" }
    }

    @Test
    fun `UserConsents constructor signature check`() {
        // Получаем все конструкторы
        val constructors = UserConsents::class.constructors
        
        println("Number of constructors: ${constructors.size}")
        
        constructors.forEach { constructor ->
            println("\nConstructor: $constructor")
            constructor.parameters.forEach { param ->
                println("  Parameter: ${param.name}, type: ${param.type}, javaType: ${param.type.javaType}")
            }
        }

        // Проверяем, что можем создать объект с Instant
        val instant = Instant.parse("2025-10-16T15:35:50.306508Z")
        val consents = UserConsents(
            analytics = false,
            marketing = false,
            notifications = true,
            updatedAt = instant
        )

        assert(consents.updatedAt == instant)
    }

    @Test
    fun `Instant type check`() {
        val instant = Instant.parse("2025-10-16T15:35:50.306508Z")
        
        println("Instant class: ${instant.javaClass}")
        println("Instant class name: ${instant.javaClass.name}")
        println("Instant package: ${instant.javaClass.`package`}")
        
        // Проверяем, что это kotlinx.datetime.Instant
        val className = instant.javaClass.name
        println("Full class name: $className")
        
        // В kotlinx-datetime 0.6.x, Instant это typealias для kotlin.time.Instant
        // Но на уровне JVM это должен быть один и тот же класс
    }
}
