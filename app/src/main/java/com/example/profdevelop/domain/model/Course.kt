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
)
