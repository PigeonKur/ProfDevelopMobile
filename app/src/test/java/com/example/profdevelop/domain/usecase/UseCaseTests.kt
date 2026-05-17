package com.example.profdevelop.domain.usecase

import com.example.profdevelop.domain.model.LeaderboardEntry
import com.example.profdevelop.domain.model.LessonAttempt
import com.example.profdevelop.domain.model.LessonResult
import com.example.profdevelop.domain.model.Question
import com.example.profdevelop.domain.model.QuestionAttempt
import com.example.profdevelop.domain.model.QuestionCheckResult
import com.example.profdevelop.domain.model.UserProfile
import com.example.profdevelop.domain.model.UserSession
import com.example.profdevelop.domain.repository.AuthRepository
import com.example.profdevelop.domain.repository.LearningRepository
import com.example.profdevelop.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

// ─── LoginUseCaseTest ─────────────────────────────────────────────────────────

class LoginUseCaseTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var authRepository: AuthRepository
    private lateinit var loginUseCase: LoginUseCase

    private val fakeSession = UserSession(
        accessToken = "at", refreshToken = "rt", rememberMe = true,
        user = UserProfile(
            id = 1, fullName = "Иван", email = "i@a.ru",
            role = "employee", positionTitle = null, departmentName = null,
            totalXp = 0, level = 1, streakDays = 0, avatarUrl = null
        )
    )

    @Before
    fun setUp() {
        authRepository = mockk()
        loginUseCase   = LoginUseCase(authRepository)
    }

    @Test
    fun `invoke передаёт корректные данные в репозиторий`() = runTest {
        coEvery { authRepository.login(any(), any(), any()) } returns Result.success(fakeSession)
        loginUseCase("i@a.ru", "pass", true)
        coVerify { authRepository.login("i@a.ru", "pass", true) }
    }

    @Test
    fun `invoke возвращает Result success при успешном логине`() = runTest {
        coEvery { authRepository.login(any(), any(), any()) } returns Result.success(fakeSession)
        val result = loginUseCase("i@a.ru", "pass", false)
        assertTrue(result.isSuccess)
        assertEquals(fakeSession, result.getOrNull())
    }

    @Test
    fun `invoke возвращает Result failure при исключении`() = runTest {
        coEvery { authRepository.login(any(), any(), any()) } throws RuntimeException("401")
        val result = loginUseCase("wrong@a.ru", "bad", false)
        assertTrue(result.isFailure)
        assertEquals("401", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invoke trimует email перед передачей`() = runTest {
        coEvery { authRepository.login(any(), any(), any()) } returns Result.success(fakeSession)
        loginUseCase("  i@a.ru  ", "pass", false)
        coVerify { authRepository.login("i@a.ru", "pass", false) }
    }
}


// ─── RestoreSessionUseCaseTest ────────────────────────────────────────────────

class RestoreSessionUseCaseTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var authRepository: AuthRepository
    private lateinit var restoreSessionUseCase: RestoreSessionUseCase

    private val fakeSession = UserSession(
        accessToken = "at", refreshToken = "rt", rememberMe = true,
        user = UserProfile(
            id = 1, fullName = "Иван", email = "i@a.ru",
            role = "employee", positionTitle = null, departmentName = null,
            totalXp = 0, level = 1, streakDays = 0, avatarUrl = null
        )
    )

    @Before
    fun setUp() {
        authRepository       = mockk()
        restoreSessionUseCase = RestoreSessionUseCase(authRepository)
    }

    @Test
    fun `invoke возвращает сессию если refresh успешен`() = runTest {
        coEvery { authRepository.restoreSession() } returns fakeSession
        val result = restoreSessionUseCase()
        assertEquals(fakeSession, result)
    }

    @Test
    fun `invoke возвращает null если сессии нет`() = runTest {
        coEvery { authRepository.restoreSession() } returns null
        val result = restoreSessionUseCase()
        assertNull(result)
    }

    @Test
    fun `invoke возвращает null при сетевой ошибке`() = runTest {
        coEvery { authRepository.restoreSession() } throws RuntimeException("network")
        val result = restoreSessionUseCase()
        assertNull(result)
    }
}


// ─── GetQuestionsUseCaseTest ──────────────────────────────────────────────────

class GetQuestionsUseCaseTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var learningRepository: LearningRepository
    private lateinit var getQuestionsUseCase: GetQuestionsUseCase

    private val fakeQuestions = listOf(
        Question(
            id = 1, type = "choice", text = "Q1", xpValue = 10,
            hint = null, explanationCorrect = null, explanationWrong = null,
            answers = emptyList(), matchingPairs = emptyList()
        ),
        Question(
            id = 2, type = "truefalse", text = "Q2", xpValue = 5,
            hint = null, explanationCorrect = null, explanationWrong = null,
            answers = emptyList(), matchingPairs = emptyList()
        ),
    )

    @Before
    fun setUp() {
        learningRepository  = mockk()
        getQuestionsUseCase = GetQuestionsUseCase(learningRepository)
    }

    @Test
    fun `invoke возвращает список вопросов`() = runTest {
        coEvery { learningRepository.getQuestions(42) } returns fakeQuestions
        val result = getQuestionsUseCase(42)
        assertEquals(2, result.size)
        assertEquals(1, result[0].id)
    }

    @Test
    fun `invoke передаёт правильный lessonId в репозиторий`() = runTest {
        coEvery { learningRepository.getQuestions(any()) } returns emptyList()
        getQuestionsUseCase(99)
        coVerify { learningRepository.getQuestions(99) }
    }

    @Test
    fun `invoke пробрасывает исключение при ошибке`() = runTest {
        coEvery { learningRepository.getQuestions(any()) } throws RuntimeException("404")
        try {
            getQuestionsUseCase(1)
            fail("Expected exception")
        } catch (e: RuntimeException) {
            assertEquals("404", e.message)
        }
    }

    @Test
    fun `invoke возвращает пустой список если вопросов нет`() = runTest {
        coEvery { learningRepository.getQuestions(any()) } returns emptyList()
        val result = getQuestionsUseCase(1)
        assertTrue(result.isEmpty())
    }
}


// ─── CheckQuestionUseCaseTest ─────────────────────────────────────────────────

class CheckQuestionUseCaseTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var learningRepository: LearningRepository
    private lateinit var checkQuestionUseCase: CheckQuestionUseCase

    private val correctResult = QuestionCheckResult(
        questionId = 1, isCorrect = true,
        explanation = "Верно!", correctAnswerIds = listOf(10), correctMatchingPairs = emptyList()
    )

    private val wrongResult = QuestionCheckResult(
        questionId = 1, isCorrect = false,
        explanation = "Неверно.", correctAnswerIds = listOf(10), correctMatchingPairs = emptyList()
    )

    @Before
    fun setUp() {
        learningRepository   = mockk()
        checkQuestionUseCase = CheckQuestionUseCase(learningRepository)
    }

    @Test
    fun `invoke возвращает isCorrect true при верном ответе`() = runTest {
        coEvery { learningRepository.checkQuestion(any(), any()) } returns correctResult
        val attempt = QuestionAttempt(
            questionId = 1, selectedAnswerIds = listOf(10), matchingPairs = emptyList()
        )
        val result = checkQuestionUseCase(attempt)
        assertTrue(result.isCorrect)
    }

    @Test
    fun `invoke возвращает isCorrect false при неверном ответе`() = runTest {
        coEvery { learningRepository.checkQuestion(any(), any()) } returns wrongResult
        val attempt = QuestionAttempt(
            questionId = 1, selectedAnswerIds = listOf(11), matchingPairs = emptyList()
        )
        val result = checkQuestionUseCase(attempt)
        assertFalse(result.isCorrect)
    }

    @Test
    fun `invoke передаёт правильные параметры в репозиторий`() = runTest {
        coEvery { learningRepository.checkQuestion(any(), any()) } returns correctResult
        val attempt = QuestionAttempt(
            questionId = 5, selectedAnswerIds = listOf(20), matchingPairs = emptyList()
        )
        checkQuestionUseCase(attempt)
        coVerify { learningRepository.checkQuestion(attempt, null) }
    }

    @Test
    fun `invoke пробрасывает исключение при сетевой ошибке`() = runTest {
        coEvery { learningRepository.checkQuestion(any(), any()) } throws RuntimeException("timeout")
        val attempt = QuestionAttempt(1, listOf(10), emptyList())
        try {
            checkQuestionUseCase(attempt)
            fail("Expected exception")
        } catch (e: RuntimeException) {
            assertEquals("timeout", e.message)
        }
    }
}


// ─── SubmitLessonAttemptUseCaseTest ──────────────────────────────────────────

class SubmitLessonAttemptUseCaseTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var learningRepository: LearningRepository
    private lateinit var submitLessonAttemptUseCase: SubmitLessonAttemptUseCase

    private val fakeResult = LessonResult(
        isCompleted = true, score = 3, maxScore = 3,
        xpEarned = 30, totalXp = 130, newLevel = 2,
        streakDays = 5, previousStreak = 4,
        streakIncreased = true, streakActive = true,
        newAchievements = emptyList(), questionReviews = emptyList()
    )

    @Before
    fun setUp() {
        learningRepository         = mockk()
        submitLessonAttemptUseCase = SubmitLessonAttemptUseCase(learningRepository)
    }

    @Test
    fun `invoke передаёт attempt в репозиторий и возвращает результат`() = runTest {
        val attempt = LessonAttempt(lessonId = 1, answers = emptyList())
        coEvery { learningRepository.submitLessonAttempt(any()) } returns fakeResult

        val result = submitLessonAttemptUseCase(attempt)
        assertEquals(fakeResult, result)
        coVerify { learningRepository.submitLessonAttempt(attempt) }
    }

    @Test
    fun `invoke isCompleted false если результат не пройден`() = runTest {
        val attempt = LessonAttempt(lessonId = 1, answers = emptyList())
        coEvery { learningRepository.submitLessonAttempt(any()) } returns
            fakeResult.copy(isCompleted = false, xpEarned = 0)

        val result = submitLessonAttemptUseCase(attempt)
        assertFalse(result.isCompleted)
        assertEquals(0, result.xpEarned)
    }

    @Test
    fun `invoke пробрасывает исключение при ошибке сервера`() = runTest {
        coEvery { learningRepository.submitLessonAttempt(any()) } throws RuntimeException("500")
        val attempt = LessonAttempt(lessonId = 1, answers = emptyList())
        try {
            submitLessonAttemptUseCase(attempt)
            fail("Expected exception")
        } catch (e: RuntimeException) {
            assertEquals("500", e.message)
        }
    }
}


// ─── GetLeaderboardUseCaseTest ────────────────────────────────────────────────

class GetLeaderboardUseCaseTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var learningRepository: LearningRepository
    private lateinit var getLeaderboardUseCase: GetLeaderboardUseCase

    private val fakeLeaderboard = listOf(
        LeaderboardEntry(1, 1, "Иван", null, 500, 5, 10, "gold"),
        LeaderboardEntry(2, 2, "Пётр", null, 300, 3, 2, "silver"),
        LeaderboardEntry(3, 3, "Мария", null, 100, 1, 0, "bronze"),
    )

    @Before
    fun setUp() {
        learningRepository    = mockk()
        getLeaderboardUseCase = GetLeaderboardUseCase(learningRepository)
    }

    @Test
    fun `invoke без фильтра возвращает весь список`() = runTest {
        coEvery { learningRepository.getLeaderboard(null) } returns fakeLeaderboard
        val result = getLeaderboardUseCase(null)
        assertEquals(3, result.size)
    }

    @Test
    fun `invoke с tier фильтром передаёт его в репозиторий`() = runTest {
        coEvery { learningRepository.getLeaderboard("gold") } returns
            fakeLeaderboard.filter { it.tier == "gold" }
        val result = getLeaderboardUseCase("gold")
        assertEquals(1, result.size)
        assertEquals("gold", result[0].tier)
        coVerify { learningRepository.getLeaderboard("gold") }
    }

    @Test
    fun `invoke возвращает пустой список если нет участников`() = runTest {
        coEvery { learningRepository.getLeaderboard(any()) } returns emptyList()
        val result = getLeaderboardUseCase(null)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `invoke пробрасывает исключение при ошибке`() = runTest {
        coEvery { learningRepository.getLeaderboard(any()) } throws RuntimeException("net")
        try {
            getLeaderboardUseCase(null)
            fail("Expected exception")
        } catch (e: RuntimeException) {
            assertEquals("net", e.message)
        }
    }
}
