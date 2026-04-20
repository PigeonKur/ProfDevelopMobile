package com.example.profdevelop.domain.model

data class UserSession(
    val accessToken: String,
    val refreshToken: String,
    val user: UserProfile,
    val rememberMe: Boolean
)
