package com.example.amulet.shared.domain.practices.model

/** Тег практики (тема, контекст, намерение и т.п.). */
data class PracticeTag(
    val id: String,
    val name: String,
    val kind: String
)
