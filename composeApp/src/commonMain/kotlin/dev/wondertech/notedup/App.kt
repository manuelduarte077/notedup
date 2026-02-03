package dev.wondertech.notedup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import cafe.adriel.voyager.navigator.Navigator
import dev.wondertech.notedup.database.ProvideDatabaseHelper
import dev.wondertech.notedup.preferences.AppSettings
import dev.wondertech.notedup.preferences.ThemeMode
import dev.wondertech.notedup.preferences.getPreferencesManager
import dev.wondertech.notedup.screens.RootNavigationScreen
import dev.wondertech.notedup.utils.SetupSystemBars

@Composable
fun App() {
    val preferencesManager = remember { getPreferencesManager() }
    val settings by preferencesManager.settingsFlow.collectAsState(AppSettings())

    val darkTheme = settings.themeMode == ThemeMode.DARK

    SetupSystemBars(darkTheme = darkTheme)

    NotedUpAppTheme(themeMode = settings.themeMode) {
        ProvideDatabaseHelper {
            Navigator(screen = RootNavigationScreen())
        }
    }
}

