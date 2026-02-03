package dev.wondertech.notedup.notifications

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@Composable
actual fun rememberNotificationPermissionRequester(
    onPermissionResult: (Boolean) -> Unit
): () -> Unit {
    val notificationScheduler = rememberNotificationScheduler()
    val coroutineScope = rememberCoroutineScope()

    return remember {
        {
            coroutineScope.launch {
                try {
                    val isGranted = notificationScheduler.requestPermission()
                    onPermissionResult(isGranted)
                } catch (e: Exception) {
                    onPermissionResult(false)
                }
            }
        }
    }
}
