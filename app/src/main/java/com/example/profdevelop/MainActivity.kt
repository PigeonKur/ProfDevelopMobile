package com.example.profdevelop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.profdevelop.presentation.navigation.ProfDevelopNavHost
import com.example.profdevelop.presentation.theme.ProfDevelopTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProfDevelopTheme {
                ProfDevelopNavHost()
            }
        }
    }
}
