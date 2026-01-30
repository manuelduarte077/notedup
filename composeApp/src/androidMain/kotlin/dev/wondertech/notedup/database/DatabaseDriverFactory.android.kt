package dev.wondertech.notedup.database

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

/**
 * Android implementation of database driver factory.
 *
 * @param context Android application context for database access
 */
actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = NotedUpDatabase.Schema,
            context = context,
            name = "noteup.db",
            callback = object : AndroidSqliteDriver.Callback(NotedUpDatabase.Schema) {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    // Check if isTaskDone column exists, if not add it
                    try {
                        db.query("SELECT isTaskDone FROM Task LIMIT 1").use { cursor ->
                            cursor.moveToFirst()
                        }
                    } catch (_: Exception) {
                        // Column doesn't exist, add it
                        db.execSQL("ALTER TABLE Task ADD COLUMN isTaskDone INTEGER NOT NULL DEFAULT 0")
                    }

                    // Check if isMeeting column exists, if not add it
                    try {
                        db.query("SELECT isMeeting FROM Task LIMIT 1").use { cursor ->
                            cursor.moveToFirst()
                        }
                    } catch (_: Exception) {
                        // Column doesn't exist, add it
                        db.execSQL("ALTER TABLE Task ADD COLUMN isMeeting INTEGER NOT NULL DEFAULT 0")
                    }

                    // Check if meetingLink column exists, if not add it
                    try {
                        db.query("SELECT meetingLink FROM Task LIMIT 1").use { cursor ->
                            cursor.moveToFirst()
                        }
                    } catch (_: Exception) {
                        // Column doesn't exist, add it
                        db.execSQL("ALTER TABLE Task ADD COLUMN meetingLink TEXT NOT NULL DEFAULT ''")
                    }
                }
            }
        )
    }
}
