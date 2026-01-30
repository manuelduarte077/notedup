package dev.wondertech.notedup.notifications

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * Android implementation of notification permission requester.
 * On Android 13+, requests POST_NOTIFICATIONS permission using ActivityResultLauncher.
 * On older versions, immediately returns true (permission not required).
 */
@Composable
actual fun rememberNotificationPermissionRequester(
    onPermissionResult: (Boolean) -> Unit
): () -> Unit {
    val context = LocalContext.current

    // Check if permission is required (Android 13+)
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        // On Android < 13, permission not required - return a no-op function that immediately succeeds
        return {
            println("PermissionHelper (Android): Android < 13, permission not required")
            onPermissionResult(true)
        }
    }

    // Create permission launcher for Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        println("PermissionHelper (Android): Permission result - granted = $isGranted")
        onPermissionResult(isGranted)
    }

    return {
        val isAlreadyGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (isAlreadyGranted) {
            println("PermissionHelper (Android): Permission already granted")
            onPermissionResult(true)
        } else {
            println("PermissionHelper (Android): Requesting POST_NOTIFICATIONS permission")
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
