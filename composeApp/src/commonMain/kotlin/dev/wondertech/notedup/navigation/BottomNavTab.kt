package dev.wondertech.notedup.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import cafe.adriel.voyager.transitions.SlideTransition
import dev.wondertech.notedup.screens.CalendarScreen
import dev.wondertech.notedup.screens.MainScreen
import notedup.composeapp.generated.resources.Res
import notedup.composeapp.generated.resources.calendar_bottom_nav
import notedup.composeapp.generated.resources.home_bottom_nav
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource


sealed class BottomNavTab(
    val title: String,
    val icon: DrawableResource
) : Tab {
    /**
     * Home tab - Shows the main task list screen
     */
    object HomeTab : BottomNavTab(
        title = "Home",
        icon = Res.drawable.home_bottom_nav
    ) {
        override val options: TabOptions
            @Composable
            get() {
                val icon = painterResource(Res.drawable.home_bottom_nav)
                return remember {
                    TabOptions(
                        index = 0u,
                        title = "Home",
                        icon = icon
                    )
                }
            }

        @Composable
        override fun Content() {
            Navigator(MainScreen()) { navigator ->
                SlideTransition(navigator)
            }
        }
    }


    /**
     * Calendar tab - Shows the calendar/schedule screen
     */
    object CalendarTab : BottomNavTab(
        title = "Calendar",
        icon = Res.drawable.calendar_bottom_nav
    ) {
        override val options: TabOptions
            @Composable
            get() {
                val icon = painterResource(Res.drawable.calendar_bottom_nav)
                return remember {
                    TabOptions(
                        index = 1u,
                        title = "Calendar",
                        icon = icon
                    )
                }
            }

        @Composable
        override fun Content() {
            Navigator(CalendarScreen()) { navigator ->
                SlideTransition(navigator)
            }
        }
    }
}
