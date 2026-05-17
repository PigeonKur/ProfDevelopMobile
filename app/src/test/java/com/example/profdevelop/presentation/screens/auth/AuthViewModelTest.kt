package com.example.profdevelop.presentation.screens.auth

import com.example.profdevelop.domain.model.UserProfile
import com.example.profdevelop.domain.model.UserSession
import com.example.profdevelop.domain.usecase.LoginUseCase
import com.example.profdevelop.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AuthViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var loginUseCase: LoginUseCase
    private lateinit var viewModel: AuthViewModel

    private val fakeSession = UserSession(
        accessToken = "access_token",
        refreshToken = "refresh_token",
        rememberMe = true,
        user = UserProfile(
            id = 1,
            fullName = "Иван Иванов",
            email = "ivanov@aml-nn.ru",
            role = "employee",
            positionTitle = null,
            departmentName = null,
            totalXp = 0,
            level = 1,
            streakDays = 0
        )
    )

    @Before
    fun setUp() {
        loginUseCase = mockk()
        viewModel = AuthViewModel(loginUseCase)
    }

    @Test
    fun `updateEmail updates state`() = runTest {
        viewModel.updateEmail("test@mail.ru")
        assertEquals("test@mail.ru", viewModel.state.value.email)
    }

    @Test
    fun `updatePassword updates state`() = runTest {
        viewModel.updatePassword("secret123")
        assertEquals("secret123", viewModel.state.value.password)
    }

    @Test
    fun `toggleRememberMe flips value`() = runTest {
        val initial = viewModel.state.value.rememberMe
        viewModel.toggleRememberMe()
        assertEquals(!initial, viewModel.state.value.rememberMe)
    }

    @Test
    fun `login with blank fields sets validation error`() = runTest {
        viewModel.login {}
        assertEquals("Введите email и пароль.", viewModel.state.value.error)
        coVerify(exactly = 0) { loginUseCase(any(), any(), any()) }
    }

    @Test
    fun `login success calls callback`() = runTest {
        coEvery { loginUseCase(any(), any(), any()) } returns Result.success(fakeSession)
        viewModel.updateEmail("ivanov@aml-nn.ru")
        viewModel.updatePassword("pass123")

        var called = false
        viewModel.login { called = true }

        assertTrue(called)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `login failure sets error`() = runTest {
        coEvery { loginUseCase(any(), any(), any()) } returns Result.failure(RuntimeException("401"))
        viewModel.updateEmail("wrong@mail.ru")
        viewModel.updatePassword("wrongpass")
        viewModel.login {}
        assertNotNull(viewModel.state.value.error)
    }
}
