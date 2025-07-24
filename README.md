# 1984 Tetris - Original Replica

A faithful recreation of the original 1984 Tetris game as it appeared on the Elektronika 60 computer, implemented in pure HTML5, CSS, and JavaScript.

## Features

- **Authentic 1984 Gameplay**: Replicates the original Tetris mechanics exactly
- **10×20 Grid**: Original board dimensions
- **7 Classic Tetrominoes**: I, O, T, S, Z, J, L pieces with original rotation states
- **No Wall-Kicks**: Simple rotation system as in the original
- **Unbiased Randomizer**: Equal 1/7 chance for each piece (no history tracking)
- **1-Cell Gravity**: Original timing and movement
- **Monochrome Green Display**: Authentic terminal aesthetic
- **Simple Scoring**: 100 points per line (no speed levels)

## File Structure

```
tetris-web/
├── index.html          # Main HTML file
├── style.css           # Monochrome green styling
├── tetris.js           # Game logic (Model-View-Controller)
└── README.md           # This file
```

## Architecture

The code follows a clean Model-View-Controller (MVC) pattern:

- **Model** (`TetrisModel`): Game state, piece logic, collision detection
- **View** (`TetrisView`): Canvas rendering, visual display
- **Controller** (`TetrisController`): Input handling, game loop

## Controls

### Keyboard Controls
- **Arrow Keys**: Move and rotate pieces
- **7/9**: Move left/right (original Elektronika 60 controls)
- **8**: Rotate piece
- **4**: Soft drop (accelerate)
- **5/Space**: Reset game
- **1**: Show next piece (toggle)
- **0**: Hide controls (toggle)

### Mobile Controls
- Touch-friendly interface with on-screen buttons
- Responsive design for various screen sizes

## Build and Run

### Prerequisites
- Modern web browser with HTML5 Canvas support
- No additional dependencies required

### Running the Game

1. **Simple Method**: Open `index.html` directly in your browser
2. **Local Server** (recommended for development):
   ```bash
   # Using Python 3
   python -m http.server 8000
   
   # Using Node.js (if you have http-server installed)
   npx http-server
   
   # Using PHP
   php -S localhost:8000
   ```

3. Navigate to `http://localhost:8000` in your browser

### Development

The game is written in vanilla JavaScript with no external dependencies. To modify:

- **Game Logic**: Edit `tetris.js` - the Model class contains all game mechanics
- **Visuals**: Modify `style.css` for styling or `tetris.js` View class for rendering
- **Controls**: Update the Controller class in `tetris.js`

## Original 1984 Specifications

This implementation follows the original Tetris specifications:

- **Platform**: Elektronika 60 (Soviet computer)
- **Grid**: 10×20 cells
- **Pieces**: 7 tetrominoes (I, O, T, S, Z, J, L)
- **Rotation**: Simple 90° clockwise (no wall-kicks)
- **Randomizer**: Unbiased (1/7 chance each)
- **Scoring**: 100 points per line cleared
- **Speed**: Fixed 1-second drop interval (no speed levels)
- **Display**: Monochrome green terminal

## Technical Details

### Piece Definitions
Each tetromino is defined as a 2D array where `1` represents a filled cell:
```javascript
I: [[1, 1, 1, 1]]
O: [[1, 1], [1, 1]]
T: [[0, 1, 0], [1, 1, 1]]
// etc.
```

### Rotation System
Pieces rotate clockwise around their center. The O-piece doesn't rotate (it's a square).

### Collision Detection
Simple boundary checking and collision detection with placed pieces.

### Line Clearing
Complete rows are removed and new empty rows are added at the top.

## Browser Compatibility

- Chrome 60+
- Firefox 55+
- Safari 12+
- Edge 79+

## License

This is an educational recreation of the original Tetris game. Tetris is a registered trademark of The Tetris Company.

## Credits

- Original Tetris created by Alexey Pajitnov (1984)
- This recreation is for educational purposes
- Monochrome green aesthetic inspired by the Elektronika 60 terminal 