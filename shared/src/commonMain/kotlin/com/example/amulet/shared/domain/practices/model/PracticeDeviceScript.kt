package com.example.amulet.shared.domain.practices.model

data class PracticeDeviceScript(
    val practiceId: PracticeId,
    val steps: List<String>,
)
