package com.example.amulet.shared.domain.practices.model

/** Преднастроенная коллекция практик/курсов для «Дома практик». */
data class PracticeCollection(
    val id: String,
    val code: String,
    val title: String,
    val description: String? = null,
    val order: Int? = null,
    val items: List<PracticeCollectionItem> = emptyList()
)

/** Элемент коллекции: ссылка на практику или курс. */
data class PracticeCollectionItem(
    val id: String,
    val collectionId: String,
    val type: String,
    val practiceId: String? = null,
    val courseId: String? = null,
    val order: Int
)
