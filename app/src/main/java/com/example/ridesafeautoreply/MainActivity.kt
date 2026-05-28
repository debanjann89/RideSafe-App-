package com.example.ridesafeautoreply

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ridesafeautoreply.data.SettingsRepository
import com.example.ridesafeautoreply.theme.RideSafeAutoReplyTheme

class MainActivity : ComponentActivity() {
  private lateinit var settingsRepository: SettingsRepository

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    settingsRepository = SettingsRepository(applicationContext)

    enableEdgeToEdge()
    setContent {
      val isDarkTheme by settingsRepository.isDarkThemeEnabled.collectAsState(initial = true)
      
      RideSafeAutoReplyTheme(darkTheme = isDarkTheme) { 
        Surface(
            modifier = Modifier.fillMaxSize(), 
            color = MaterialTheme.colorScheme.background
        ) { 
            MainNavigation() 
        } 
      }
    }
  }
}
