package com.example.profdevelop.domain.model

data class UserProfile(
    val id: Int,
    val fullName: String,
    val email: String,
    val role: String,
    val positionTitle: String?,
    val departmentName: String?,
    val totalXp: Int,
    val level: Int,
    val streakDays: Int,
    val avatarUrl: String?
)
