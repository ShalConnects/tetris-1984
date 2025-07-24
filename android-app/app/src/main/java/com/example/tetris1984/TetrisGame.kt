package com.example.tetris1984

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle

/**
 * Main Tetris Game Composable
 * 
 * This composable contains the complete Tetris game interface including:
 * - Language selection
 * - Game board rendering with character-based graphics
 * - Score display
 * - Control buttons
 * - Game state management
 */
@Composable
fun TetrisGame() {
    var showLanguageSelection by remember { mutableStateOf(true) }
    var selectedLanguage by remember { mutableStateOf("en") }
    val gameState = remember { TetrisGameState() }
    val scope = rememberCoroutineScope()
    
    // Game loop using LaunchedEffect
    LaunchedEffect(Unit) {
        while (true) {
            if (!gameState.isPaused && !gameState.isGameOver && !showLanguageSelection) {
                gameState.update()
            }
            delay(1000) // 1 second tick (original 1984 timing)
        }
    }
    
    if (showLanguageSelection) {
        LanguageSelectionScreen(
            onLanguageSelected = { language ->
                selectedLanguage = language
                showLanguageSelection = false
            }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Score and game info
            GameInfoPanel(gameState, selectedLanguage)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Game board
            GameBoard(gameState)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Control buttons
            ControlButtons(gameState, selectedLanguage)
        }
    }
}

/**
 * Language Selection Screen
 */
@Composable
fun LanguageSelectionScreen(onLanguageSelected: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .width(300.dp)
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF001100)
            ),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF00FF00))
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Выберите язык / Choose Language",
                    color = Color(0xFF00FF00),
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                Button(
                    onClick = { onLanguageSelected("ru") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF001100),
                        contentColor = Color(0xFF00FF00)
                    ),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF00FF00))
                ) {
                    Text(
                        text = "Русский",
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Button(
                    onClick = { onLanguageSelected("en") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF001100),
                        contentColor = Color(0xFF00FF00)
                    ),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF00FF00))
                ) {
                    Text(
                        text = "English",
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Game Information Panel
 * Displays score, lines, and level in selected language
 */
@Composable
fun GameInfoPanel(gameState: TetrisGameState, language: String) {
    val strings = if (language == "ru") {
        mapOf(
            "lines" to "ПОЛНЫХ СТРОК: ",
            "level" to "УРОВЕНЬ: ",
            "score" to "СЧЕТ: "
        )
    } else {
        mapOf(
            "lines" to "FULL LINES: ",
            "level" to "LEVEL: ",
            "score" to "SCORE: "
        )
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF001100)
        ),
        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF00FF00))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = strings["lines"] + gameState.lines.toString(),
                color = Color(0xFF00FF00),
                fontSize = 16.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = strings["level"] + gameState.level.toString(),
                color = Color(0xFF00FF00),
                fontSize = 16.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = strings["score"] + gameState.score.toString(),
                color = Color(0xFF00FF00),
                fontSize = 16.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Game Board Canvas
 * Renders the 10x20 Tetris grid as ASCII art
 */
@Composable
fun GameBoard(gameState: TetrisGameState) {
    val blockSize = 20.dp
    
    Box(
        modifier = Modifier
            .size(width = blockSize * 10, height = blockSize * 20)
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black
            )
        ) {
            val asciiBoard = remember(gameState.board, gameState.currentPiece) {
                buildAnnotatedString {
                    for (y in 0 until 20) {
                        append("<1")
                        for (x in 0 until 10) {
                            val filled = gameState.board[y][x]
                            val isPiece = gameState.currentPiece?.let { piece ->
                                !gameState.isGameOver && piece.shape.indices.any { py ->
                                    piece.shape[py].indices.any { px ->
                                        piece.shape[py][px] &&
                                        piece.x + px == x &&
                                        piece.y + py == y
                                    }
                                }
                            } ?: false
                            
                            if (filled == 2) { // Flashing cells
                                pushStyle(SpanStyle(color = Color(0xFFFF0000))) // Red for flashing effect to make it more visible
                                append("[]")
                                pop()
                            } else if (filled == 1 || isPiece) {
                                append("[]")
                            } else {
                                append(" ") // Space at full opacity
                                pushStyle(SpanStyle(color = Color(0x9900FF00))) // 60% opacity green for dot
                                append(".")
                                pop()
                            }
                        }
                        append("1>\n")
                    }
                    // Bottom wall
                    append("<1" + "=".repeat(20) + "1>\n")
                    // Base
                    append("^".repeat(24) + "\n")
                }
            }
            
            Text(
                text = asciiBoard,
                color = Color(0xFF00FF00),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                lineHeight = 14.sp,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            )
        }
        
        // Game Over Overlay
        if (gameState.isGameOver) {
            GameOverOverlay(gameState.restartCountdown)
        }
    }
}

/**
 * Game Over Overlay
 */
@Composable
fun GameOverOverlay(countdown: Int) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x80000000)), // Semi-transparent background
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF001100)
            ),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF00FF00))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "GAME OVER!",
                    color = Color(0xFF00FF00),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Restarting in ${countdown}s",
                    color = Color(0xFF00FF00),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    opacity = 0.8f
                )
            }
        }
    }
}

/**
 * Control Buttons
 * Provides touch controls for the game
 */
@Composable
fun ControlButtons(gameState: TetrisGameState, language: String) {
    val strings = if (language == "ru") {
        mapOf(
            "pause" to "ПАУЗА",
            "resume" to "ПРОДОЛЖИТЬ",
            "reset" to "СБРОСИТЬ"
        )
    } else {
        mapOf(
            "pause" to "PAUSE",
            "resume" to "RESUME",
            "reset" to "RESET"
        )
    }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top row: Left, Rotate, Right
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GameButton(
                text = "←",
                onClick = { gameState.moveLeft() }
            )
            GameButton(
                text = "↻",
                onClick = { gameState.rotate() }
            )
            GameButton(
                text = "→",
                onClick = { gameState.moveRight() }
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Bottom row: Down, Pause, Reset
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GameButton(
                text = "↓",
                onClick = { gameState.moveDown() }
            )
            GameButton(
                text = if (gameState.isPaused) strings["resume"]!! else strings["pause"]!!,
                onClick = { gameState.togglePause() }
            )
            GameButton(
                text = strings["reset"]!!,
                onClick = { gameState.reset() }
            )
        }
    }
}

/**
 * Individual Game Button
 * Styled button for game controls
 */
@Composable
fun GameButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .size(80.dp, 50.dp)
            .clip(RoundedCornerShape(8.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF001100),
            contentColor = Color(0xFF00FF00)
        ),
        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF00FF00))
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
} 