package dev.wondertech.notedup.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val driver = NativeSqliteDriver(NotedUpDatabase.Schema, "notedup.db")

        try {
            driver.executeQuery(null, "SELECT isTaskDone FROM Task LIMIT 1", { cursor ->
                cursor.next()
            }, 0)
        } catch (_: Exception) {
            driver.execute(null, "ALTER TABLE Task ADD COLUMN isTaskDone INTEGER NOT NULL DEFAULT 0", 0)
        }

        return driver
    }
}
