package com.example.profdevelop.presentation.screens.lesson

import com.example.profdevelop.domain.model.AnswerOption
import com.example.profdevelop.domain.model.LessonAttempt
import com.example.profdevelop.domain.model.LessonResult
import com.example.profdevelop.domain.model.MatchingPair
import com.example.profdevelop.domain.model.Question
import com.example.profdevelop.domain.model.QuestionAttempt
import com.example.profdevelop.domain.model.QuestionCheckResult
import com.example.profdevelop.domain.usecase.CheckQuestionUseCase
import com.example.profdevelop.domain.usecase.GetQuestionsUseCase
import com.example.profdevelop.domain.usecase.SubmitLessonAttemptUseCase
import com.example.profdevelop.utils.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LessonViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var getQuestionsUseCase: GetQuestionsUseCase
    private lateinit var checkQuestionUseCase: CheckQuestionUseCase
    private lateinit var submitLessonAttemptUseCase: SubmitLessonAttemptUseCase
    private lateinit var viewModel: LessonViewModel

    private val choiceQuestion = Question(
        id = 1, type = "choice", text = "Вопрос 1", xpValue = 10,
        hint = null, explanationCorrect = "Верно!", explanationWrong = "Неверно.",
        answers = listOf(
            AnswerOption(id = 10, text = "Ответ А", orderIndex = 0),
            AnswerOption(id = 11, text = "Ответ Б", orderIndex = 1),
        ),
        matchingPairs = emptyList()
    )

    private val trueFalseQuestion = Question(
        id = 2, type = "truefalse", text = "Это правда?", xpValue = 5,
        hint = null, explanationCorrect = null, explanationWrong = null,
        answers = listOf(
            AnswerOption(id = 20, text = "Верно", orderIndex = 0),
            AnswerOption(id = 21, text = "Неверно", orderIndex = 1),
        ),
        matchingPairs = emptyList()
    )

    private val matchingQuestion = Question(
        id = 3, type = "matching", text = "Соответствие", xpValue = 15,
        hint = null, explanationCorrect = null, explanationWrong = null,
        answers = emptyList(),
        matchingPairs = listOf(
            MatchingPair(id = 30, leftText = "Левый 1", rightText = "Правый 1", orderIndex = 0),
            MatchingPair(id = 31, leftText = "Левый 2", rightText = "Правый 2", orderIndex = 1),
        )
    )

    private val fakeResult = LessonResult(
        isCompleted = true, score = 3, maxScore = 3,
        xpEarned = 30, totalXp = 130, newLevel = 2,
        streakDays = 5, previousStreak = 4,
        streakIncreased = true, streakActive = true,
        newAchievements = emptyList(), questionReviews = emptyList()
    )

    private fun buildViewModel(questions: List<Question> = listOf(choiceQuestion)) {
        coEvery { getQuestionsUseCase(any()) } returns questions
        viewModel = LessonViewModel(
            lessonId = 1,
            getQuestionsUseCase = getQuestionsUseCase,
            checkQuestionUseCase = checkQuestionUseCase,
            submitLessonAttemptUseCase = submitLessonAttemptUseCase
        )
    }

    @Before
    fun setUp() {
        getQuestionsUseCase        = mockk()
        checkQuestionUseCase       = mockk()
        submitLessonAttemptUseCase = mockk()
    }

    // ── load ──────────────────────────────────────────────────────────────────

    @Test
    fun `load загружает вопросы и формирует очередь`() = runTest {
        buildViewModel(listOf(choiceQuestion, trueFalseQuestion))
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(2, state.questions.size)
        assertEquals(listOf(1, 2), state.questionQueue)
        assertEquals(choiceQuestion, state.currentQuestion)
    }

    @Test
    fun `load при ошибке устанавливает error`() = runTest {
        coEvery { getQuestionsUseCase(any()) } throws RuntimeException("network error")
        viewModel = LessonViewModel(1, getQuestionsUseCase, checkQuestionUseCase, submitLessonAttemptUseCase)
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertTrue(state.questions.isEmpty())
    }

    // ── selectAnswer ──────────────────────────────────────────────────────────

    @Test
    fun `selectAnswer для truefalse заменяет предыдущий выбор`() = runTest {
        buildViewModel(listOf(trueFalseQuestion))
        viewModel.selectAnswer(2, 20)
        assertEquals(setOf(20), viewModel.state.value.selectedAnswers[2])
        viewModel.selectAnswer(2, 21)
        assertEquals(setOf(21), viewModel.state.value.selectedAnswers[2])
    }

    @Test
    fun `selectAnswer для choice добавляет ответ при первом нажатии`() = runTest {
        buildViewModel(listOf(choiceQuestion))
        viewModel.selectAnswer(1, 10)
        assertTrue(viewModel.state.value.selectedAnswers[1]!!.contains(10))
    }

    @Test
    fun `selectAnswer для choice снимает ответ при повторном нажатии`() = runTest {
        buildViewModel(listOf(choiceQuestion))
        viewModel.selectAnswer(1, 10)
        viewModel.selectAnswer(1, 10)
        assertTrue(viewModel.state.value.selectedAnswers[1].isNullOrEmpty())
    }

    @Test
    fun `selectAnswer не работает когда есть feedback`() = runTest {
        coEvery { checkQuestionUseCase(any(), any()) } returns
            QuestionCheckResult(1, true, "Верно!", emptyList(), emptyList())
        coEvery { submitLessonAttemptUseCase(any()) } returns fakeResult

        buildViewModel(listOf(choiceQuestion))
        viewModel.selectAnswer(1, 10)
        viewModel.checkCurrentQuestion()

        // Попытка сменить ответ при активном feedback
        viewModel.selectAnswer(1, 11)
        assertFalse(viewModel.state.value.selectedAnswers[1]!!.contains(11))
    }

    // ── selectMatching ────────────────────────────────────────────────────────

    @Test
    fun `selectMatching добавляет пару соответствия`() = runTest {
        buildViewModel(listOf(matchingQuestion))
        viewModel.selectMatching(3, 30, 30)
        assertEquals(30, viewModel.state.value.selectedMatches[3]!![30])
    }

    @Test
    fun `selectMatching освобождает правый элемент при переназначении`() = runTest {
        buildViewModel(listOf(matchingQuestion))
        viewModel.selectMatching(3, 30, 30)
        // Другой левый берёт тот же правый — первая связь должна разорваться
        viewModel.selectMatching(3, 31, 30)
        val matches = viewModel.state.value.selectedMatches[3]!!
        assertNull(matches[30])
        assertEquals(30, matches[31])
    }

    @Test
    fun `selectMatching не работает когда есть feedback`() = runTest {
        coEvery { checkQuestionUseCase(any(), any()) } returns
            QuestionCheckResult(3, false, null, emptyList(), emptyList())
        buildViewModel(listOf(matchingQuestion))
        viewModel.selectMatching(3, 30, 30)
        viewModel.selectMatching(3, 31, 31)
        viewModel.checkCurrentQuestion()
        val before = viewModel.state.value.selectedMatches[3]!!.toMap()
        viewModel.selectMatching(3, 30, 31)
        assertEquals(before, viewModel.state.value.selectedMatches[3])
    }

    // ── checkCurrentQuestion — валидация ──────────────────────────────────────

    @Test
    fun `checkCurrentQuestion без выбранного ответа устанавливает error`() = runTest {
        buildViewModel(listOf(choiceQuestion))
        viewModel.checkCurrentQuestion()
        assertEquals("Сначала выберите ответ.", viewModel.state.value.error)
        coVerify(exactly = 0) { checkQuestionUseCase(any(), any()) }
    }

    @Test
    fun `checkCurrentQuestion для matching без всех пар устанавливает error`() = runTest {
        buildViewModel(listOf(matchingQuestion))
        viewModel.selectMatching(3, 30, 30) // только одна из двух пар
        viewModel.checkCurrentQuestion()
        assertEquals("Сначала выберите ответ.", viewModel.state.value.error)
    }

    // ── checkCurrentQuestion — верный ответ ───────────────────────────────────

    @Test
    fun `checkCurrentQuestion при верном ответе устанавливает feedback isCorrect=true`() = runTest {
        coEvery { checkQuestionUseCase(any(), any()) } returns
            QuestionCheckResult(1, true, "Отлично!", emptyList(), emptyList())
        coEvery { submitLessonAttemptUseCase(any()) } returns fakeResult

        buildViewModel(listOf(choiceQuestion))
        viewModel.selectAnswer(1, 10)
        viewModel.checkCurrentQuestion()

        val fb = viewModel.state.value.feedback
        assertNotNull(fb)
        assertTrue(fb!!.isCorrect)
        assertEquals("Отлично!", fb.message)
    }

    @Test
    fun `checkCurrentQuestion при верном ответе добавляет id в completedQuestionIds`() = runTest {
        coEvery { checkQuestionUseCase(any(), any()) } returns
            QuestionCheckResult(1, true, null, emptyList(), emptyList())
        coEvery { submitLessonAttemptUseCase(any()) } returns fakeResult

        buildViewModel(listOf(choiceQuestion))
        viewModel.selectAnswer(1, 10)
        viewModel.checkCurrentQuestion()

        assertTrue(viewModel.state.value.completedQuestionIds.contains(1))
    }

    @Test
    fun `checkCurrentQuestion при верном ответе НЕ расширяет очередь`() = runTest {
        coEvery { checkQuestionUseCase(any(), any()) } returns
            QuestionCheckResult(1, true, null, emptyList(), emptyList())
        coEvery { submitLessonAttemptUseCase(any()) } returns fakeResult

        buildViewModel(listOf(choiceQuestion))
        val queueBefore = viewModel.state.value.questionQueue.size
        viewModel.selectAnswer(1, 10)
        viewModel.checkCurrentQuestion()

        assertEquals(queueBefore, viewModel.state.value.questionQueue.size)
    }

    // ── checkCurrentQuestion — неверный ответ ─────────────────────────────────

    @Test
    fun `checkCurrentQuestion при неверном ответе устанавливает feedback isCorrect=false`() = runTest {
        coEvery { checkQuestionUseCase(any(), any()) } returns
            QuestionCheckResult(1, false, "Попробуй ещё раз.", emptyList(), emptyList())

        buildViewModel(listOf(choiceQuestion))
        viewModel.selectAnswer(1, 11)
        viewModel.checkCurrentQuestion()

        val fb = viewModel.state.value.feedback
        assertNotNull(fb)
        assertFalse(fb!!.isCorrect)
    }

    @Test
    fun `checkCurrentQuestion при неверном ответе добавляет id в конец очереди`() = runTest {
        coEvery { checkQuestionUseCase(any(), any()) } returns
            QuestionCheckResult(1, false, null, emptyList(), emptyList())

        buildViewModel(listOf(choiceQuestion, trueFalseQuestion))
        viewModel.selectAnswer(1, 11)
        viewModel.checkCurrentQuestion()

        val queue = viewModel.state.value.questionQueue
        assertEquals(3, queue.size)
        assertEquals(1, queue.last()) // вопрос 1 добавлен в конец
    }

    @Test
    fun `checkCurrentQuestion при неверном ответе НЕ добавляет в completedQuestionIds`() = runTest {
        coEvery { checkQuestionUseCase(any(), any()) } returns
            QuestionCheckResult(1, false, null, emptyList(), emptyList())

        buildViewModel(listOf(choiceQuestion))
        viewModel.selectAnswer(1, 11)
        viewModel.checkCurrentQuestion()

        assertFalse(viewModel.state.value.completedQuestionIds.contains(1))
    }

    // ── checkCurrentQuestion — fallback message ───────────────────────────────

    @Test
    fun `feedback message использует дефолтный текст если explanation null`() = runTest {
        coEvery { checkQuestionUseCase(any(), any()) } returns
            QuestionCheckResult(1, true, null, emptyList(), emptyList())
        coEvery { submitLessonAttemptUseCase(any()) } returns fakeResult

        buildViewModel(listOf(choiceQuestion))
        viewModel.selectAnswer(1, 10)
        viewModel.checkCurrentQuestion()

        assertEquals("Верно. Можно идти дальше.", viewModel.state.value.feedback!!.message)
    }

    @Test
    fun `feedback message использует дефолтный текст при неверном ответе и null explanation`() = runTest {
        coEvery { checkQuestionUseCase(any(), any()) } returns
            QuestionCheckResult(1, false, null, emptyList(), emptyList())

        buildViewModel(listOf(choiceQuestion))
        viewModel.selectAnswer(1, 11)
        viewModel.checkCurrentQuestion()

        assertEquals(
            "Ответ неверный. Этот вопрос вернётся в конец урока.",
            viewModel.state.value.feedback!!.message
        )
    }

    // ── continueAfterFeedback ─────────────────────────────────────────────────

    @Test
    fun `continueAfterFeedback без feedback ничего не делает`() = runTest {
        buildViewModel(listOf(choiceQuestion))
        val before = viewModel.state.value.currentQuestionIndex
        viewModel.continueAfterFeedback()
        assertEquals(before, viewModel.state.value.currentQuestionIndex)
    }

    @Test
    fun `continueAfterFeedback после верного ответа переходит к следующему вопросу`() = runTest {
        coEvery { checkQuestionUseCase(any(), any()) } returns
            QuestionCheckResult(1, true, null, emptyList(), emptyList())
        coEvery { submitLessonAttemptUseCase(any()) } returns fakeResult

        buildViewModel(listOf(choiceQuestion, trueFalseQuestion))
        viewModel.selectAnswer(1, 10)
        viewModel.checkCurrentQuestion()
        viewModel.continueAfterFeedback()

        assertNull(viewModel.state.value.feedback)
        assertEquals(1, viewModel.state.value.currentQuestionIndex)
    }

    @Test
    fun `continueAfterFeedback после последнего верного ответа вызывает submit`() = runTest {
        coEvery { checkQuestionUseCase(any(), any()) } returns
            QuestionCheckResult(1, true, null, emptyList(), emptyList())
        coEvery { submitLessonAttemptUseCase(any()) } returns fakeResult

        buildViewModel(listOf(choiceQuestion))
        viewModel.selectAnswer(1, 10)
        viewModel.checkCurrentQuestion()
        viewModel.continueAfterFeedback()

        assertTrue(viewModel.state.value.showResult)
        assertEquals(fakeResult, viewModel.state.value.result)
    }

    @Test
    fun `continueAfterFeedback после неверного ответа НЕ вызывает submit`() = runTest {
        coEvery { checkQuestionUseCase(any(), any()) } returns
            QuestionCheckResult(1, false, null, emptyList(), emptyList())

        buildViewModel(listOf(choiceQuestion))
        viewModel.selectAnswer(1, 11)
        viewModel.checkCurrentQuestion()
        viewModel.continueAfterFeedback()

        assertFalse(viewModel.state.value.showResult)
        coVerify(exactly = 0) { submitLessonAttemptUseCase(any()) }
    }

    // ── previousQuestion ──────────────────────────────────────────────────────

    @Test
    fun `previousQuestion уменьшает индекс`() = runTest {
        coEvery { checkQuestionUseCase(any(), any()) } returns
            QuestionCheckResult(1, true, null, emptyList(), emptyList())
        coEvery { submitLessonAttemptUseCase(any()) } returns fakeResult

        buildViewModel(listOf(choiceQuestion, trueFalseQuestion))
        viewModel.selectAnswer(1, 10)
        viewModel.checkCurrentQuestion()
        viewModel.continueAfterFeedback()
        assertEquals(1, viewModel.state.value.currentQuestionIndex)

        viewModel.previousQuestion()
        assertEquals(0, viewModel.state.value.currentQuestionIndex)
    }

    @Test
    fun `previousQuestion не уходит ниже нуля`() = runTest {
        buildViewModel(listOf(choiceQuestion))
        viewModel.previousQuestion()
        assertEquals(0, viewModel.state.value.currentQuestionIndex)
    }

    @Test
    fun `previousQuestion не работает при активном feedback`() = runTest {
        coEvery { checkQuestionUseCase(any(), any()) } returns
            QuestionCheckResult(1, false, null, emptyList(), emptyList())

        buildViewModel(listOf(choiceQuestion, trueFalseQuestion))
        viewModel.selectAnswer(1, 11)
        viewModel.checkCurrentQuestion()
        val indexBefore = viewModel.state.value.currentQuestionIndex
        viewModel.previousQuestion()
        assertEquals(indexBefore, viewModel.state.value.currentQuestionIndex)
    }

    // ── progressLabel ─────────────────────────────────────────────────────────

    @Test
    fun `progressLabel возвращает корректное значение`() = runTest {
        buildViewModel(listOf(choiceQuestion, trueFalseQuestion))
        assertEquals("0 из 2", viewModel.state.value.progressLabel)
    }

    // ── submit — ошибка ───────────────────────────────────────────────────────

    @Test
    fun `submit при ошибке устанавливает error`() = runTest {
        coEvery { checkQuestionUseCase(any(), any()) } returns
            QuestionCheckResult(1, true, null, emptyList(), emptyList())
        coEvery { submitLessonAttemptUseCase(any()) } throws RuntimeException("server error")

        buildViewModel(listOf(choiceQuestion))
        viewModel.selectAnswer(1, 10)
        viewModel.checkCurrentQuestion()
        viewModel.continueAfterFeedback()

        assertFalse(viewModel.state.value.showResult)
        assertNotNull(viewModel.state.value.error)
    }
}
