package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.ui.components.NovaBottomNav
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.ToolsScreen
import com.example.ui.theme.NovaTheme
import com.example.ui.viewmodel.NovaTab
import com.example.ui.viewmodel.NovaViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: NovaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            var showSplash by remember { mutableStateOf(true) }

            NovaTheme(themeMode = uiState.themeMode) {
                if (showSplash) {
                    SplashScreen(
                        onSplashFinished = { showSplash = false }
                    )
                } else {
                    NovaMainScreen(
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun NovaMainScreen(
    viewModel: NovaViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    // Back handling: if not on HOME, pressing Back returns to HOME
    BackHandler(enabled = uiState.currentTab != NovaTab.HOME) {
        viewModel.switchTab(NovaTab.HOME)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NovaBottomNav(
                currentTab = uiState.currentTab,
                onTabSelected = { viewModel.switchTab(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Crossfade(
                targetState = uiState.currentTab,
                label = "screen_transition"
            ) { tab ->
                when (tab) {
                    NovaTab.HOME -> HomeScreen(
                        viewModel = viewModel,
                        uiState = uiState
                    )
                    NovaTab.CHAT -> ChatScreen(
                        viewModel = viewModel,
                        uiState = uiState
                    )
                    NovaTab.TOOLS -> ToolsScreen(
                        viewModel = viewModel,
                        uiState = uiState
                    )
                    NovaTab.SETTINGS -> SettingsScreen(
                        viewModel = viewModel,
                        uiState = uiState
                    )
                }
            }
        }
    }
}
