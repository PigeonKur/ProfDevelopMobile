package com.example.profdevelop.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.profdevelop.di.AppModule
import com.example.profdevelop.presentation.screens.achievements.AchievementsScreen
import com.example.profdevelop.presentation.screens.achievements.AchievementsViewModel
import com.example.profdevelop.presentation.screens.achievements.AchievementsViewModelFactory
import com.example.profdevelop.presentation.screens.home.HomeScreen
import com.example.profdevelop.presentation.screens.home.HomeViewModel
import com.example.profdevelop.presentation.screens.home.HomeViewModelFactory
import com.example.profdevelop.presentation.screens.practice.PracticeScreen
import com.example.profdevelop.presentation.screens.practice.PracticeViewModel
import com.example.profdevelop.presentation.screens.practice.PracticeViewModelFactory
import com.example.profdevelop.presentation.screens.profile.ProfileScreen
import com.example.profdevelop.presentation.screens.profile.ProfileViewModel
import com.example.profdevelop.presentation.screens.profile.ProfileViewModelFactory
import com.example.profdevelop.presentation.screens.quests.QuestsScreen
import com.example.profdevelop.presentation.theme.BrandBackground
import com.example.profdevelop.presentation.theme.BrandGreen
import com.example.profdevelop.presentation.theme.BrandGreenSoft
import com.example.profdevelop.presentation.theme.BrandMuted
import com.example.profdevelop.presentation.theme.BrandSurface

private data class TabItem(
    val destination: AppDestination,
    val label: String,
    val emoji: String
)

private val tabs = listOf(
    TabItem(AppDestination.Home, "Главная", "🏠"),
    TabItem(AppDestination.Practice, "Практика", "💪"),
    TabItem(AppDestination.Quests, "Задания", "🎯"),
    TabItem(AppDestination.Achievements, "Награды", "🏆"),
    TabItem(AppDestination.Profile, "Профиль", "👤"),
)

@Composable
fun MainShell(
    module: AppModule,
    refreshToken: Int,
    onOpenCourse: (Int, String) -> Unit,
    onOpenLesson: (Int, String) -> Unit,
    onLoggedOut: () -> Unit
) {
    val tabNav = rememberNavController()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BrandBackground,
        bottomBar = { BottomBar(tabNav) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BrandBackground)
        ) {
            NavHost(
                navController = tabNav,
                startDestination = AppDestination.Home.route
            ) {
                composable(AppDestination.Home.route) {
                    val viewModel: HomeViewModel = viewModel(
                        factory = HomeViewModelFactory(
                            getAssignedCoursesUseCase = module.getAssignedCoursesUseCase,
                            getLessonsUseCase = module.getLessonsUseCase,
                            getStoredSessionUseCase = module.getStoredSessionUseCase,
                            getAchievementsUseCase = module.getAchievementsUseCase
                        )
                    )
                    HomeScreen(
                        viewModel = viewModel,
                        refreshToken = refreshToken,
                        onOpenCourse = onOpenCourse,
                        onOpenLesson = onOpenLesson
                    )
                }

                composable(AppDestination.Practice.route) {
                    val viewModel: PracticeViewModel = viewModel(
                        factory = PracticeViewModelFactory(
                            getPracticeQuestionsUseCase = module.getPracticeQuestionsUseCase,
                            checkQuestionUseCase = module.checkQuestionUseCase
                        )
                    )
                    PracticeScreen(viewModel = viewModel)
                }

                composable(AppDestination.Quests.route) {
                    QuestsScreen(getStoredSessionUseCase = module.getStoredSessionUseCase)
                }

                composable(AppDestination.Achievements.route) {
                    val viewModel: AchievementsViewModel = viewModel(
                        factory = AchievementsViewModelFactory(
                            getStoredSessionUseCase = module.getStoredSessionUseCase,
                            getAchievementsUseCase = module.getAchievementsUseCase
                        )
                    )
                    AchievementsScreen(viewModel = viewModel)
                }

                composable(AppDestination.Profile.route) {
                    val viewModel: ProfileViewModel = viewModel(
                        factory = ProfileViewModelFactory(
                            getStoredSessionUseCase = module.getStoredSessionUseCase,
                            logoutUseCase = module.logoutUseCase
                        )
                    )
                    ProfileScreen(viewModel = viewModel, onLoggedOut = onLoggedOut)
                }
            }
        }
    }
}

@Composable
private fun BottomBar(navController: NavController) {
    val backEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backEntry?.destination?.route

    NavigationBar(
        containerColor = BrandSurface,
        contentColor = BrandMuted,
        tonalElevation = 0.dp
    ) {
        tabs.forEach { tab ->
            val selected = currentRoute == tab.destination.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        navController.navigate(tab.destination.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Text(
                        text = tab.emoji,
                        fontSize = 22.sp
                    )
                },
                label = {
                    Text(
                        text = tab.label,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = BrandGreen,
                    selectedTextColor = BrandGreen,
                    indicatorColor = BrandGreenSoft,
                    unselectedIconColor = BrandMuted,
                    unselectedTextColor = BrandMuted
                )
            )
        }
    }
}
