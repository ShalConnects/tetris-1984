/**
 * 1984 Tetris - Original Replica
 * JavaScript implementation replicating the original Elektronika 60 version
 * 
 * This implementation now renders the board as ASCII art using <pre> and <code> tags, with:
 * - <1 and 1> as vertical walls
 * - [] for blocks, spaces for empty
 * - <1======1> as the bottom wall
 * - ^^^^^^^^ as the base
 */

// ============================================================================
// LANGUAGE MANAGEMENT
// ============================================================================

let currentLanguage = 'en'; // Default language

// Language strings
const strings = {
    en: {
        fullLines: " Full lines: ",
        level: " Level: ",
        score: " Score: ",
        controls: {
            left: " Move left : ←/7",
            right: " Move right: →/9",
            rotate: " Rotate    : ↑/8",
            accelerate: " Soft drop : ↓/4",
            hardDrop: " Hard drop : SPACE",
            reset: " Reset game: R/5",
            hold: " Hold piece: C",
            pause: " Pause     : P",
            title: " 1984 TETRIS"
        },
        hints: {
            holdPiece: "press c to save"
        },
        androidTooltip: "Android App Available"
    },
    ru: {
        fullLines: " Полных строк: ",
        level: " Уровень: ",
        score: " Счет: ",
        controls: {
            left: "Move left : ←/7",
            right: "Move right: →/9",
            rotate: "Rotate    : ↑/8",
            accelerate: "Soft drop : ↓/4",
            hardDrop: "Hard drop : SPACE",
            reset: "Reset game: R/5",
            hold: "Hold piece: C",
            pause: "Pause     : P",
            title: "1984 TETRIS"
        },
        hints: {
            holdPiece: "нажмите c для сохранения"
        },
        androidTooltip: "Доступно приложение для Android"
    }
};

function selectLanguage(lang) {
    console.log('Language selected:', lang);
    currentLanguage = lang;
    localStorage.setItem('tetrisLanguage', lang);
    
    // Hide language modal
    const modal = document.getElementById('languageModal');
    if (modal) {
        modal.style.display = 'none';
        console.log('Language modal hidden');
    }
    
    // Update UI text
    updateLanguageUI();
    
    // Start the game immediately
    console.log('Starting game initialization...');
    setTimeout(() => {
        if (!window.tetrisController) {
            console.log('Initializing game...');
            initializeGame();
        } else {
            console.log('Game already initialized');
        }
    }, 100);
}

function updateLanguageUI() {
    const lang = strings[currentLanguage];
    
    // Update score lines using data attributes
    document.querySelectorAll('.score-line').forEach((element) => {
        const dataAttr = `data-${currentLanguage}`;
        if (element.hasAttribute(dataAttr)) {
            const spans = element.querySelectorAll('span');
            if (spans.length > 0) {
                const value = spans[0].textContent;
                const prefix = element.getAttribute(dataAttr).split(':')[0] + ': ';
                element.innerHTML = prefix + '<span id="' + spans[0].id + '">' + value + '</span>';
            }
        }
    });
    
    // Update control lines using data attributes
    document.querySelectorAll('.control-line').forEach((element) => {
        const dataAttr = `data-${currentLanguage}`;
        if (element.hasAttribute(dataAttr)) {
            element.textContent = element.getAttribute(dataAttr);
        }
    });
    
    // Update header elements
    const gameTitle = document.querySelector('.game-title');
    if (gameTitle) {
        const dataAttr = `data-${currentLanguage}`;
        if (gameTitle.hasAttribute(dataAttr)) {
            gameTitle.textContent = gameTitle.getAttribute(dataAttr);
        }
    }
    
    const gameDescription = document.querySelector('.game-description');
    if (gameDescription) {
        const dataAttr = `data-${currentLanguage}`;
        if (gameDescription.hasAttribute(dataAttr)) {
            gameDescription.textContent = gameDescription.getAttribute(dataAttr);
        }
    }
    
    // Update hint texts
    const holdPieceHint = document.querySelector('.hold-piece-hint');
    if (holdPieceHint) {
        holdPieceHint.textContent = lang.hints.holdPiece;
    }
    
    // Update Android tooltip
    const androidTooltip = document.querySelector('.android-tooltip');
    if (androidTooltip) {
        androidTooltip.textContent = lang.androidTooltip;
    }
}

// Check for saved language preference
function checkLanguagePreference() {
    const savedLang = localStorage.getItem('tetrisLanguage');
    if (savedLang) {
        currentLanguage = savedLang;
        document.getElementById('languageModal').style.display = 'none';
        updateLanguageUI();
        return true;
    }
    return false;
}

// ============================================================================
// MODEL - Game State and Logic
// ============================================================================

class TetrisModel {
    constructor() {
        // Game board: 10x20 grid (original Elektronika 60 dimensions)
        this.BOARD_WIDTH = 10;
        this.BOARD_HEIGHT = 20;
        
        // Initialize empty board (0 = empty, 1 = filled, 2 = flashing)
        this.board = Array(this.BOARD_HEIGHT).fill().map(() => Array(this.BOARD_WIDTH).fill(0));
        
        // Game state
        this.score = 0;
        this.lines = 0;
        this.level = 1;
        this.gameOver = false;
        this.isPaused = false;
        
        // Enhanced scoring system
        this.highScore = parseInt(localStorage.getItem('tetrisHighScore')) || 0;
        this.comboCount = 0;
        this.lastTetris = false; // For back-to-back Tetris bonus
        this.piecesPlaced = 0;
        
        // Time tracking
        this.gameStartTime = Date.now();
        this.totalPauseTime = 0;
        this.pauseStartTime = 0;
        
        // Current, next, and hold pieces
        this.currentPiece = null;
        this.nextPiece = null;
        this.holdPiece = null;
        this.canHold = true; // Can only hold once per piece
        
        // Ghost piece
        this.ghostPiece = null;
        
        // Timing
        this.dropTime = 0;
        this.dropInterval = 1000; // 1 second (original timing)
        
        // Line clearing animation
        this.flashTime = 0;
        this.flashInterval = 100; // Flash every 100ms
        this.flashCount = 0;
        this.maxFlashes = 4; // Flash 4 times before clearing
        this.linesToClear = []; // Rows that are being cleared
        
        // Game over and restart
        this.gameOverTime = 0;
        this.restartDelay = 5000; // 5 seconds
        this.restartCountdown = 5;
        
        // Initialize pieces
        this.initializePieces();
        this.spawnNewPiece();
    }
    
    /**
     * Initialize the 7 classic tetrominoes with their original shapes
     * Based on the original 1984 implementation
     */
    initializePieces() {
        this.pieces = {
            I: {
                shape: [
                    [1, 1, 1, 1]
                ],
                // I-piece: horizontal line of 4 blocks
            },
            O: {
                shape: [
                    [1, 1],
                    [1, 1]
                ],
                // O-piece: 2x2 square (no rotation needed)
            },
            T: {
                shape: [
                    [0, 1, 0],
                    [1, 1, 1]
                ],
                // T-piece: T-shape
            },
            S: {
                shape: [
                    [0, 1, 1],
                    [1, 1, 0]
                ],
                // S-piece: S-shape
            },
            Z: {
                shape: [
                    [1, 1, 0],
                    [0, 1, 1]
                ],
                // Z-piece: Z-shape
            },
            J: {
                shape: [
                    [1, 0, 0],
                    [1, 1, 1]
                ],
                // J-piece: J-shape
            },
            L: {
                shape: [
                    [0, 0, 1],
                    [1, 1, 1]
                ],
                // L-piece: L-shape
            }
        };
        
        this.pieceTypes = Object.keys(this.pieces);
    }
    
    /**
     * Unbiased randomizer - each piece has equal 1/7 chance
     * No history tracking (original 1984 behavior)
     */
    getRandomPiece() {
        const type = this.pieceTypes[Math.floor(Math.random() * this.pieceTypes.length)];
        const pieceData = this.pieces[type];
        
        return {
            type: type,
            shape: pieceData.shape.map(row => [...row]), // Deep copy
            x: Math.floor(this.BOARD_WIDTH / 2) - Math.floor(pieceData.shape[0].length / 2),
            y: 0
        };
    }
    
    /**
     * Spawn a new piece at the top center of the board
     */
    spawnNewPiece() {
        this.currentPiece = this.nextPiece || this.getRandomPiece();
        this.nextPiece = this.getRandomPiece();
        
        // Check for game over
        if (!this.isValidPosition(this.currentPiece, 0, 0)) {
            this.gameOver = true;
            this.gameOverTime = Date.now();
            console.log('Game Over! Restarting in 10 seconds...');
        }
    }
    
    /**
     * Check if a piece position is valid (within bounds and no collisions)
     */
    isValidPosition(piece, dx = 0, dy = 0) {
        for (let y = 0; y < piece.shape.length; y++) {
            for (let x = 0; x < piece.shape[y].length; x++) {
                if (piece.shape[y][x]) {
                    const newX = piece.x + x + dx;
                    const newY = piece.y + y + dy;
                    
                    // Check bounds
                    if (newX < 0 || newX >= this.BOARD_WIDTH || newY >= this.BOARD_HEIGHT) {
                        return false;
                    }
                    
                    // Check collision with placed pieces (only if within board)
                    if (newY >= 0 && this.board[newY][newX]) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    /**
     * Rotate piece clockwise (original 1984 rotation - no wall kicks)
     */
    rotatePiece(piece) {
        const rotated = [];
        const rows = piece.shape.length;
        const cols = piece.shape[0].length;
        
        // Create rotated shape
        for (let i = 0; i < cols; i++) {
            rotated[i] = [];
            for (let j = 0; j < rows; j++) {
                rotated[i][j] = piece.shape[rows - 1 - j][i];
            }
        }
        
        return {
            ...piece,
            shape: rotated
        };
    }
    
    /**
     * Move current piece by delta x, y
     */
    movePiece(dx, dy) {
        if (this.isValidPosition(this.currentPiece, dx, dy)) {
            this.currentPiece.x += dx;
            this.currentPiece.y += dy;
            return true;
        }
        return false;
    }
    
    /**
     * Rotate current piece with basic wall-kick attempts
     * Tries original position, then left, right, up, down shifts
     */
    rotateCurrentPiece() {
        const rotated = this.rotatePiece(this.currentPiece);
        
        // Try original position first
        if (this.isValidPosition(rotated)) {
            this.currentPiece = rotated;
            return;
        }
        
        // Try wall-kick positions (basic 1984-style attempts)
        const kickAttempts = [
            { dx: -1, dy: 0 },  // Try left
            { dx: 1, dy: 0 },   // Try right
            { dx: 0, dy: -1 },  // Try up
            { dx: -1, dy: -1 }, // Try up-left
            { dx: 1, dy: -1 },  // Try up-right
        ];
        
        for (let attempt of kickAttempts) {
            if (this.isValidPosition(rotated, attempt.dx, attempt.dy)) {
                this.currentPiece = rotated;
                this.currentPiece.x += attempt.dx;
                this.currentPiece.y += attempt.dy;
                return;
            }
        }
        
        // If no valid position found, rotation fails (original behavior)
    }
    
    /**
     * Place current piece on the board
     */
    placePiece() {
        for (let y = 0; y < this.currentPiece.shape.length; y++) {
            for (let x = 0; x < this.currentPiece.shape[y].length; x++) {
                if (this.currentPiece.shape[y][x]) {
                    const boardY = this.currentPiece.y + y;
                    const boardX = this.currentPiece.x + x;
                    if (boardY >= 0) {
                        this.board[boardY][boardX] = 1;
                    }
                }
            }
        }
        
        // Update statistics
        this.piecesPlaced++;
        this.canHold = true; // Reset hold ability for next piece
    }
    
    /**
     * Hold current piece (swap with hold piece)
     */
    holdCurrentPiece() {
        if (!this.canHold || !this.currentPiece) return;
        
        if (this.holdPiece) {
            // Swap pieces
            const temp = this.holdPiece;
            this.holdPiece = this.currentPiece;
            this.currentPiece = temp;
        } else {
            // First hold
            this.holdPiece = this.currentPiece;
            this.currentPiece = this.nextPiece;
            this.nextPiece = this.getRandomPiece();
        }
        
        // Reset piece position
        this.currentPiece.x = Math.floor(this.BOARD_WIDTH / 2) - Math.floor(this.currentPiece.shape[0].length / 2);
        this.currentPiece.y = 0;
        
        this.canHold = false; // Can only hold once per piece
    }
    
    /**
     * Update ghost piece position
     */
    updateGhostPiece() {
        if (!this.currentPiece) return;
        
        this.ghostPiece = {
            ...this.currentPiece,
            x: this.currentPiece.x,
            y: this.currentPiece.y
        };
        
        // Drop ghost piece to bottom
        while (this.isValidPosition(this.ghostPiece, 0, 1)) {
            this.ghostPiece.y++;
        }
    }
    
    /**
     * Clear completed lines and update score with enhanced scoring system
     */
    clearLines() {
        // Find lines to clear
        let linesToClear = [];
        for (let y = this.BOARD_HEIGHT - 1; y >= 0; y--) {
            if (this.board[y].every(cell => cell === 1)) {
                linesToClear.push(y);
            }
        }
        
        if (linesToClear.length > 0) {
            console.log('Starting line clear animation for lines:', linesToClear);
            
            // Enhanced scoring system
            this.updateScore(linesToClear.length);
            
            // Start flashing animation
            this.linesToClear = linesToClear;
            this.flashCount = 0;
            this.flashTime = 0;
            
            // Mark lines as flashing (value 2)
            for (let y of linesToClear) {
                for (let x = 0; x < this.BOARD_WIDTH; x++) {
                    this.board[y][x] = 2; // 2 = flashing
                }
            }
        } else {
            // No lines cleared, reset combo
            this.comboCount = 0;
        }
    }
    
    /**
     * Enhanced scoring system with line bonuses, combos, and Tetris bonuses
     */
    updateScore(linesCleared) {
        if (linesCleared === 0) return;
        
        // Base points for lines (classic scoring)
        const linePoints = [0, 40, 100, 300, 1200]; // 0, 1, 2, 3, 4 lines
        let points = linePoints[linesCleared] * this.level;
        
        // Tetris bonus (4 lines at once)
        if (linesCleared === 4) {
            if (this.lastTetris) {
                points += 1200 * this.level; // Back-to-back Tetris bonus
            }
            this.lastTetris = true;
        } else {
            this.lastTetris = false;
        }
        
        // Combo bonus
        if (this.comboCount > 0) {
            points += 50 * this.comboCount * this.level;
        }
        
        this.score += points;
        this.comboCount++;
        
        // Update high score
        if (this.score > this.highScore) {
            this.highScore = this.score;
            localStorage.setItem('tetrisHighScore', this.highScore.toString());
        }
        
        // Level progression (every 10 lines)
        const newLevel = Math.floor(this.lines / 10) + 1;
        if (newLevel > this.level) {
            this.level = newLevel;
            this.dropInterval = Math.max(50, 1000 - (this.level - 1) * 50); // Speed up with level
        }
    }
    
    /**
     * Handle line clearing animation
     */
    handleLineAnimation(deltaTime) {
        if (this.linesToClear.length === 0) return;
        
        this.flashTime += deltaTime;
        
        if (this.flashTime >= this.flashInterval) {
            this.flashCount++;
            this.flashTime = 0;
            
            console.log('Flash count:', this.flashCount, 'of', this.maxFlashes);
            
            if (this.flashCount >= this.maxFlashes) {
                // Animation complete, clear the lines
                console.log('Animation complete, clearing lines');
                this.completeLineClear();
            } else {
                // Toggle flash state - make it more visible
                for (let y of this.linesToClear) {
                    for (let x = 0; x < this.BOARD_WIDTH; x++) {
                        // Alternate between visible and invisible
                        this.board[y][x] = this.flashCount % 2 === 0 ? 2 : 0;
                    }
                }
                console.log('Toggled flash state, flash count:', this.flashCount);
            }
        }
    }
    
    /**
     * Complete the line clearing after animation
     */
    completeLineClear() {
        // Sort lines to clear from bottom to top to avoid index shifting issues
        const sortedLines = this.linesToClear.sort((a, b) => b - a);
        
        // Remove the lines using a more robust approach
        for (let y of sortedLines) {
            // Move all lines above this one down by one position
            for (let moveY = y; moveY > 0; moveY--) {
                this.board[moveY] = [...this.board[moveY - 1]];
            }
            // Set the top line to empty
            this.board[0] = Array(this.BOARD_WIDTH).fill(0);
        }
        
        // Update score
        this.lines += this.linesToClear.length;
        
        // Reset animation state
        this.linesToClear = [];
        this.flashCount = 0;
    }
    
    /**
     * Update game state (called each frame)
     */
    update(deltaTime) {
        if (this.isPaused) return;
        
        // Handle game over countdown
        this.handleGameOver(deltaTime);
        
        if (this.gameOver) return; // Don't update game if game over
        
        // Update ghost piece
        this.updateGhostPiece();
        
        this.dropTime += deltaTime;
        
        if (this.dropTime >= this.dropInterval) {
            if (!this.movePiece(0, 1)) {
                // Piece can't move down, place it
                this.placePiece();
                this.clearLines();
                this.spawnNewPiece();
            }
            this.dropTime = 0;
        }
        
        // Handle line clearing animation
        this.handleLineAnimation(deltaTime);
    }
    
    /**
     * Get current game time (excluding pause time)
     */
    getGameTime() {
        const currentTime = Date.now();
        const totalTime = currentTime - this.gameStartTime - this.totalPauseTime;
        return Math.floor(totalTime / 1000); // Return seconds
    }
    
    /**
     * Start pause timer
     */
    startPause() {
        if (!this.isPaused) {
            this.pauseStartTime = Date.now();
            this.isPaused = true;
        }
    }
    
    /**
     * End pause timer
     */
    endPause() {
        if (this.isPaused) {
            this.totalPauseTime += Date.now() - this.pauseStartTime;
            this.isPaused = false;
        }
    }
    
    /**
     * Handle game over countdown and restart
     */
    handleGameOver(deltaTime) {
        if (!this.gameOver) return;
        
        const elapsed = Date.now() - this.gameOverTime;
        const remaining = Math.max(0, this.restartDelay - elapsed);
        this.restartCountdown = Math.ceil(remaining / 1000);
        
        if (remaining <= 0) {
            console.log('Auto-restarting game...');
            this.reset();
        }
    }
    
    /**
     * Reset game to initial state
     */
    reset() {
        this.board = Array(this.BOARD_HEIGHT).fill().map(() => Array(this.BOARD_WIDTH).fill(0));
        this.score = 0;
        this.lines = 0;
        this.level = 1;
        this.gameOver = false;
        this.isPaused = false;
        this.dropTime = 0;
        this.restartCountdown = 10;
        this.linesToClear = [];
        this.flashCount = 0;
        
        // Reset enhanced features
        this.comboCount = 0;
        this.lastTetris = false;
        this.piecesPlaced = 0;
        this.holdPiece = null;
        this.canHold = true;
        this.ghostPiece = null;
        
        // Reset timing
        this.gameStartTime = Date.now();
        this.totalPauseTime = 0;
        this.pauseStartTime = 0;
        
        this.spawnNewPiece();
    }
}

// ============================================================================
// VIEW - Rendering and Display
// ============================================================================

class TetrisView {
    constructor() {
        this.boardElem = document.getElementById('asciiBoard');
        this.width = 10;
        this.height = 20;
    }

    /**
     * Render the complete game state as ASCII art
     */
    render(model) {
        let output = '';
        
        for (let y = 0; y < this.height; y++) {
            output += '<1';
            for (let x = 0; x < this.width; x++) {
                let filled = model.board[y][x];
                // Check if current piece occupies this cell
                let isPiece = false;
                let isGhost = false;
                
                if (model.currentPiece && !model.gameOver) {
                    for (let py = 0; py < model.currentPiece.shape.length; py++) {
                        for (let px = 0; px < model.currentPiece.shape[py].length; px++) {
                            if (
                                model.currentPiece.shape[py][px] &&
                                model.currentPiece.x + px === x &&
                                model.currentPiece.y + py === y
                            ) {
                                isPiece = true;
                            }
                        }
                    }
                }
                
                // Check if ghost piece occupies this cell
                if (model.ghostPiece && !model.gameOver) {
                    for (let py = 0; py < model.ghostPiece.shape.length; py++) {
                        for (let px = 0; px < model.ghostPiece.shape[py].length; px++) {
                            if (
                                model.ghostPiece.shape[py][px] &&
                                model.ghostPiece.x + px === x &&
                                model.ghostPiece.y + py === y
                            ) {
                                isGhost = true;
                            }
                        }
                    }
                }
                
                if (filled === 2) { // Flashing cells
                    output += '<span class="flashing-cell">[]</span>';
                } else if (filled || isPiece) {
                    output += '[]';
                } else if (isGhost) {
                    output += '<span class="ghost-piece">[]</span>';
                } else {
                    output += ' <span class="faded-dot">.</span>';
                }
            }
            output += '1>\n';
        }
        // Bottom wall
        output += '<1' + '='.repeat(this.width * 2) + '1>\n';
        // Base
        output += '^'.repeat(this.width * 2 + 4) + '\n';
        this.boardElem.innerHTML = output;
        
        // Update game over overlay
        this.updateGameOverOverlay(model);
        
        // Update piece previews
        this.renderNextPiece(model);
        this.renderHoldPiece(model);
    }
    
    /**
     * Update the game over overlay
     */
    updateGameOverOverlay(model) {
        let overlay = document.getElementById('gameOverOverlay');
        if (!overlay) {
            overlay = document.createElement('div');
            overlay.id = 'gameOverOverlay';
            overlay.className = 'game-over-overlay';
            document.querySelector('.game-board').appendChild(overlay);
        }
        
        if (model.gameOver) {
            overlay.innerHTML = `
                <div class="game-over-content">
                    <div class="game-over-title">GAME OVER!</div>
                    <div class="game-over-countdown">Restarting in ${model.restartCountdown}s</div>
                </div>
            `;
            overlay.style.display = 'flex';
        } else {
            overlay.style.display = 'none';
        }
    }
    
    /**
     * Update pause indicator
     */
    updatePauseIndicator(isPaused) {
        let pauseIndicator = document.getElementById('pauseIndicator');
        if (!pauseIndicator) {
            pauseIndicator = document.createElement('div');
            pauseIndicator.id = 'pauseIndicator';
            pauseIndicator.className = 'pause-indicator';
            document.querySelector('.game-board').appendChild(pauseIndicator);
        }
        
        if (isPaused) {
            pauseIndicator.innerHTML = `
                <div class="pause-content">
                    <div class="pause-title">PAUSED</div>
                    <div class="pause-hint">Press P to resume</div>
                </div>
            `;
            pauseIndicator.style.display = 'flex';
        } else {
            pauseIndicator.style.display = 'none';
        }
    }
    
    /**
     * Render next piece preview
     */
    renderNextPiece(model) {
        const nextPieceElem = document.getElementById('nextPieceDisplay');
        if (!nextPieceElem || !model.nextPiece) return;
        
        let output = '';
        for (let y = 0; y < model.nextPiece.shape.length; y++) {
            for (let x = 0; x < model.nextPiece.shape[y].length; x++) {
                if (model.nextPiece.shape[y][x]) {
                    output += '[]';
                } else {
                    output += '  ';
                }
            }
            output += '\n';
        }
        nextPieceElem.textContent = output;
    }
    
    /**
     * Render hold piece preview
     */
    renderHoldPiece(model) {
        const holdPieceElem = document.getElementById('holdPieceDisplay');
        if (!holdPieceElem) return;
        
        if (model.holdPiece) {
            let output = '';
            for (let y = 0; y < model.holdPiece.shape.length; y++) {
                for (let x = 0; x < model.holdPiece.shape[y].length; x++) {
                    if (model.holdPiece.shape[y][x]) {
                        output += '[]';
                    } else {
                        output += '  ';
                    }
                }
                output += '\n';
            }
            holdPieceElem.textContent = output;
        } else {
            holdPieceElem.textContent = '     \n     \n     \n';
        }
    }
}

// ============================================================================
// CONTROLLER - Input Handling and Game Loop
// ============================================================================

class TetrisController {
    constructor(model, view) {
        this.model = model;
        this.view = view;
        this.lastTime = 0;
        this.isRunning = false;
        this.isPaused = false;
        
        this.setupInputHandlers();
    }
    
    /**
     * Setup keyboard input handlers
     */
    setupInputHandlers() {
        document.addEventListener('keydown', (e) => {
            if (this.model.gameOver) return;
            
            switch (e.code) {
                case 'ArrowLeft':
                case 'Digit7':
                    this.model.movePiece(-1, 0);
                    break;
                    
                case 'ArrowRight':
                case 'Digit9':
                    this.model.movePiece(1, 0);
                    break;
                    
                case 'ArrowDown':
                case 'Digit4':
                    if (this.model.movePiece(0, 1)) {
                        this.model.score += 2; // Bonus for soft drop
                    }
                    break;
                    
                case 'ArrowUp':
                case 'Digit8':
                    this.model.rotateCurrentPiece();
                    break;
                    
                case 'Space':
                    // Hard drop (instant drop)
                    while (this.model.movePiece(0, 1)) {
                        this.model.score += 2; // Bonus for hard drop
                    }
                    break;
                    
                case 'KeyR':
                case 'Digit5':
                    this.model.reset();
                    this.isPaused = false;
                    break;
                    
                case 'KeyP':
                    this.isPaused = !this.isPaused;
                    if (this.isPaused) {
                        this.model.startPause();
                    } else {
                        this.model.endPause();
                    }
                    break;
                    
                case 'KeyC':
                    // Hold piece
                    this.model.holdCurrentPiece();
                    break;
            }
            
            this.updateUI();
        });
    }
    
    /**
     * Update UI elements
     */
    updateUI() {
        document.getElementById('score').textContent = this.model.score;
        document.getElementById('lines').textContent = this.model.lines;
        document.getElementById('level').textContent = this.model.level;
        
        // Update new UI elements if they exist
        const highScoreElem = document.getElementById('highScore');
        if (highScoreElem) {
            highScoreElem.textContent = this.model.highScore;
        }
        
        const timeElem = document.getElementById('time');
        if (timeElem) {
            timeElem.textContent = this.model.getGameTime();
        }
        
        const piecesElem = document.getElementById('pieces');
        if (piecesElem) {
            piecesElem.textContent = this.model.piecesPlaced;
        }
        
        const comboElem = document.getElementById('combo');
        if (comboElem) {
            comboElem.textContent = this.model.comboCount;
        }
    }
    
    /**
     * Game loop
     */
    gameLoop(currentTime) {
        if (!this.isRunning) return;
        
        const deltaTime = currentTime - this.lastTime;
        this.lastTime = currentTime;
        
        if (!this.isPaused) {
            this.model.update(deltaTime);
        }
        this.view.render(this.model);
        this.view.updatePauseIndicator(this.isPaused);
        this.updateUI();
        
        requestAnimationFrame((time) => this.gameLoop(time));
    }
    
    /**
     * Start the game
     */
    start() {
        this.isRunning = true;
        this.lastTime = performance.now();
        this.gameLoop(this.lastTime);
    }
    
    /**
     * Stop the game
     */
    stop() {
        this.isRunning = false;
    }
}

// ============================================================================
// INITIALIZATION
// ============================================================================

function initializeGame() {
    try {
        console.log('Creating TetrisModel...');
        const model = new TetrisModel();
        
        console.log('Creating TetrisView...');
        const view = new TetrisView();
        
        console.log('Creating TetrisController...');
        const controller = new TetrisController(model, view);
        
        console.log('Starting controller...');
        controller.start();
        
        window.tetrisController = controller;
        console.log('Game initialized successfully!');
        
        // Ensure the game board is visible
        const gameBoard = document.getElementById('asciiBoard');
        if (gameBoard) {
            gameBoard.style.display = 'block';
        }
        
    } catch (error) {
        console.error('Error initializing game:', error);
        // Fallback: try again after a short delay
        setTimeout(() => {
            console.log('Retrying game initialization...');
            initializeGame();
        }, 500);
    }
}

// Initialize the game when the page loads
document.addEventListener('DOMContentLoaded', () => {
    console.log('DOM loaded, checking language preference...');
    
    // Check for saved language preference
    if (!checkLanguagePreference()) {
        console.log('No language preference found, showing modal');
        // Show language selection modal
        const modal = document.getElementById('languageModal');
        if (modal) {
            modal.style.display = 'flex';
        }
    } else {
        console.log('Language preference found, starting game');
        // Language already selected, start game
        initializeGame();
    }
    
    // Debug: Add a manual start button for testing (only if no language selected)
    setTimeout(() => {
        if (!window.tetrisController && !localStorage.getItem('tetrisLanguage')) {
            console.log('Adding debug start button...');
            const debugBtn = document.createElement('button');
            debugBtn.textContent = 'Start Game (Debug)';
            debugBtn.style.position = 'fixed';
            debugBtn.style.top = '10px';
            debugBtn.style.right = '10px';
            debugBtn.style.zIndex = '9999';
            debugBtn.onclick = () => {
                console.log('Manual start clicked');
                document.getElementById('languageModal').style.display = 'none';
                initializeGame();
            };
            document.body.appendChild(debugBtn);
        }
    }, 1000);
}); 