package dev.wondertech.notedup.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.wondertech.notedup.common.NotedUpTopAppBar
import dev.wondertech.notedup.database.LocalDatabase
import dev.wondertech.notedup.notifications.rememberNotificationScheduler
import dev.wondertech.notedup.preferences.AppSettings
import dev.wondertech.notedup.preferences.ThemeMode
import dev.wondertech.notedup.preferences.getPreferencesManager
import dev.wondertech.notedup.primaryColorVariant
import kotlinx.coroutines.launch

class SettingsScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val preferencesManager = remember { getPreferencesManager() }
        val settings by preferencesManager.settingsFlow.collectAsState(AppSettings())
        val coroutineScope = rememberCoroutineScope()
        val notificationScheduler = rememberNotificationScheduler()
        val databaseHelper = LocalDatabase.current

        Scaffold { _ ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                NotedUpTopAppBar(
                    title = "Settings",
                    canShowNavigationIcon = true,
                    onBackButtonClick = {
                        navigator.pop()
                    }
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            coroutineScope.launch {
                                val newTheme = if (settings.themeMode == ThemeMode.LIGHT) {
                                    ThemeMode.DARK
                                } else {
                                    ThemeMode.LIGHT
                                }
                                preferencesManager.updateThemeMode(newTheme)
                            }
                        },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Theme",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Current: ${settings.themeMode.displayName}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }

                        Text(
                            text = if (settings.themeMode == ThemeMode.LIGHT) "🌞" else "🌙",
                            fontSize = 32.sp
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Task Notifications",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Enable reminders for tasks (meetings always notify)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                lineHeight = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Switch(
                            checked = settings.notificationsEnabled,
                            onCheckedChange = { enabled ->
                                coroutineScope.launch {
                                    preferencesManager.updateNotificationsEnabled(enabled)
                                    if (!enabled) {
                                        val allTasks = databaseHelper.getAllTasks()
                                        allTasks.forEach { task ->
                                            if (!task.isMeeting && !task.isDone) {
                                                notificationScheduler.cancelNotification(task.timestampMillis)
                                            }
                                        }
                                        println("SettingsScreen: Cancelled all non-meeting notifications")
                                    }
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.background,
                                checkedTrackColor = primaryColorVariant,
                                uncheckedThumbColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                uncheckedTrackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                            )
                        )
                    }
                }

                Text(
                    text = "Note: Meeting tasks will always send notifications 15 minutes before, regardless of this setting.",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}
