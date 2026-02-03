package dev.wondertech.notedup.database

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf

/**
 * CompositionLocal for providing database access throughout the app.
 * Throws an error if accessed before being provided.
 */
val LocalDatabase = compositionLocalOf<TaskDatabaseHelper> {
    error("No database provided")
}

/**
 * Expected composable function that provides the database helper to the composition.
 * Platform-specific implementations (Android/iOS) create the appropriate SQLDelight driver.
 *
 * @param content The composable content that will have access to the database
 */
@Composable
expect fun ProvideDatabaseHelper(content: @Composable () -> Unit)
