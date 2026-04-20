package com.example.profdevelop.data.remote.dto

import com.example.profdevelop.domain.model.UserProfile
import com.example.profdevelop.domain.model.UserSession
import com.google.gson.annotations.SerializedName

data class LoginRequestDto(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("deviceInfo") val deviceInfo: String = "Android"
)

data class RefreshTokenRequestDto(
    @SerializedName("refreshToken") val refreshToken: String
)

data class AuthResponseDto(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String,
    @SerializedName("user") val user: UserProfileDto
)

data class UserProfileDto(
    @SerializedName("id") val id: Int,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("email") val email: String,
    @SerializedName("role") val role: String,
    @SerializedName("positionTitle") val positionTitle: String?,
    @SerializedName("departmentName") val departmentName: String?,
    @SerializedName("totalXp") val totalXp: Int,
    @SerializedName("level") val level: Int,
    @SerializedName("streakDays") val streakDays: Int,
    @SerializedName("avatarUrl") val avatarUrl: String?
)

fun AuthResponseDto.toDomain(rememberMe: Boolean): UserSession = UserSession(
    accessToken = accessToken,
    refreshToken = refreshToken,
    user = user.toDomain(),
    rememberMe = rememberMe
)

private fun UserProfileDto.toDomain(): UserProfile = UserProfile(
    id = id,
    fullName = fullName,
    email = email,
    role = role,
    positionTitle = positionTitle,
    departmentName = departmentName,
    totalXp = totalXp,
    level = level,
    streakDays = streakDays,
    avatarUrl = avatarUrl
)
