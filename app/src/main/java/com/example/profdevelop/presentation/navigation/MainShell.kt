package com.example.profdevelop.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    val iconActive: ImageVector,
    val iconInactive: ImageVector
)

private val tabs = listOf(
    TabItem(AppDestination.Home, "Главная", Icons.Filled.Home, Icons.Outlined.Home),
    TabItem(AppDestination.Practice, "Практика", Icons.Filled.FitnessCenter, Icons.Outlined.FitnessCenter),
    TabItem(AppDestination.Quests, "Задания", Icons.Filled.Flag, Icons.Outlined.Flag),
    TabItem(AppDestination.Achievements, "Награды", Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents),
    TabItem(AppDestination.Profile, "Профиль", Icons.Filled.Person, Icons.Outlined.Person),
)

@Composable
fun MainShell(
    module: AppModule,
    refreshToken: Int,
    onOpenCourse: (Int, String) -> Unit,
    onOpenLesson: (Int, String) -> Unit,
    onLoggedOut: () -> Unit,
    onOpenSettings: () -> Unit = {}
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
                            getAchievementsUseCase = module.getAchievementsUseCase,
                            getXpBoostStatusUseCase = module.getXpBoostStatusUseCase,
                            activateXpBoostUseCase = module.activateXpBoostUseCase
                        )
                    )
                    HomeScreen(
                        viewModel = viewModel,
                        refreshToken = refreshToken,
                        onOpenCourse = onOpenCourse,
                        onOpenLesson = onOpenLesson,
                        onOpenProfile = {
                            tabNav.navigate(AppDestination.Profile.route) {
                                popUpTo(tabNav.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }

                composable(AppDestination.Practice.route) {
                    val viewModel: PracticeViewModel = viewModel(
                        factory = PracticeViewModelFactory(
                            getPracticeQuestionsUseCase = module.getPracticeQuestionsUseCase,
                            checkQuestionUseCase = module.checkQuestionUseCase
                        )
                    )
                    PracticeScreen(
                        viewModel = viewModel,
                        onClose = {
                            tabNav.navigate(AppDestination.Home.route) {
                                popUpTo(tabNav.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
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
                            logoutUseCase = module.logoutUseCase,
                            getLeaderboardUseCase = module.getLeaderboardUseCase
                        )
                    )
                    ProfileScreen(
                        viewModel = viewModel,
                        onLoggedOut = onLoggedOut,
                        onOpenSettings = onOpenSettings
                    )
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
                    Icon(
                        imageVector = if (selected) tab.iconActive else tab.iconInactive,
                        contentDescription = tab.label,
                        modifier = Modifier.size(24.dp)
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
