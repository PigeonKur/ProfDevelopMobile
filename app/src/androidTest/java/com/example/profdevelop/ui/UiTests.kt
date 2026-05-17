package com.example.profdevelop.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.profdevelop.domain.model.*
import com.example.profdevelop.presentation.screens.auth.AuthScreen
import com.example.profdevelop.presentation.screens.auth.AuthUiState
import com.example.profdevelop.presentation.screens.home.HomeScreen
import com.example.profdevelop.presentation.screens.home.HomeUiState
import com.example.profdevelop.presentation.screens.lesson.FeedbackState
import com.example.profdevelop.presentation.screens.lesson.LessonFeedback
import com.example.profdevelop.presentation.screens.lesson.LessonScreen
import com.example.profdevelop.presentation.screens.lesson.LessonUiState
import com.example.profdevelop.presentation.screens.profile.ProfileScreen
import com.example.profdevelop.presentation.screens.profile.ProfileUiState
import com.example.profdevelop.presentation.screens.settings.SettingsScreen
import com.example.profdevelop.presentation.screens.settings.SettingsUiState
import com.example.profdevelop.presentation.theme.ProfDevelopTheme
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

// ─── Вспомогательные данные ───────────────────────────────────────────────────

private val fakeUser = UserProfile(
    id = 1, fullName = "Иван Иванов", email = "ivan@aml.ru",
    role = "employee", positionTitle = "Менеджер", departmentName = "IT",
    totalXp = 450, level = 5, streakDays = 7, avatarUrl = null
)

private val choiceQuestion = Question(
    id = 1, type = "choice", text = "Что такое протокол совещания?",
    xpValue = 10, hint = "Подсказка", explanationCorrect = "Верно!", explanationWrong = "Нет.",
    answers = listOf(
        AnswerOption(id = 10, text = "Официальный документ", orderIndex = 0),
        AnswerOption(id = 11, text = "Черновик", orderIndex = 1),
    ),
    matchingPairs = emptyList()
)

private val matchingQuestion = Question(
    id = 2, type = "matching", text = "Установите соответствие",
    xpValue = 15, hint = null, explanationCorrect = null, explanationWrong = null,
    answers = emptyList(),
    matchingPairs = listOf(
        MatchingPair(id = 20, leftText = "Левый 1", rightText = "Правый 1", orderIndex = 0),
        MatchingPair(id = 21, leftText = "Левый 2", rightText = "Правый 2", orderIndex = 1),
    )
)

// ══════════════════════════════════════════════════════════════════════════════
// AuthScreen UI Tests
// ══════════════════════════════════════════════════════════════════════════════

class AuthScreenUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContent(state: AuthUiState = AuthUiState()) {
        composeTestRule.setContent {
            ProfDevelopTheme {
                AuthScreen(
                    state        = state,
                    onEmailChange    = {},
                    onPasswordChange = {},
                    onToggleRemember = {},
                    onLogin          = {},
                )
            }
        }
    }

    @Test
    fun authScreen_emailFieldIsDisplayed() {
        setContent()
        composeTestRule.onNodeWithTag("email_field").assertIsDisplayed()
    }

    @Test
    fun authScreen_passwordFieldIsDisplayed() {
        setContent()
        composeTestRule.onNodeWithTag("password_field").assertIsDisplayed()
    }

    @Test
    fun authScreen_loginButtonIsDisplayed() {
        setContent()
        composeTestRule.onNodeWithTag("login_button").assertIsDisplayed()
    }

    @Test
    fun authScreen_loginButtonDisabledWhenFieldsEmpty() {
        setContent(AuthUiState(email = "", password = ""))
        composeTestRule.onNodeWithTag("login_button").assertIsNotEnabled()
    }

    @Test
    fun authScreen_loginButtonEnabledWhenFieldsFilled() {
        setContent(AuthUiState(email = "ivan@aml.ru", password = "pass123"))
        composeTestRule.onNodeWithTag("login_button").assertIsEnabled()
    }

    @Test
    fun authScreen_showsErrorMessage() {
        setContent(AuthUiState(error = "Неверный email или пароль."))
        composeTestRule.onNodeWithText("Неверный email или пароль.").assertIsDisplayed()
    }

    @Test
    fun authScreen_showsLoadingIndicatorWhenLoading() {
        setContent(AuthUiState(isLoading = true))
        composeTestRule.onNodeWithTag("loading_indicator").assertIsDisplayed()
        composeTestRule.onNodeWithTag("login_button").assertDoesNotExist()
    }

    @Test
    fun authScreen_hidesErrorWhenAbsent() {
        setContent(AuthUiState(error = null))
        composeTestRule.onNodeWithTag("error_text").assertDoesNotExist()
    }

    @Test
    fun authScreen_rememberMeCheckboxExists() {
        setContent()
        composeTestRule.onNodeWithTag("remember_me_checkbox").assertExists()
    }

}

// ══════════════════════════════════════════════════════════════════════════════
// HomeScreen UI Tests
// ══════════════════════════════════════════════════════════════════════════════

class HomeScreenUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val fakeCourse = Course(
        id = 1, title = "Основы делопроизводства",
        description = "Описание", totalLessons = 5,
        completedLessons = 2, progressPercent = 40,
        isAssigned = true, thumbnailUrl = null
    )

    private val fakeLessons = listOf(
        Lesson(
            id = 1, courseId = 1, title = "Урок 1",
            orderIndex = 1, xpReward = 30,
            isCompleted = true, isUnlocked = true
        ),
        Lesson(
            id = 2, courseId = 1, title = "Урок 2",
            orderIndex = 2, xpReward = 30,
            isCompleted = false, isUnlocked = true
        ),
        Lesson(
            id = 3, courseId = 1, title = "Урок 3",
            orderIndex = 3, xpReward = 30,
            isCompleted = false, isUnlocked = false
        ),
    )

    private fun setContent(state: HomeUiState) {
        composeTestRule.setContent {
            ProfDevelopTheme {
                HomeScreen(
                    state = state,
                    onOpenLesson  = { _, _ -> },
                    onOpenCourse  = { _ -> },
                )
            }
        }
    }

    @Test
    fun homeScreen_showsLoadingWhenLoading() {
        setContent(HomeUiState(isLoading = true))
        composeTestRule.onNodeWithTag("home_loading").assertIsDisplayed()
    }

    @Test
    fun homeScreen_showsUserNameInTopBar() {
        setContent(HomeUiState(user = fakeUser, isLoading = false))
        composeTestRule.onNodeWithText("И").assertIsDisplayed() // аватар с инициалом
    }

    @Test
    fun homeScreen_showsStreakInTopBar() {
        setContent(HomeUiState(user = fakeUser, isLoading = false))
        composeTestRule.onNodeWithText("7").assertExists()
    }

    @Test
    fun homeScreen_showsXpInTopBar() {
        setContent(HomeUiState(user = fakeUser, isLoading = false))
        composeTestRule.onNodeWithText("450").assertExists()
    }

    @Test
    fun homeScreen_showsLevelInTopBar() {
        setContent(HomeUiState(user = fakeUser, isLoading = false))
        composeTestRule.onNodeWithText("5").assertExists()
    }

    @Test
    fun homeScreen_showsCourseTitle() {
        setContent(HomeUiState(
            user = fakeUser, isLoading = false,
            chapters = listOf(HomeChapter(fakeCourse, fakeLessons, 2))
        ))
        composeTestRule.onNodeWithText("Основы делопроизводства").assertIsDisplayed()
    }

    @Test
    fun homeScreen_completedLessonNodeIsGreen() {
        setContent(HomeUiState(
            user = fakeUser, isLoading = false,
            chapters = listOf(HomeChapter(fakeCourse, fakeLessons, 2))
        ))
        composeTestRule.onNodeWithTag("lesson_node_completed_1").assertIsDisplayed()
    }


    @Test
    fun homeScreen_continueCardClickable() {
        setContent(HomeUiState(
            user = fakeUser, isLoading = false,
            nextLesson = NextLessonInfo(fakeLessons[1], "Основы делопроизводства"),
        ))
        composeTestRule.onNodeWithTag("continue_card").assertHasClickAction()
    }

    @Test
    fun homeScreen_showsErrorMessage() {
        setContent(HomeUiState(error = "Нет подключения", isLoading = false))
        composeTestRule.onNodeWithText("Нет подключения").assertIsDisplayed()
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// LessonScreen UI Tests
// ══════════════════════════════════════════════════════════════════════════════

class LessonScreenUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContent(state: LessonUiState) {
        composeTestRule.setContent {
            ProfDevelopTheme {
                LessonScreen(
                    state               = state,
                    onSelectAnswer      = { _, _ -> },
                    onSelectMatching    = { _, _, _ -> },
                    onCheck             = {},
                    onContinue          = {},
                    onBack              = {},
                    onExit              = {},
                    onFinish            = {},
                )
            }
        }
    }

    private fun baseState() = LessonUiState(
        isLoading       = false,
        questions       = listOf(choiceQuestion),
        questionQueue   = listOf(1),
        currentQuestionIndex = 0,
        totalUniqueCount = 1,
    )

    @Test
    fun lessonScreen_showsLoadingWhenLoading() {
        setContent(LessonUiState(isLoading = true))
        composeTestRule.onNodeWithTag("lesson_loading").assertIsDisplayed()
    }

    @Test
    fun lessonScreen_showsQuestionText() {
        setContent(baseState())
        composeTestRule.onNodeWithText("Что такое протокол совещания?").assertIsDisplayed()
    }

    @Test
    fun lessonScreen_showsHintText() {
        setContent(baseState())
        composeTestRule.onNodeWithText("Подсказка", substring = true).assertIsDisplayed()
    }

    @Test
    fun lessonScreen_showsAnswerOptions() {
        setContent(baseState())
        composeTestRule.onNodeWithText("Официальный документ").assertIsDisplayed()
        composeTestRule.onNodeWithText("Черновик").assertIsDisplayed()
    }

    @Test
    fun lessonScreen_checkButtonDisabledInitially() {
        setContent(baseState())
        composeTestRule.onNodeWithTag("check_button").assertIsNotEnabled()
    }

    @Test
    fun lessonScreen_checkButtonEnabledAfterSelection() {
        setContent(baseState().copy(
            selectedAnswers = mapOf(1 to setOf(10))
        ))
        composeTestRule.onNodeWithTag("check_button").assertIsEnabled()
    }

    @Test
    fun lessonScreen_showsGreenFeedbackOnCorrectAnswer() {
        setContent(baseState().copy(
            feedback = LessonFeedback(isCorrect = true, message = "Верно!")
        ))
        composeTestRule.onNodeWithTag("feedback_panel").assertIsDisplayed()
        composeTestRule.onNodeWithText("Верно!").assertIsDisplayed()
    }

    @Test
    fun lessonScreen_showsRedFeedbackOnWrongAnswer() {
        setContent(baseState().copy(
            feedback = LessonFeedback(isCorrect = false, message = "Ответ неверный.")
        ))
        composeTestRule.onNodeWithTag("feedback_panel").assertIsDisplayed()
        composeTestRule.onNodeWithText("Ответ неверный.", substring = true).assertIsDisplayed()
    }

    @Test
    fun lessonScreen_continueButtonShownAfterFeedback() {
        setContent(baseState().copy(
            feedback = LessonFeedback(isCorrect = true, message = "Верно!")
        ))
        composeTestRule.onNodeWithTag("continue_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("check_button").assertDoesNotExist()
    }

    @Test
    fun lessonScreen_progressBarDisplayed() {
        setContent(baseState())
        composeTestRule.onNodeWithTag("lesson_progress_bar").assertIsDisplayed()
    }

    @Test
    fun lessonScreen_progressCounterText() {
        setContent(baseState().copy(
            completedQuestionIds = emptySet(),
            totalUniqueCount     = 3
        ))
        composeTestRule.onNodeWithText("0 из 3").assertIsDisplayed()
    }

    @Test
    fun lessonScreen_showsExplanationInFeedback() {
        setContent(baseState().copy(
            feedback = LessonFeedback(isCorrect = true, message = "Верно!",)
        ))
        composeTestRule.onNodeWithText("Верно!").assertIsDisplayed()
    }

    @Test
    fun lessonScreen_matchingQuestionShowsPairs() {
        setContent(baseState().copy(
            questions = listOf(matchingQuestion),
            questionQueue = listOf(2),
        ))
        composeTestRule.onNodeWithText("Левый 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Правый 1").assertIsDisplayed()
    }

    @Test
    fun lessonScreen_exitButtonExists() {
        setContent(baseState())
        composeTestRule.onNodeWithTag("exit_button").assertExists()
    }

    @Test
    fun lessonScreen_resultScreenShownAfterCompletion() {
        setContent(baseState().copy(
            showResult = true,
            result = LessonResult(
                isCompleted = true, score = 1, maxScore = 1,
                xpEarned = 10, totalXp = 110, newLevel = 2,
                streakDays = 5, previousStreak = 4,
                streakIncreased = true, streakActive = true,
                newAchievements = emptyList(), questionReviews = emptyList()
            )
        ))
        composeTestRule.onNodeWithTag("result_screen").assertIsDisplayed()
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// ProfileScreen UI Tests
// ══════════════════════════════════════════════════════════════════════════════

class ProfileScreenUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContent(state: ProfileUiState) {
        composeTestRule.setContent {
            ProfDevelopTheme {
                ProfileScreen(
                    state      = state,
                    onLogout   = {},
                    onSelectTier = {},
                    onRefresh  = {},
                )
            }
        }
    }

    @Test
    fun profileScreen_showsUserName() {
        setContent(ProfileUiState(user = fakeUser))
        composeTestRule.onNodeWithText("Иван Иванов").assertIsDisplayed()
    }

    @Test
    fun profileScreen_showsUserPosition() {
        setContent(ProfileUiState(user = fakeUser))
        composeTestRule.onNodeWithText("Менеджер").assertIsDisplayed()
    }

    @Test
    fun profileScreen_showsXpValue() {
        setContent(ProfileUiState(user = fakeUser))
        composeTestRule.onNodeWithText("450").assertIsDisplayed()
    }

    @Test
    fun profileScreen_showsLevel() {
        setContent(ProfileUiState(user = fakeUser))
        composeTestRule.onNodeWithText("5").assertIsDisplayed()
    }

    @Test
    fun profileScreen_showsStreakDays() {
        setContent(ProfileUiState(user = fakeUser))
        composeTestRule.onNodeWithText("7").assertIsDisplayed()
    }

    @Test
    fun profileScreen_showsLeaderboard() {
        val leaderboard = listOf(
            LeaderboardEntry(1, 1, "Иван Иванов", null, 450, 5, 7, "gold"),
            LeaderboardEntry(2, 2, "Пётр Петров", null, 300, 3, 2, "silver"),
        )
        setContent(ProfileUiState(user = fakeUser, leaderboard = leaderboard))
        composeTestRule.onNodeWithText("Пётр Петров").assertIsDisplayed()
    }

    @Test
    fun profileScreen_showsLoadingForLeaderboard() {
        setContent(ProfileUiState(user = fakeUser, leaderboardLoading = true))
        composeTestRule.onNodeWithTag("leaderboard_loading").assertIsDisplayed()
    }

    @Test
    fun profileScreen_logoutButtonExists() {
        setContent(ProfileUiState(user = fakeUser))
        composeTestRule.onNodeWithTag("logout_button").assertExists()
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// SettingsScreen UI Tests
// ══════════════════════════════════════════════════════════════════════════════

class SettingsScreenUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContent(state: SettingsUiState = SettingsUiState()) {
        composeTestRule.setContent {
            ProfDevelopTheme {
                SettingsScreen(
                    state             = state,
                    onApiUrlChange    = {},
                    onSaveApiUrl      = {},
                    onToggleHaptics   = {},
                    onToggleReminder  = {},
                    onSetReminderHour = {},
                    onToggleLargeText = {},
                    onResetAll        = {},
                    onConsumeMessage  = {},
                )
            }
        }
    }

    @Test
    fun settingsScreen_apiUrlFieldDisplayed() {
        setContent()
        composeTestRule.onNodeWithTag("api_url_field").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_saveButtonDisplayed() {
        setContent()
        composeTestRule.onNodeWithTag("save_api_url_button").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_showsValidationError() {
        setContent(SettingsUiState(apiUrlError = "Некорректный URL"))
        composeTestRule.onNodeWithText("Некорректный URL").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_showsSuccessMessage() {
        setContent(SettingsUiState(message = "Адрес сервера сохранён"))
        composeTestRule.onNodeWithText("Адрес сервера сохранён").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_hapticsToggleExists() {
        setContent()
        composeTestRule.onNodeWithTag("haptics_toggle").assertExists()
    }

    @Test
    fun settingsScreen_reminderToggleExists() {
        setContent()
        composeTestRule.onNodeWithTag("reminder_toggle").assertExists()
    }

    @Test
    fun settingsScreen_largeTextToggleExists() {
        setContent()
        composeTestRule.onNodeWithTag("large_text_toggle").assertExists()
    }

    @Test
    fun settingsScreen_resetButtonExists() {
        setContent()
        composeTestRule.onNodeWithTag("reset_button").assertExists()
    }

    @Test
    fun settingsScreen_showsCurrentApiUrl() {
        setContent(SettingsUiState(apiUrl = "https://192.168.50.56:7222"))
        composeTestRule.onNodeWithTag("api_url_field")
            .assertTextContains("https://192.168.50.56:7222")
    }

    @Test
    fun settingsScreen_reminderHourPickerHiddenWhenDisabled() {
        setContent(SettingsUiState(reminderEnabled = false))
        composeTestRule.onNodeWithTag("reminder_hour_picker").assertDoesNotExist()
    }

    @Test
    fun settingsScreen_reminderHourPickerShownWhenEnabled() {
        setContent(SettingsUiState(reminderEnabled = true))
        composeTestRule.onNodeWithTag("reminder_hour_picker").assertIsDisplayed()
    }
}
