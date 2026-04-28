package com.example.profdevelop.data.remote.api

import com.example.profdevelop.data.remote.dto.AchievementDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface UsersApi {
    @GET("api/users/{id}/achievements")
    suspend fun getAchievements(
        @Path("id") userId: Int,
        @Query("includeUnearned") includeUnearned: Boolean = true
    ): List<AchievementDto>
}
