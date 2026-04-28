package com.example.profdevelop.presentation.navigation

sealed class AppDestination(val route: String) {
    data object Splash : AppDestination("splash")
    data object Login : AppDestination("login")
    data object Main : AppDestination("main")
    data object Home : AppDestination("home")
    data object Practice : AppDestination("practice")
    data object Quests : AppDestination("quests")
    data object Achievements : AppDestination("achievements")
    data object Profile : AppDestination("profile")
    data object Course : AppDestination("course/{courseId}/{courseTitle}")
    data object Lesson : AppDestination("lesson/{lessonId}/{lessonTitle}")

    companion object {
        fun courseRoute(courseId: Int, courseTitle: String): String =
            "course/$courseId/${courseTitle.encode()}"

        fun lessonRoute(lessonId: Int, lessonTitle: String): String =
            "lesson/$lessonId/${lessonTitle.encode()}"
    }
}

private fun String.encode(): String = java.net.URLEncoder.encode(this, Charsets.UTF_8.name())
