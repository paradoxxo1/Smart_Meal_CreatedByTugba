package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.GameplayScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.LevelSelectScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val gameViewModel: GameViewModel = viewModel()
            val currentScreen by gameViewModel.currentScreen.collectAsState()
            val progressList by gameViewModel.allProgress.collectAsState()
            val isDarkTheme = gameViewModel.isDarkTheme

            MyApplicationTheme(darkTheme = isDarkTheme) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AnimatedContent(
                        targetState = currentScreen,
                        label = "screen_transition"
                    ) { screen ->
                        when (screen) {
                            is AppScreen.Home -> {
                                HomeScreen(
                                    viewModel = gameViewModel,
                                    progressList = progressList,
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                            is AppScreen.LevelSelect -> {
                                LevelSelectScreen(
                                    viewModel = gameViewModel,
                                    progressList = progressList,
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                            is AppScreen.Gameplay -> {
                                GameplayScreen(
                                    viewModel = gameViewModel,
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                            is AppScreen.Stats -> {
                                StatsScreen(
                                    viewModel = gameViewModel,
                                    progressList = progressList,
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                            is AppScreen.Leaderboard -> {
                                LeaderboardScreen(
                                    viewModel = gameViewModel,
                                    progressList = progressList,
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
