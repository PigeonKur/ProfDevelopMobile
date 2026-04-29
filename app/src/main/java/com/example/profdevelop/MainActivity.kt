package com.example.profdevelop

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.profdevelop.data.local.SettingsPreferencesDataSource
import com.example.profdevelop.notifications.StreakReminderWorker
import com.example.profdevelop.presentation.navigation.ProfDevelopNavHost
import com.example.profdevelop.presentation.theme.ProfDevelopTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* ignored */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProfDevelopTheme {
                ProfDevelopNavHost()
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        lifecycleScope.launch {
            val settings = SettingsPreferencesDataSource(applicationContext).flow.first()
            if (settings.dailyReminderEnabled) {
                StreakReminderWorker.schedule(applicationContext, settings.dailyReminderHour)
            } else {
                StreakReminderWorker.cancel(applicationContext)
            }
        }
    }
}
