package com.example.profdevelop.presentation.screens.splash

import com.example.profdevelop.domain.model.UserProfile
import com.example.profdevelop.domain.model.UserSession
import com.example.profdevelop.domain.usecase.RestoreSessionUseCase
import com.example.profdevelop.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SplashViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val restoreSessionUseCase: RestoreSessionUseCase = mockk()

    private val fakeSession = UserSession(
        accessToken = "tok",
        refreshToken = "ref",
        rememberMe = true,
        user = UserProfile(
            id = 1,
            fullName = "Тест",
            email = "t@t.ru",
            role = "employee",
            positionTitle = null,
            departmentName = null,
            totalXp = 0,
            level = 1,
            streakDays = 0
        )
    )

    @Test
    fun `initial state is loading`() = runTest {
        coEvery { restoreSessionUseCase() } returns fakeSession
        val vm = SplashViewModel(restoreSessionUseCase)
        assertTrue(vm.state.value is SplashState.Loading)
    }

    @Test
    fun `authorized after delay when session exists`() = runTest {
        coEvery { restoreSessionUseCase() } returns fakeSession
        val vm = SplashViewModel(restoreSessionUseCase)
        advanceTimeBy(1200)
        assertTrue(vm.state.value is SplashState.Authorized)
    }

    @Test
    fun `unauthorized after delay when session absent`() = runTest {
        coEvery { restoreSessionUseCase() } returns null
        val vm = SplashViewModel(restoreSessionUseCase)
        advanceTimeBy(1200)
        assertTrue(vm.state.value is SplashState.Unauthorized)
    }

    @Test
    fun `state remains defined on restore failure`() = runTest {
        coEvery { restoreSessionUseCase() } throws RuntimeException("network")
        val vm = SplashViewModel(restoreSessionUseCase)
        advanceTimeBy(1200)
        assertNotNull(vm.state.value)
    }
}
