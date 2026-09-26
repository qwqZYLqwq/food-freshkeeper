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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

    val tabOrder = remember { mapOf("home" to 0, "list" to 1, "tips" to 2, "settings" to 3) }
    var slideDirection by remember { mutableStateOf(AnimatedContentTransitionScope.SlideDirection.Left) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                AppBottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        if (route != currentRoute) {
                            val fromIndex = tabOrder[currentRoute] ?: 0
                            val toIndex = tabOrder[route] ?: 0
                            slideDirection = if (toIndex >= fromIndex) {
                                AnimatedContentTransitionScope.SlideDirection.Left
                            } else {
                                AnimatedContentTransitionScope.SlideDirection.Right
                            }

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
            // 菜单切换动画仅在四大 Tab 之间按空间相对位置平移，进入二级界面（如详情页）采用原地无滑动卡片扩展
            enterTransition = {
                val initialRoute = initialState.destination.route?.substringBefore("?")?.substringBefore("/")
                val targetRoute = targetState.destination.route?.substringBefore("?")?.substringBefore("/")
                val isTabSwitch = (initialRoute in tabOrder || initialRoute == null) && targetRoute in tabOrder
                if (isTabSwitch) {
                    slideIntoContainer(slideDirection, animationSpec = tween(220))
                } else {
                    androidx.compose.animation.fadeIn(animationSpec = tween(250))
                }
            },
            exitTransition = {
                val initialRoute = initialState.destination.route?.substringBefore("?")?.substringBefore("/")
                val targetRoute = targetState.destination.route?.substringBefore("?")?.substringBefore("/")
                val isTabSwitch = initialRoute in tabOrder && (targetRoute in tabOrder || targetRoute == null)
                if (isTabSwitch) {
                    slideOutOfContainer(slideDirection, animationSpec = tween(220))
                } else {
                    androidx.compose.animation.fadeOut(animationSpec = tween(200))
                }
            },
            popEnterTransition = {
                val initialRoute = initialState.destination.route?.substringBefore("?")?.substringBefore("/")
                val targetRoute = targetState.destination.route?.substringBefore("?")?.substringBefore("/")
                val isTabSwitch = initialRoute in tabOrder && targetRoute in tabOrder
                if (isTabSwitch) {
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(220))
                } else {
                    androidx.compose.animation.fadeIn(animationSpec = tween(250))
                }
            },
            popExitTransition = {
                val initialRoute = initialState.destination.route?.substringBefore("?")?.substringBefore("/")
                val targetRoute = targetState.destination.route?.substringBefore("?")?.substringBefore("/")
                val isTabSwitch = initialRoute in tabOrder && targetRoute in tabOrder
                if (isTabSwitch) {
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(220))
                } else {
                    androidx.compose.animation.fadeOut(animationSpec = tween(200))
                }
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
                ),
                enterTransition = {
                    androidx.compose.animation.fadeIn(animationSpec = tween(280)) +
                    androidx.compose.animation.scaleIn(
                        initialScale = 0.70f,
                        transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0.4f),
                        animationSpec = tween(360, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                    )
                },
                exitTransition = {
                    androidx.compose.animation.fadeOut(animationSpec = tween(200))
                },
                popEnterTransition = {
                    androidx.compose.animation.fadeIn(animationSpec = tween(250))
                },
                popExitTransition = {
                    androidx.compose.animation.fadeOut(animationSpec = tween(220)) +
                    androidx.compose.animation.scaleOut(
                        targetScale = 0.70f,
                        transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0.4f),
                        animationSpec = tween(280, easing = androidx.compose.animation.core.FastOutSlowInEasing)
                    )
                }
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
