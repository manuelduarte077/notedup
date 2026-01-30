package dev.wondertech.notedup.preferences

enum class ThemeMode(val displayName: String) {
    LIGHT("Light"),
    DARK("Dark")
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.LIGHT,
    val notificationsEnabled: Boolean = true
)