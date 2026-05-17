package com.example.profdevelop.presentation.screens.profile

import com.example.profdevelop.domain.model.LeaderboardEntry
import com.example.profdevelop.domain.model.UserProfile
import com.example.profdevelop.domain.model.UserSession
import com.example.profdevelop.domain.usecase.GetLeaderboardUseCase
import com.example.profdevelop.domain.usecase.GetStoredSessionUseCase
import com.example.profdevelop.domain.usecase.LogoutUseCase
import com.example.profdevelop.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coJustRun
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

class ProfileViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var getStoredSessionUseCase: GetStoredSessionUseCase
    private lateinit var logoutUseCase: LogoutUseCase
    private lateinit var getLeaderboardUseCase: GetLeaderboardUseCase
    private lateinit var viewModel: ProfileViewModel

    private val fakeUser = UserProfile(
        id = 1,
        fullName = "Иван Иванов",
        email = "ivan@aml.ru",
        role = "employee",
        positionTitle = "Менеджер",
        departmentName = "IT",
        totalXp = 450,
        level = 5,
        streakDays = 7
    )

    private val fakeSession = UserSession(
        accessToken = "at",
        refreshToken = "rt",
        rememberMe = true,
        user = fakeUser
    )

    private val fakeLeaderboard = listOf(
        LeaderboardEntry(1, 1, "Иван Иванов", "Менеджер", 450, 5, 7, "gold"),
        LeaderboardEntry(2, 2, "Пётр Петров", null, 320, 4, 3, "silver")
    )

    @Before
    fun setUp() {
        getStoredSessionUseCase = mockk()
        logoutUseCase = mockk()
        getLeaderboardUseCase = mockk()
        coEvery { getStoredSessionUseCase() } returns fakeSession
        coEvery { getLeaderboardUseCase(any()) } returns fakeLeaderboard
    }

    private fun buildViewModel() {
        viewModel = ProfileViewModel(
            getStoredSessionUseCase = getStoredSessionUseCase,
            logoutUseCase = logoutUseCase,
            getLeaderboardUseCase = getLeaderboardUseCase
        )
    }

    @Test
    fun `init loads user from stored session`() = runTest {
        buildViewModel()
        assertEquals(fakeUser, viewModel.state.value.user)
    }

    @Test
    fun `init loads leaderboard`() = runTest {
        buildViewModel()
        assertEquals(2, viewModel.state.value.leaderboard.size)
        assertTrue(!viewModel.state.value.leaderboardLoading)
    }

    @Test
    fun `refreshSession updates user`() = runTest {
        buildViewModel()
        coEvery { getStoredSessionUseCase() } returns fakeSession.copy(user = fakeUser.copy(totalXp = 500))
        viewModel.refreshSession()
        assertEquals(500, viewModel.state.value.user?.totalXp)
    }

    @Test
    fun `refreshSession clears user when no session`() = runTest {
        buildViewModel()
        coEvery { getStoredSessionUseCase() } returns null
        viewModel.refreshSession()
        assertNull(viewModel.state.value.user)
    }

    @Test
    fun `loadLeaderboard stores error on failure`() = runTest {
        coEvery { getLeaderboardUseCase(any()) } throws RuntimeException("server down")
        buildViewModel()
        assertNotNull(viewModel.state.value.leaderboardError)
        assertTrue(viewModel.state.value.leaderboard.isEmpty())
    }

    @Test
    fun `selectTier forwards selected tier`() = runTest {
        buildViewModel()
        viewModel.selectTier("gold")
        assertEquals("gold", viewModel.state.value.tierFilter)
        coVerify { getLeaderboardUseCase("gold") }
    }

    @Test
    fun `logout calls use case and completion callback`() = runTest {
        coJustRun { logoutUseCase() }
        buildViewModel()
        var doneCalled = false
        viewModel.logout { doneCalled = true }
        assertTrue(doneCalled)
        coVerify(exactly = 1) { logoutUseCase() }
    }
}
