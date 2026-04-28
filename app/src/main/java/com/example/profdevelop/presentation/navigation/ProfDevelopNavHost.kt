package com.example.profdevelop.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.example.profdevelop.presentation.screens.lesson.LessonScreen
import com.example.profdevelop.presentation.screens.lesson.LessonViewModel
import com.example.profdevelop.presentation.screens.lesson.LessonViewModelFactory
import com.example.profdevelop.presentation.screens.settings.SettingsScreen
import com.example.profdevelop.presentation.screens.settings.SettingsViewModel
import com.example.profdevelop.presentation.screens.splash.SplashScreen
import com.example.profdevelop.presentation.screens.splash.SplashViewModel
import com.example.profdevelop.presentation.screens.splash.SplashViewModelFactory
import com.example.profdevelop.presentation.util.Haptics
import androidx.compose.runtime.LaunchedEffect
import java.net.URLDecoder

@Composable
fun ProfDevelopNavHost() {
    val context = LocalContext.current
    val module = remember { AppModule(context.applicationContext) }
    val navController = rememberNavController()

    LaunchedEffect(Unit) {
        module.settingsDataSource.flow.collect { settings ->
            Haptics.enabled = settings.hapticsEnabled
        }
    }

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
                    navController.navigate(AppDestination.Main.route) {
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
                    loginUseCase = module.loginUseCase
                )
            )
            LoginScreen(
                viewModel = viewModel,
                onAuthorized = {
                    navController.navigate(AppDestination.Main.route) {
                        popUpTo(AppDestination.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(AppDestination.Main.route) { backStackEntry ->
            val refreshToken = backStackEntry.savedStateHandle
                .getStateFlow("refreshToken", 0)
                .collectAsState()
                .value
            MainShell(
                module = module,
                refreshToken = refreshToken,
                onOpenCourse = { courseId, courseTitle ->
                    navController.navigate(AppDestination.courseRoute(courseId, courseTitle))
                },
                onOpenLesson = { lessonId, lessonTitle ->
                    navController.navigate(AppDestination.lessonRoute(lessonId, lessonTitle))
                },
                onLoggedOut = {
                    navController.navigate(AppDestination.Login.route) {
                        popUpTo(AppDestination.Main.route) { inclusive = true }
                    }
                },
                onOpenSettings = {
                    navController.navigate(AppDestination.Settings.route)
                }
            )
        }

        composable(AppDestination.Settings.route) {
            val viewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModel.Factory(
                    dataSource = module.settingsDataSource,
                    getApiUrlUseCase = module.getApiUrlUseCase,
                    updateApiUrlUseCase = module.updateApiUrlUseCase
                )
            )
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
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
            val refreshToken = backStackEntry.savedStateHandle
                .getStateFlow("refreshToken", 0)
                .collectAsState()
                .value
            val viewModel: CourseViewModel = viewModel(
                factory = CourseViewModelFactory(
                    courseId = courseId,
                    getLessonsUseCase = module.getLessonsUseCase
                )
            )
            CourseScreen(
                title = courseTitle,
                viewModel = viewModel,
                refreshToken = refreshToken,
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
                    checkQuestionUseCase = module.checkQuestionUseCase,
                    submitLessonAttemptUseCase = module.submitLessonAttemptUseCase
                )
            )
            LessonScreen(
                title = lessonTitle,
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onFinished = {
                    val previousEntry = navController.previousBackStackEntry
                    val current = previousEntry?.savedStateHandle?.get<Int>("refreshToken") ?: 0
                    previousEntry?.savedStateHandle?.set("refreshToken", current + 1)
                    navController.popBackStack()
                }
            )
        }
    }
}

private fun decode(value: String?): String =
    if (value.isNullOrBlank()) "" else URLDecoder.decode(value, Charsets.UTF_8.name())
