package com.example.tetris1984

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.random.Random

/**
 * Tetris Game State
 * 
 * This class manages the complete game state and logic for the 1984 Tetris implementation.
 * It follows the original specifications:
 * - 10x20 grid
 * - 7 classic tetrominoes
 * - No wall-kicks
 * - Unbiased randomizer
 * - Simple scoring
 * - Line clearing animation
 */
class TetrisGameState {
    companion object {
        const val BOARD_WIDTH = 10
        const val BOARD_HEIGHT = 20
    }
    
    // Game state variables
    var board by mutableStateOf(Array(BOARD_HEIGHT) { Array(BOARD_WIDTH) { 0 } }) // 0=empty, 1=filled, 2=flashing
        private set
    
    var currentPiece by mutableStateOf<TetrisPiece?>(null)
        private set
    
    var nextPiece by mutableStateOf<TetrisPiece?>(null)
        private set
    
    var score by mutableStateOf(0)
        private set
    
    var lines by mutableStateOf(0)
        private set
    
    var level by mutableStateOf(1)
        private set
    
    var isGameOver by mutableStateOf(false)
        private set
    
    var isPaused by mutableStateOf(false)
        private set
    
    // Line clearing animation
    private var flashTime = 0L
    private val flashInterval = 100L // Flash every 100ms
    private var flashCount = 0
    private val maxFlashes = 4 // Flash 4 times before clearing
    private var linesToClear = mutableListOf<Int>()
    
    // Game over and restart
    private var gameOverTime = 0L
    private val restartDelay = 10000L // 10 seconds
    var restartCountdown by mutableStateOf(10)
        private set
    
    // Piece definitions (original 1984 shapes)
    private val pieces = mapOf(
        'I' to arrayOf(
            booleanArrayOf(true, true, true, true)
        ),
        'O' to arrayOf(
            booleanArrayOf(true, true),
            booleanArrayOf(true, true)
        ),
        'T' to arrayOf(
            booleanArrayOf(false, true, false),
            booleanArrayOf(true, true, true)
        ),
        'S' to arrayOf(
            booleanArrayOf(false, true, true),
            booleanArrayOf(true, true, false)
        ),
        'Z' to arrayOf(
            booleanArrayOf(true, true, false),
            booleanArrayOf(false, true, true)
        ),
        'J' to arrayOf(
            booleanArrayOf(true, false, false),
            booleanArrayOf(true, true, true)
        ),
        'L' to arrayOf(
            booleanArrayOf(false, false, true),
            booleanArrayOf(true, true, true)
        )
    )
    
    private val pieceTypes = pieces.keys.toList()
    
    init {
        reset()
    }
    
    /**
     * Handle game over countdown and restart
     */
    private fun handleGameOver() {
        if (!isGameOver) return
        
        val elapsed = System.currentTimeMillis() - gameOverTime
        val remaining = maxOf(0L, restartDelay - elapsed)
        restartCountdown = (remaining / 1000).toInt()
        
        if (remaining <= 0) {
            reset()
        }
    }
    
    /**
     * Reset the game to initial state
     */
    fun reset() {
        board = Array(BOARD_HEIGHT) { Array(BOARD_WIDTH) { 0 } }
        score = 0
        lines = 0
        level = 1
        isGameOver = false
        isPaused = false
        restartCountdown = 10
        linesToClear.clear()
        flashCount = 0
        spawnNewPiece()
    }
    
    /**
     * Toggle pause state
     */
    fun togglePause() {
        isPaused = !isPaused
    }
    
    /**
     * Move current piece left
     */
    fun moveLeft() {
        if (!isPaused && !isGameOver) {
            currentPiece?.let { piece ->
                if (isValidPosition(piece.x - 1, piece.y, piece.shape)) {
                    currentPiece = piece.copy(x = piece.x - 1)
                }
            }
        }
    }
    
    /**
     * Move current piece right
     */
    fun moveRight() {
        if (!isPaused && !isGameOver) {
            currentPiece?.let { piece ->
                if (isValidPosition(piece.x + 1, piece.y, piece.shape)) {
                    currentPiece = piece.copy(x = piece.x + 1)
                }
            }
        }
    }
    
    /**
     * Move current piece down
     */
    fun moveDown() {
        if (!isPaused && !isGameOver) {
            currentPiece?.let { piece ->
                if (isValidPosition(piece.x, piece.y + 1, piece.shape)) {
                    currentPiece = piece.copy(y = piece.y + 1)
                    score += 2 // Bonus for soft drop
                } else {
                    placePiece()
                    clearLines()
                    spawnNewPiece()
                }
            }
        }
    }
    
    /**
     * Rotate current piece
     */
    fun rotate() {
        if (!isPaused && !isGameOver) {
            currentPiece?.let { piece ->
                val rotatedShape = rotateShape(piece.shape)
                if (isValidPosition(piece.x, piece.y, rotatedShape)) {
                    currentPiece = piece.copy(shape = rotatedShape)
                }
            }
        }
    }
    
    /**
     * Update game state (called by game loop)
     */
    fun update() {
        if (isPaused) return
        
        // Handle game over countdown
        handleGameOver()
        
        if (isGameOver) return // Don't update game if game over
        
        currentPiece?.let { piece ->
            if (isValidPosition(piece.x, piece.y + 1, piece.shape)) {
                currentPiece = piece.copy(y = piece.y + 1)
            } else {
                placePiece()
                clearLines()
                spawnNewPiece()
            }
        }
        
        // Handle line clearing animation
        handleLineAnimation()
    }
    
    /**
     * Generate a random piece
     */
    private fun getRandomPiece(): TetrisPiece {
        val type = pieceTypes.random()
        val shape = pieces[type]!!
        
        return TetrisPiece(
            type = type,
            shape = shape.map { it.clone() }.toTypedArray(),
            x = BOARD_WIDTH / 2 - shape[0].size / 2,
            y = 0
        )
    }
    
    /**
     * Spawn a new piece
     */
    private fun spawnNewPiece() {
        currentPiece = nextPiece ?: getRandomPiece()
        nextPiece = getRandomPiece()
        
        // Check for game over
        if (!isValidPosition(currentPiece!!.x, currentPiece!!.y, currentPiece!!.shape)) {
            isGameOver = true
            gameOverTime = System.currentTimeMillis()
        }
    }
    
    /**
     * Check if a position is valid for a piece
     */
    private fun isValidPosition(x: Int, y: Int, shape: Array<BooleanArray>): Boolean {
        for (row in shape.indices) {
            for (col in shape[row].indices) {
                if (shape[row][col]) {
                    val boardX = x + col
                    val boardY = y + row
                    
                    // Check bounds
                    if (boardX < 0 || boardX >= BOARD_WIDTH || boardY >= BOARD_HEIGHT) {
                        return false
                    }
                    
                    // Check collision with placed pieces
                    if (boardY >= 0 && board[boardY][boardX] != 0) { // Changed to != 0 to check for filled or flashing
                        return false
                    }
                }
            }
        }
        return true
    }
    
    /**
     * Rotate a shape clockwise
     */
    private fun rotateShape(shape: Array<BooleanArray>): Array<BooleanArray> {
        val rows = shape.size
        val cols = shape[0].size
        val rotated = Array(cols) { BooleanArray(rows) }
        
        for (i in 0 until cols) {
            for (j in 0 until rows) {
                rotated[i][j] = shape[rows - 1 - j][i]
            }
        }
        
        return rotated
    }
    
    /**
     * Place current piece on the board
     */
    private fun placePiece() {
        currentPiece?.let { piece ->
            for (row in piece.shape.indices) {
                for (col in piece.shape[row].indices) {
                    if (piece.shape[row][col]) {
                        val boardX = piece.x + col
                        val boardY = piece.y + row
                        if (boardY >= 0) {
                            board[boardY][boardX] = 1 // Mark as filled
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Clear completed lines
     */
    private fun clearLines() {
        var linesCleared = 0
        
        for (y in BOARD_HEIGHT - 1 downTo 0) {
            if (board[y].all { it == 1 }) { // Check for filled lines
                linesToClear.add(y)
                linesCleared++
            }
        }
        
        if (linesCleared > 0) {
            // Start flashing animation
            flashTime = System.currentTimeMillis()
            flashCount = 0
            
            // Mark lines as flashing (value 2)
            for (y in linesToClear) {
                for (x in 0 until BOARD_WIDTH) {
                    board[y][x] = 2 // 2 = flashing
                }
            }
        }
    }
    
    /**
     * Handle line clearing animation
     */
    private fun handleLineAnimation() {
        if (linesToClear.isEmpty()) return
        
        val currentTime = System.currentTimeMillis()
        if (currentTime - flashTime >= flashInterval) {
            flashCount++
            flashTime = currentTime
            
            if (flashCount >= maxFlashes) {
                // Animation complete, clear the lines
                completeLineClear()
            } else {
                // Toggle flash state - make it more visible
                for (y in linesToClear) {
                    for (x in 0 until BOARD_WIDTH) {
                        // Alternate between visible and invisible
                        board[y][x] = if (flashCount % 2 == 0) 2 else 0
                    }
                }
            }
        }
    }
    
    /**
     * Complete the line clearing after animation
     */
    private fun completeLineClear() {
        // Remove the lines (sort in descending order to avoid index issues)
        for (y in linesToClear.sortedDescending()) {
            for (moveY in y downTo 1) {
                board[moveY] = board[moveY - 1].clone()
            }
            board[0] = Array(BOARD_WIDTH) { 0 }
        }
        
        // Update score
        lines += linesToClear.size
        score += linesToClear.size * 100 // Original 1984 scoring
        level = (lines / 10) + 1
        
        // Reset animation state
        linesToClear.clear()
        flashCount = 0
    }
}

/**
 * Data class representing a Tetris piece
 */
data class TetrisPiece(
    val type: Char,
    val shape: Array<BooleanArray>,
    val x: Int,
    val y: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TetrisPiece

        if (type != other.type) return false
        if (!shape.contentDeepEquals(other.shape)) return false
        if (x != other.x) return false
        if (y != other.y) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + shape.contentDeepHashCode()
        result = 31 * result + x
        result = 31 * result + y
        return result
    }
} 