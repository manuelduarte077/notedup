package dev.wondertech.notedup.database

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember

/**
 * iOS implementation of database provider.
 * Creates the SQLDelight native driver for iOS platform without requiring a context.
 *
 * @param content The composable content that will have access to the database
 */
@Composable
actual fun ProvideDatabaseHelper(content: @Composable () -> Unit) {
    val databaseHelper = remember {
        TaskDatabaseHelper(DatabaseDriverFactory().createDriver())
    }

    CompositionLocalProvider(LocalDatabase provides databaseHelper) {
        content()
    }
}
