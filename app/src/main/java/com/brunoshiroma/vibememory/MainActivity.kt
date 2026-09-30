package com.brunoshiroma.vibememory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.brunoshiroma.vibememory.ui.BenchmarkScreen
import com.brunoshiroma.vibememory.ui.BenchmarkViewModel
import com.brunoshiroma.vibememory.ui.theme.VibeMemoryTheme

class MainActivity : ComponentActivity() {

    private val viewModel: BenchmarkViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VibeMemoryTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    BenchmarkScreen(viewModel)
                }
            }
        }
    }
}
