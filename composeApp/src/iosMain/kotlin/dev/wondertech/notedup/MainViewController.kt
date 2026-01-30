package dev.wondertech.notedup

import androidx.compose.ui.window.ComposeUIViewController

/**
 * Creates the main UIViewController for the iOS app.
 *
 * This function is called from the iOS Swift/Objective-C code to create
 * the root view controller containing the Compose Multiplatform UI.
 *
 * @return ComposeUIViewController containing the App() composable
 */
fun MainViewController() = ComposeUIViewController { App() }