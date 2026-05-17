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
) {
    constructor(
        id: Int,
        courseId: Int,
        title: String,
        orderIndex: Int,
        xpReward: Int,
        isCompleted: Boolean,
        isUnlocked: Boolean
    ) : this(
        id = id,
        title = title,
        orderIndex = orderIndex,
        xpReward = xpReward,
        description = null,
        lessonType = "quiz",
        estimatedMinutes = 0,
        isCompleted = isCompleted,
        isUnlocked = isUnlocked,
        score = null,
        maxScore = null
    )
}
