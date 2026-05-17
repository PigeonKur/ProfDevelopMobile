package com.example.profdevelop.presentation.screens.settings

import com.example.profdevelop.data.local.AppSettings
import com.example.profdevelop.data.local.SettingsPreferencesDataSource
import com.example.profdevelop.domain.usecase.GetApiUrlUseCase
import com.example.profdevelop.domain.usecase.UpdateApiUrlUseCase
import com.example.profdevelop.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var dataSource: SettingsPreferencesDataSource
    private lateinit var getApiUrlUseCase: GetApiUrlUseCase
    private lateinit var updateApiUrlUseCase: UpdateApiUrlUseCase
    private lateinit var viewModel: SettingsViewModel

    private val settingsFlow = MutableStateFlow(AppSettings())

    @Before
    fun setUp() {
        dataSource = mockk(relaxed = true)
        getApiUrlUseCase = mockk()
        updateApiUrlUseCase = mockk()

        every { dataSource.flow } returns settingsFlow
        coEvery { getApiUrlUseCase() } returns "https://192.168.1.1:7222"
        coJustRun { updateApiUrlUseCase(any()) }

        viewModel = SettingsViewModel(dataSource, getApiUrlUseCase, updateApiUrlUseCase)
    }

    @Test
    fun `init loads api url`() = runTest {
        assertEquals("https://192.168.1.1:7222", viewModel.state.value.apiUrl)
    }

    @Test
    fun `updateApiUrl updates state`() = runTest {
        viewModel.updateApiUrl("https://10.0.0.1:7222")
        assertEquals("https://10.0.0.1:7222", viewModel.state.value.apiUrl)
    }

    @Test
    fun `saveApiUrl with invalid url stores validation error`() = runTest {
        viewModel.updateApiUrl("not-a-url")
        viewModel.saveApiUrl()
        assertNotNull(viewModel.state.value.apiUrlError)
        coVerify(exactly = 0) { updateApiUrlUseCase(any()) }
    }

    @Test
    fun `saveApiUrl with valid url persists and sets message`() = runTest {
        val newUrl = "https://192.168.50.56:7222"
        coEvery { getApiUrlUseCase() } returns newUrl
        viewModel.updateApiUrl(newUrl)
        viewModel.saveApiUrl()
        coVerify { updateApiUrlUseCase(newUrl) }
        assertEquals("Адрес сервера сохранён", viewModel.state.value.message)
        assertNull(viewModel.state.value.apiUrlError)
    }

    @Test
    fun `consumeMessage clears message`() = runTest {
        val newUrl = "https://10.0.0.1:8080"
        coEvery { getApiUrlUseCase() } returns newUrl
        viewModel.updateApiUrl(newUrl)
        viewModel.saveApiUrl()
        assertNotNull(viewModel.state.value.message)
        viewModel.consumeMessage()
        assertNull(viewModel.state.value.message)
    }

    @Test
    fun `resetAll invokes data source reset and sets message`() = runTest {
        viewModel.resetAll()
        coVerify { dataSource.resetAll() }
        assertEquals("Настройки сброшены", viewModel.state.value.message)
    }

    @Test
    fun `toggle methods delegate to data source`() = runTest {
        viewModel.toggleHaptics(false)
        viewModel.toggleReminder(true)
        viewModel.setReminderHour(8)
        viewModel.setDailyXpGoal(50)
        viewModel.toggleLargeText(true)

        coVerify { dataSource.setHaptics(false) }
        coVerify { dataSource.setReminderEnabled(true) }
        coVerify { dataSource.setReminderHour(8) }
        coVerify { dataSource.setDailyXpGoal(50) }
        coVerify { dataSource.setLargeText(true) }
    }
}
