package com.example.tetris1984

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.tetris1984.ui.theme.Tetris1984Theme

/**
 * Main Activity for the 1984 Tetris Android App
 * 
 * This activity hosts the main Tetris game using Jetpack Compose.
 * The game follows the original 1984 specifications with monochrome green styling.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Tetris1984Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TetrisGameWithContext()
                }
            }
        }
    }
}

/**
 * TetrisGame with context for sound initialization
 */
@Composable
fun TetrisGameWithContext() {
    val context = LocalContext.current
    val gameState = remember { TetrisGameState() }
    
    // Initialize sound system
    LaunchedEffect(Unit) {
        gameState.initSound(context)
    }
    
    TetrisGame()
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    Tetris1984Theme {
        TetrisGame()
    }
} 