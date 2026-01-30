package dev.wondertech.notedup.notifications

import androidx.compose.runtime.Composable

@Composable
expect fun rememberNotificationPermissionRequester(
    onPermissionResult: (Boolean) -> Unit
): () -> Unit
