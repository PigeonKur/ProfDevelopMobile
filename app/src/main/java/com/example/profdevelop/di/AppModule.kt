package com.example.profdevelop.di

import android.content.Context
import com.example.profdevelop.data.local.AuthPreferencesDataSource
import com.example.profdevelop.data.remote.AuthRemoteDataSource
import com.example.profdevelop.data.remote.LearningRemoteDataSource
import com.example.profdevelop.data.repository.AuthRepositoryImpl
import com.example.profdevelop.data.repository.LearningRepositoryImpl
import com.example.profdevelop.domain.repository.AuthRepository
import com.example.profdevelop.domain.repository.LearningRepository
import com.example.profdevelop.domain.usecase.GetAchievementsUseCase
import com.example.profdevelop.domain.usecase.GetApiUrlUseCase
import com.example.profdevelop.domain.usecase.GetAssignedCoursesUseCase
import com.example.profdevelop.domain.usecase.CheckQuestionUseCase
import com.example.profdevelop.domain.usecase.GetLessonsUseCase
import com.example.profdevelop.domain.usecase.GetQuestionsUseCase
import com.example.profdevelop.domain.usecase.GetStoredSessionUseCase
import com.example.profdevelop.domain.usecase.LoginUseCase
import com.example.profdevelop.domain.usecase.RestoreSessionUseCase
import com.example.profdevelop.domain.usecase.SubmitLessonAttemptUseCase
import com.example.profdevelop.domain.usecase.UpdateApiUrlUseCase

class AppModule(context: Context) {
    private val networkFactory = NetworkFactory()
    private val preferencesDataSource = AuthPreferencesDataSource(context)
    private val authRemoteDataSource = AuthRemoteDataSource(networkFactory)
    private val learningRemoteDataSource = LearningRemoteDataSource(networkFactory)

    private val authRepository: AuthRepository = AuthRepositoryImpl(
        remoteDataSource = authRemoteDataSource,
        localDataSource = preferencesDataSource
    )
    private val learningRepository: LearningRepository = LearningRepositoryImpl(
        remoteDataSource = learningRemoteDataSource,
        localDataSource = preferencesDataSource
    )

    val loginUseCase = LoginUseCase(authRepository)
    val restoreSessionUseCase = RestoreSessionUseCase(authRepository)
    val getStoredSessionUseCase = GetStoredSessionUseCase(authRepository)
    val getApiUrlUseCase = GetApiUrlUseCase(authRepository)
    val updateApiUrlUseCase = UpdateApiUrlUseCase(authRepository)
    val getAssignedCoursesUseCase = GetAssignedCoursesUseCase(learningRepository)
    val getLessonsUseCase = GetLessonsUseCase(learningRepository)
    val getQuestionsUseCase = GetQuestionsUseCase(learningRepository)
    val getAchievementsUseCase = GetAchievementsUseCase(learningRepository)
    val checkQuestionUseCase = CheckQuestionUseCase(learningRepository)
    val submitLessonAttemptUseCase = SubmitLessonAttemptUseCase(learningRepository)
}
