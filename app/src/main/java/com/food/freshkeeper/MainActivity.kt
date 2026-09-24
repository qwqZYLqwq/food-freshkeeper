package com.food.freshkeeper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.food.freshkeeper.ui.components.AppBottomNavBar
import com.food.freshkeeper.ui.screens.*
import com.food.freshkeeper.ui.theme.FoodKeeperTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FoodKeeperTheme {
                FreshKeeperMainApp()
            }
        }
    }
}

@Composable
fun FreshKeeperMainApp(viewModel: FoodViewModel = viewModel()) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"

    val showBottomBar = currentRoute in listOf("home", "list", "tips", "settings")

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                AppBottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        if (route != currentRoute) {
                            if (route == "home") {
                                val popped = navController.popBackStack("home", inclusive = false)
                                if (!popped) {
                                    navController.navigate("home") {
                                        launchSingleTop = true
                                    }
                                }
                            } else {
                                navController.navigate(route) {
                                    popUpTo("home") {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues),
            // 升级需求4：动画不要渐变，改成利落干脆的左右平移滑动，时间220ms
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(220)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(220)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(220)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(220)
                )
            }
        ) {
            composable("home") {
                HomeScreen(navController = navController, viewModel = viewModel)
            }
            composable("list") {
                ListScreen(navController = navController, viewModel = viewModel)
            }
            composable("tips") {
                TipsScreen()
            }
            composable("settings") {
                SettingsScreen(navController = navController, viewModel = viewModel)
            }
            // 回收站页面
            composable("trash") {
                TrashScreen(navController = navController, viewModel = viewModel)
            }
            composable(
                route = "add_edit?foodId={foodId}",
                arguments = listOf(
                    navArgument("foodId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) { backStackEntry ->
                val foodId = backStackEntry.arguments?.getLong("foodId") ?: -1L
                AddEditScreen(
                    navController = navController,
                    viewModel = viewModel,
                    foodId = if (foodId > 0) foodId else null
                )
            }
            composable(
                route = "detail/{foodId}",
                arguments = listOf(
                    navArgument("foodId") {
                        type = NavType.LongType
                    }
                )
            ) { backStackEntry ->
                val foodId = backStackEntry.arguments?.getLong("foodId") ?: 0L
                DetailScreen(
                    navController = navController,
                    viewModel = viewModel,
                    foodId = foodId
                )
            }
        }
    }
}
