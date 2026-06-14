package dev.mjamalidev.tandemcommunity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import dev.mjamalidev.tandemcommunity.presentation.community.CommunityRoute
import dev.mjamalidev.tandemcommunity.ui.theme.CommunityTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CommunityTheme {
                CommunityRoute()
            }
        }
    }
}
