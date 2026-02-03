package dev.wondertech.notedup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.wondertech.notedup.preferences.AndroidPreferencesManager


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        AndroidPreferencesManager.initialize(this)

        setContent {
            App()
        }
    }
}
