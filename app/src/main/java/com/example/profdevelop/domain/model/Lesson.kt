package com.example.profdevelop.domain.model

data class Lesson(
    val id: Int,
    val title: String,
    val orderIndex: Int,
    val xpReward: Int,
    val description: String?,
    val lessonType: String,
    val estimatedMinutes: Int,
    val isCompleted: Boolean,
    val isUnlocked: Boolean,
    val score: Int?,
    val maxScore: Int?
)
