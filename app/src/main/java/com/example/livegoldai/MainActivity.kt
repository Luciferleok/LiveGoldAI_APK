package com.example.livegoldai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.livegoldai.theme.LiveGoldAITheme
import com.example.livegoldai.ui.GoldHomeScreen
import com.example.livegoldai.ui.GoldViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: GoldViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LiveGoldAITheme {
                GoldHomeScreen(viewModel = viewModel)
            }
        }
    }
}
