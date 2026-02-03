package dev.wondertech.notedup.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import cafe.adriel.voyager.transitions.SlideTransition
import dev.wondertech.notedup.common.NotedUpBottomNavBar
import dev.wondertech.notedup.navigation.BottomNavTab

class RootNavigationScreen : Screen {

    @Composable
    override fun Content() {
        var previousTabIndex by remember { mutableStateOf(0) }
        var showBottomBar by remember { mutableStateOf(true) }
        var addTaskNavigationCallback by remember { mutableStateOf<(() -> Unit)?>(null) }

        TabNavigator(BottomNavTab.HomeTab) {
            val tabNavigator = LocalTabNavigator.current
            val tabs = listOf(
                BottomNavTab.HomeTab,
                BottomNavTab.CalendarTab
            )
            val currentTabIndex = tabs.indexOfFirst { it.key == tabNavigator.current.key }.coerceAtLeast(0)

            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                bottomBar = {
                    AnimatedVisibility(
                        visible = showBottomBar,
                        enter = slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = tween(durationMillis = 300)
                        ) + fadeIn(animationSpec = tween(durationMillis = 300)),
                        exit = slideOutVertically(
                            targetOffsetY = { it },
                            animationSpec = tween(durationMillis = 250)
                        ) + fadeOut(animationSpec = tween(durationMillis = 250))
                    ) {
                        NotedUpBottomNavBar(
                            currentTab = tabNavigator.current,
                            onTabSelected = { tab ->
                                tabNavigator.current = tab
                            },
                            onAddTaskClick = {
                                addTaskNavigationCallback?.invoke()
                            }
                        )
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(paddingValues)
                ) {
                    AnimatedContent(
                        targetState = tabNavigator.current,
                        transitionSpec = {
                            val direction = if (currentTabIndex > previousTabIndex) 1 else -1

                            slideInHorizontally(
                                initialOffsetX = { it * direction },
                                animationSpec = tween(
                                    durationMillis = 400,
                                    easing = FastOutSlowInEasing
                                )
                            ) + fadeIn(
                                animationSpec = tween(durationMillis = 400)
                            ) togetherWith slideOutHorizontally(
                                targetOffsetX = { -it * direction },
                                animationSpec = tween(
                                    durationMillis = 400,
                                    easing = FastOutSlowInEasing
                                )
                            ) + fadeOut(
                                animationSpec = tween(durationMillis = 400)
                            )
                        },
                        modifier = Modifier.fillMaxSize()
                    ) { currentTab ->
                        previousTabIndex = currentTabIndex

                        Box(modifier = Modifier.fillMaxSize()) {
                            TabContentWithBottomBarControl(
                                tab = currentTab,
                                onBottomBarVisibilityChange = { visible ->
                                    showBottomBar = visible
                                },
                                onNavigationCallbackReady = { callback ->
                                    addTaskNavigationCallback = callback
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TabContentWithBottomBarControl(
    tab: Tab,
    onBottomBarVisibilityChange: (Boolean) -> Unit,
    onNavigationCallbackReady: (() -> Unit) -> Unit
) {
    if (tab is BottomNavTab) {
        TabContentWithAddTaskNavigation(
            tab = tab,
            onBottomBarVisibilityChange = onBottomBarVisibilityChange,
            onNavigationCallbackReady = onNavigationCallbackReady
        )
    } else {
        tab.Content()
    }
}

@Composable
private fun TabContentWithAddTaskNavigation(
    tab: BottomNavTab,
    onBottomBarVisibilityChange: (Boolean) -> Unit,
    onNavigationCallbackReady: (() -> Unit) -> Unit
) {
    when (tab) {
        is BottomNavTab.HomeTab -> {
            Navigator(MainScreen()) { navigator ->
                LaunchedEffect(navigator) {
                    onNavigationCallbackReady {
                        navigator.push(CreateTaskScreen(taskTimestampToEdit = null))
                    }
                }

                LaunchedEffect(navigator.size) {
                    val isAtRoot = navigator.size == 1
                    onBottomBarVisibilityChange(isAtRoot)
                }
                SlideTransition(navigator)
            }
        }

        is BottomNavTab.CalendarTab -> {
            Navigator(CalendarScreen()) { navigator ->
                LaunchedEffect(navigator) {
                    onNavigationCallbackReady {
                        navigator.push(CreateTaskScreen(taskTimestampToEdit = null))
                    }
                }

                LaunchedEffect(navigator.size) {
                    val isAtRoot = navigator.size == 1
                    onBottomBarVisibilityChange(isAtRoot)
                }

                SlideTransition(navigator)
            }
        }
    }
}
