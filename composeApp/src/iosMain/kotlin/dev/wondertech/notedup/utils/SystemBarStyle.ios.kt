package dev.wondertech.notedup.utils

import androidx.compose.runtime.Composable

/**
 * No-op implementation for iOS.
 *
 * iOS status bar styling is typically handled through:
 * - Info.plist configuration
 * - UIViewController.preferredStatusBarStyle
 * - UIUserInterfaceStyle for dark mode support
 *
 * @param darkTheme Whether the app is currently in dark theme mode (unused on iOS)
 */
@Composable
actual fun SetupSystemBars(darkTheme: Boolean) {
    // No-op for iOS - status bar styling is handled differently
}
