package com.example.profdevelop.domain.model

data class Course(
    val id: Int,
    val title: String,
    val description: String?,
    val category: String?,
    val difficulty: String?,
    val estimatedMinutes: Int,
    val progressPercent: Int,
    val totalLessons: Int,
    val completedLessons: Int,
    val isMandatory: Boolean,
    val deadline: String?
) {
    constructor(
        id: Int,
        title: String,
        description: String?,
        totalLessons: Int,
        completedLessons: Int,
        progressPercent: Int,
        isAssigned: Boolean,
        thumbnailUrl: String?
    ) : this(
        id = id,
        title = title,
        description = description,
        category = null,
        difficulty = null,
        estimatedMinutes = 0,
        progressPercent = progressPercent,
        totalLessons = totalLessons,
        completedLessons = completedLessons,
        isMandatory = isAssigned,
        deadline = thumbnailUrl
    )
}
