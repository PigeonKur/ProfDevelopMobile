package com.example.profdevelop.data.remote

import com.example.profdevelop.data.remote.api.AuthApi
import com.example.profdevelop.data.remote.dto.AuthResponseDto
import com.example.profdevelop.data.remote.dto.LoginRequestDto
import com.example.profdevelop.data.remote.dto.RefreshTokenRequestDto
import com.example.profdevelop.di.NetworkFactory

class AuthRemoteDataSource(
    private val networkFactory: NetworkFactory
) {
    suspend fun login(baseUrl: String, request: LoginRequestDto): AuthResponseDto {
        return createApi(baseUrl).login(request)
    }

    suspend fun refresh(baseUrl: String, refreshToken: String): AuthResponseDto {
        return createApi(baseUrl).refresh(RefreshTokenRequestDto(refreshToken))
    }

    suspend fun logout(baseUrl: String, refreshToken: String) {
        createApi(baseUrl).logout(RefreshTokenRequestDto(refreshToken))
    }

    private fun createApi(baseUrl: String): AuthApi {
        return networkFactory.createRetrofit(baseUrl).create(AuthApi::class.java)
    }
}
