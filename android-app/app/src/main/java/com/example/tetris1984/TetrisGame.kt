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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Main Tetris Game Composable
 * 
 * This composable contains the complete Tetris game interface including:
 * - Game header with title and description
 * - Language selection and switching
 * - Game board rendering with character-based graphics
 * - Score display with high score
 * - Next piece and hold piece displays
 * - Sound controls and feedback system
 * - Control buttons
 * - Game state management
 */
@Composable
fun TetrisGame() {
    var showLanguageSelection by remember { mutableStateOf(true) }
    var selectedLanguage by remember { mutableStateOf("en") }
    var soundEnabled by remember { mutableStateOf(true) }
    var volume by remember { mutableStateOf(0.7f) }
    var soundOptions by remember { mutableStateOf(mapOf("move" to true, "rotate" to true, "drop" to true, "line" to true)) }
    var feedbackCounts by remember { mutableStateOf(mapOf("like" to 0, "dislike" to 0)) }
    val gameState = remember { TetrisGameState() }
    val scope = rememberCoroutineScope()
    
    // Load saved preferences
    LaunchedEffect(Unit) {
        val savedLang = gameState.getSavedLanguage()
        if (savedLang != null) {
            selectedLanguage = savedLang
            showLanguageSelection = false
        }
        
        soundEnabled = gameState.getSavedSoundEnabled()
        volume = gameState.getSavedVolume()
        soundOptions = gameState.getSavedSoundOptions()
        feedbackCounts = gameState.getSavedFeedbackCounts()
    }
    
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
                gameState.saveLanguage(language)
            }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Game Header
            GameHeader(selectedLanguage)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Language Switcher
            LanguageSwitcher(
                currentLanguage = selectedLanguage,
                onLanguageChanged = { language ->
                    selectedLanguage = language
                    gameState.saveLanguage(language)
                }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Main Game Layout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Left Panel - Game Board
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Score and game info
                    GameInfoPanel(gameState, selectedLanguage)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Game board
                    GameBoard(gameState)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Control buttons
                    ControlButtons(gameState, selectedLanguage)
                }
                
                // Right Panel - Next/Hold Pieces and Controls
                Column(
                    modifier = Modifier.width(200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Next and Hold Pieces Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Next Piece
                        NextPieceDisplay(gameState, selectedLanguage)
                        
                        // Hold Piece
                        HoldPieceDisplay(gameState, selectedLanguage)
                    }
                    
                    // Sound Controls
                    SoundControls(
                        soundEnabled = soundEnabled,
                        volume = volume,
                        soundOptions = soundOptions,
                        onSoundToggled = { enabled ->
                            soundEnabled = enabled
                            gameState.saveSoundEnabled(enabled)
                        },
                        onVolumeChanged = { newVolume ->
                            volume = newVolume
                            gameState.saveVolume(newVolume)
                        },
                        onSoundOptionToggled = { option, enabled ->
                            soundOptions = soundOptions.toMutableMap().apply { put(option, enabled) }
                            gameState.saveSoundOptions(soundOptions)
                        },
                        language = selectedLanguage
                    )
                    
                    // Feedback System
                    FeedbackSystem(
                        feedbackCounts = feedbackCounts,
                        onFeedbackSubmitted = { type ->
                            feedbackCounts = feedbackCounts.toMutableMap().apply {
                                put(type, getOrDefault(type, 0) + 1)
                            }
                            gameState.saveFeedbackCounts(feedbackCounts)
                        },
                        language = selectedLanguage
                    )
                }
            }
        }
    }
}

/**
 * Game Header with Title and Description
 */
@Composable
fun GameHeader(language: String) {
    val title = if (language == "ru") "Тетрис 1984" else "Tetris 1984"
    val description = if (language == "ru") {
        "Классический Тетрис 1984 года - чистая игра без лишних элементов"
    } else {
        "Classic 1984 Tetris - pure gameplay without distractions"
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF001100)
        ),
        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF00FF00))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = Color(0xFF00FF00),
                fontSize = 24.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                color = Color(0xFF00FF00),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                opacity = 0.8f
            )
        }
    }
}

/**
 * Language Switcher
 */
@Composable
fun LanguageSwitcher(
    currentLanguage: String,
    onLanguageChanged: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF001100)
        ),
        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF00FF00))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (currentLanguage == "ru") "Язык / Language" else "Language / Язык",
                color = Color(0xFF00FF00),
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onLanguageChanged("en") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentLanguage == "en") Color(0xFF00FF00) else Color(0xFF001100),
                        contentColor = if (currentLanguage == "en") Color.Black else Color(0xFF00FF00)
                    ),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF00FF00))
                ) {
                    Text(
                        text = "EN",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Button(
                    onClick = { onLanguageChanged("ru") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentLanguage == "ru") Color(0xFF00FF00) else Color(0xFF001100),
                        contentColor = if (currentLanguage == "ru") Color.Black else Color(0xFF00FF00)
                    ),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF00FF00))
                ) {
                    Text(
                        text = "RU",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
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
 * Displays score, lines, level, and high score in selected language
 */
@Composable
fun GameInfoPanel(gameState: TetrisGameState, language: String) {
    val strings = if (language == "ru") {
        mapOf(
            "lines" to "ПОЛНЫХ СТРОК: ",
            "level" to "УРОВЕНЬ: ",
            "score" to "СЧЕТ: ",
            "highScore" to "РЕКОРД: "
        )
    } else {
        mapOf(
            "lines" to "FULL LINES: ",
            "level" to "LEVEL: ",
            "score" to "SCORE: ",
            "highScore" to "HIGH SCORE: "
        )
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
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
            Text(
                text = strings["highScore"] + gameState.highScore.toString(),
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
            "reset" to "СБРОСИТЬ",
            "hold" to "ДЕРЖАТЬ"
        )
    } else {
        mapOf(
            "pause" to "PAUSE",
            "resume" to "RESUME",
            "reset" to "RESET",
            "hold" to "HOLD"
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
        
        // Middle row: Down, Hold, Pause
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GameButton(
                text = "↓",
                onClick = { gameState.moveDown() }
            )
            GameButton(
                text = strings["hold"]!!,
                onClick = { gameState.holdPiece() }
            )
            GameButton(
                text = if (gameState.isPaused) strings["resume"]!! else strings["pause"]!!,
                onClick = { gameState.togglePause() }
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Bottom row: Reset
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GameButton(
                text = strings["reset"]!!,
                onClick = { gameState.reset() },
                modifier = Modifier.width(120.dp)
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier.size(80.dp, 50.dp)
) {
    Button(
        onClick = onClick,
        modifier = modifier.clip(RoundedCornerShape(8.dp)),
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

/**
 * Next Piece Display
 */
@Composable
fun NextPieceDisplay(gameState: TetrisGameState, language: String) {
    val title = if (language == "ru") "СЛЕДУЮЩАЯ" else "NEXT"
    
    Card(
        modifier = Modifier
            .weight(1f)
            .height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF001100)
        ),
        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF00FF00))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                color = Color(0xFF00FF00),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            // Next piece preview
            gameState.nextPiece?.let { piece ->
                val pieceText = buildAnnotatedString {
                    for (y in piece.shape.indices) {
                        for (x in piece.shape[y].indices) {
                            if (piece.shape[y][x]) {
                                append("[]")
                            } else {
                                append("  ")
                            }
                        }
                        if (y < piece.shape.size - 1) append("\n")
                    }
                }
                
                Text(
                    text = pieceText,
                    color = Color(0xFF00FF00),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 8.sp,
                    lineHeight = 10.sp
                )
            } ?: run {
                Text(
                    text = "?",
                    color = Color(0xFF00FF00),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp
                )
            }
        }
    }
}

/**
 * Hold Piece Display
 */
@Composable
fun HoldPieceDisplay(gameState: TetrisGameState, language: String) {
    val title = if (language == "ru") "ДЕРЖАТЬ" else "HOLD"
    val hint = if (language == "ru") "нажмите c" else "press c"
    
    Card(
        modifier = Modifier
            .weight(1f)
            .height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF001100)
        ),
        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF00FF00))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                color = Color(0xFF00FF00),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            // Hold piece preview
            gameState.heldPiece?.let { piece ->
                val pieceText = buildAnnotatedString {
                    for (y in piece.shape.indices) {
                        for (x in piece.shape[y].indices) {
                            if (piece.shape[y][x]) {
                                append("[]")
                            } else {
                                append("  ")
                            }
                        }
                        if (y < piece.shape.size - 1) append("\n")
                    }
                }
                
                Text(
                    text = pieceText,
                    color = Color(0xFF00FF00),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 8.sp,
                    lineHeight = 10.sp
                )
            } ?: run {
                Text(
                    text = "-",
                    color = Color(0xFF00FF00),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp
                )
            }
            
            Text(
                text = hint,
                color = Color(0xFF00FF00),
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                opacity = 0.6f,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Sound Controls
 */
@Composable
fun SoundControls(
    soundEnabled: Boolean,
    volume: Float,
    soundOptions: Map<String, Boolean>,
    onSoundToggled: (Boolean) -> Unit,
    onVolumeChanged: (Float) -> Unit,
    onSoundOptionToggled: (String, Boolean) -> Unit,
    language: String
) {
    val title = if (language == "ru") "Звук" else "Sound"
    val volumeLabel = if (language == "ru") "Громкость" else "Volume"
    val moveLabel = if (language == "ru") "Движение" else "Move"
    val rotateLabel = if (language == "ru") "Поворот" else "Rotate"
    val dropLabel = if (language == "ru") "Падение" else "Drop"
    val lineLabel = if (language == "ru") "Линии" else "Lines"
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF001100)
        ),
        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF00FF00))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Title and master toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = Color(0xFF00FF00),
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                
                Button(
                    onClick = { onSoundToggled(!soundEnabled) },
                    modifier = Modifier.size(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (soundEnabled) Color(0xFF00FF00) else Color(0xFF001100),
                        contentColor = if (soundEnabled) Color.Black else Color(0xFF00FF00)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00FF00))
                ) {
                    Icon(
                        imageVector = if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = if (soundEnabled) "Mute" else "Unmute",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            // Volume slider
            if (soundEnabled) {
                Column {
                    Text(
                        text = volumeLabel,
                        color = Color(0xFF00FF00),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Slider(
                        value = volume,
                        onValueChange = onVolumeChanged,
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF00FF00),
                            activeTrackColor = Color(0xFF00FF00),
                            inactiveTrackColor = Color(0xFF003300)
                        )
                    )
                }
                
                // Individual sound options
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SoundOptionCheckbox(
                        label = moveLabel,
                        checked = soundOptions["move"] ?: true,
                        onCheckedChange = { onSoundOptionToggled("move", it) }
                    )
                    SoundOptionCheckbox(
                        label = rotateLabel,
                        checked = soundOptions["rotate"] ?: true,
                        onCheckedChange = { onSoundOptionToggled("rotate", it) }
                    )
                    SoundOptionCheckbox(
                        label = dropLabel,
                        checked = soundOptions["drop"] ?: true,
                        onCheckedChange = { onSoundOptionToggled("drop", it) }
                    )
                    SoundOptionCheckbox(
                        label = lineLabel,
                        checked = soundOptions["line"] ?: true,
                        onCheckedChange = { onSoundOptionToggled("line", it) }
                    )
                }
            }
        }
    }
}

/**
 * Sound Option Checkbox
 */
@Composable
fun SoundOptionCheckbox(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = Color(0xFF00FF00),
                uncheckedColor = Color(0xFF00FF00),
                checkmarkColor = Color.Black
            )
        )
        Text(
            text = label,
            color = Color(0xFF00FF00),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

/**
 * Feedback System
 */
@Composable
fun FeedbackSystem(
    feedbackCounts: Map<String, Int>,
    onFeedbackSubmitted: (String) -> Unit,
    language: String
) {
    val title = if (language == "ru") "Обратная связь" else "Feedback"
    val likeText = if (language == "ru") "Нравится" else "Like"
    val dislikeText = if (language == "ru") "Не нравится" else "Dislike"
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF001100)
        ),
        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF00FF00))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                color = Color(0xFF00FF00),
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            
            // Feedback buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onFeedbackSubmitted("like") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF001100),
                        contentColor = Color(0xFF00FF00)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00FF00))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ThumbUp,
                            contentDescription = "Like",
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${feedbackCounts["like"] ?: 0}",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                
                Button(
                    onClick = { onFeedbackSubmitted("dislike") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF001100),
                        contentColor = Color(0xFF00FF00)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00FF00))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ThumbDown,
                            contentDescription = "Dislike",
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${feedbackCounts["dislike"] ?: 0}",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
} 