package com.example.profdevelop.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.profdevelop.di.AppModule
import com.example.profdevelop.presentation.screens.auth.AuthViewModel
import com.example.profdevelop.presentation.screens.auth.AuthViewModelFactory
import com.example.profdevelop.presentation.screens.auth.LoginScreen
import com.example.profdevelop.presentation.screens.course.CourseScreen
import com.example.profdevelop.presentation.screens.course.CourseViewModel
import com.example.profdevelop.presentation.screens.course.CourseViewModelFactory
import com.example.profdevelop.presentation.screens.home.HomeScreen
import com.example.profdevelop.presentation.screens.home.HomeViewModel
import com.example.profdevelop.presentation.screens.home.HomeViewModelFactory
import com.example.profdevelop.presentation.screens.lesson.LessonScreen
import com.example.profdevelop.presentation.screens.lesson.LessonViewModel
import com.example.profdevelop.presentation.screens.lesson.LessonViewModelFactory
import com.example.profdevelop.presentation.screens.splash.SplashScreen
import com.example.profdevelop.presentation.screens.splash.SplashViewModel
import com.example.profdevelop.presentation.screens.splash.SplashViewModelFactory
import java.net.URLDecoder

@Composable
fun ProfDevelopNavHost() {
    val context = LocalContext.current
    val module = remember { AppModule(context.applicationContext) }
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppDestination.Splash.route
    ) {
        composable(AppDestination.Splash.route) {
            val viewModel: SplashViewModel = viewModel(
                factory = SplashViewModelFactory(module.restoreSessionUseCase)
            )
            SplashScreen(
                viewModel = viewModel,
                onOpenHome = {
                    navController.navigate(AppDestination.Home.route) {
                        popUpTo(AppDestination.Splash.route) { inclusive = true }
                    }
                },
                onOpenLogin = {
                    navController.navigate(AppDestination.Login.route) {
                        popUpTo(AppDestination.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(AppDestination.Login.route) {
            val viewModel: AuthViewModel = viewModel(
                factory = AuthViewModelFactory(
                    loginUseCase = module.loginUseCase,
                    getApiUrlUseCase = module.getApiUrlUseCase,
                    updateApiUrlUseCase = module.updateApiUrlUseCase
                )
            )
            LoginScreen(
                viewModel = viewModel,
                onAuthorized = {
                    navController.navigate(AppDestination.Home.route) {
                        popUpTo(AppDestination.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(AppDestination.Home.route) {
            val viewModel: HomeViewModel = viewModel(
                factory = HomeViewModelFactory(
                    getAssignedCoursesUseCase = module.getAssignedCoursesUseCase,
                    getLessonsUseCase = module.getLessonsUseCase,
                    authPreferences = module.preferencesDataSource
                )
            )
            HomeScreen(
                viewModel = viewModel,
                onOpenCourse = { courseId, courseTitle ->
                    navController.navigate(AppDestination.courseRoute(courseId, courseTitle))
                },
                onOpenLesson = { lessonId, lessonTitle ->
                    navController.navigate(AppDestination.lessonRoute(lessonId, lessonTitle))
                }
            )
        }

        composable(
            route = AppDestination.Course.route,
            arguments = listOf(
                navArgument("courseId") { type = NavType.IntType },
                navArgument("courseTitle") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getInt("courseId") ?: 0
            val courseTitle = decode(backStackEntry.arguments?.getString("courseTitle"))
            val viewModel: CourseViewModel = viewModel(
                factory = CourseViewModelFactory(
                    courseId = courseId,
                    getLessonsUseCase = module.getLessonsUseCase
                )
            )
            CourseScreen(
                title = courseTitle,
                viewModel = viewModel,
                onOpenLesson = { lessonId, lessonTitle ->
                    navController.navigate(AppDestination.lessonRoute(lessonId, lessonTitle))
                }
            )
        }

        composable(
            route = AppDestination.Lesson.route,
            arguments = listOf(
                navArgument("lessonId") { type = NavType.IntType },
                navArgument("lessonTitle") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getInt("lessonId") ?: 0
            val lessonTitle = decode(backStackEntry.arguments?.getString("lessonTitle"))
            val viewModel: LessonViewModel = viewModel(
                factory = LessonViewModelFactory(
                    lessonId = lessonId,
                    getQuestionsUseCase = module.getQuestionsUseCase,
                    submitLessonAttemptUseCase = module.submitLessonAttemptUseCase
                )
            )
            LessonScreen(
                title = lessonTitle,
                viewModel = viewModel,
                onFinished = { navController.popBackStack() }
            )
        }
    }
}

private fun decode(value: String?): String =
    if (value.isNullOrBlank()) "" else URLDecoder.decode(value, Charsets.UTF_8.name())
