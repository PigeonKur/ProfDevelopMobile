package com.example.profdevelop.domain.model

data class Question(
    val id: Int,
    val type: String,
    val text: String,
    val xpValue: Int,
    val hint: String?,
    val explanationCorrect: String?,
    val explanationWrong: String?,
    val answers: List<AnswerOption>,
    val matchingPairs: List<MatchingPair>
)

data class AnswerOption(
    val id: Int,
    val text: String,
    val orderIndex: Int
)

data class MatchingPair(
    val id: Int,
    val leftText: String,
    val rightText: String,
    val orderIndex: Int
)
