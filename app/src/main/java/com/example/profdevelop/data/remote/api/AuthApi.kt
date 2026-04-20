package com.example.profdevelop.data.remote.api

import com.example.profdevelop.data.remote.dto.AuthResponseDto
import com.example.profdevelop.data.remote.dto.LoginRequestDto
import com.example.profdevelop.data.remote.dto.RefreshTokenRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequestDto): AuthResponseDto

    @POST("api/auth/refresh")
    suspend fun refresh(@Body request: RefreshTokenRequestDto): AuthResponseDto

    @POST("api/auth/logout")
    suspend fun logout(@Body request: RefreshTokenRequestDto)
}
