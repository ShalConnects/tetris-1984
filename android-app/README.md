# 1984 Tetris - Android App

A faithful recreation of the original 1984 Tetris game for Android, implemented using Kotlin and Jetpack Compose. This app replicates the original Elektronika 60 version with authentic gameplay mechanics and monochrome green styling.

## Features

- **Authentic 1984 Gameplay**: Replicates the original Tetris mechanics exactly
- **10×20 Grid**: Original board dimensions
- **7 Classic Tetrominoes**: I, O, T, S, Z, J, L pieces with original rotation states
- **No Wall-Kicks**: Simple rotation system as in the original
- **Unbiased Randomizer**: Equal 1/7 chance for each piece (no history tracking)
- **1-Cell Gravity**: Original timing and movement
- **Monochrome Green Display**: Authentic terminal aesthetic
- **Simple Scoring**: 100 points per line (no speed levels)
- **Touch Controls**: On-screen buttons for mobile gameplay
- **Portrait Orientation**: Optimized for vertical play

## Project Structure

```
android-app/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/tetris1984/
│   │   │   ├── MainActivity.kt              # Main activity
│   │   │   ├── TetrisGame.kt                # Main game composable
│   │   │   ├── TetrisGameState.kt           # Game logic and state
│   │   │   └── ui/theme/
│   │   │       ├── Theme.kt                 # App theme
│   │   │       └── Type.kt                  # Typography
│   │   ├── res/
│   │   │   ├── values/
│   │   │   │   ├── colors.xml               # Color definitions
│   │   │   │   ├── strings.xml              # String resources
│   │   │   │   └── themes.xml               # Theme definitions
│   │   │   └── xml/
│   │   │       ├── backup_rules.xml         # Backup configuration
│   │   │       └── data_extraction_rules.xml # Data extraction rules
│   │   └── AndroidManifest.xml              # App manifest
│   ├── build.gradle                         # App-level build config
│   └── proguard-rules.pro                   # ProGuard rules
├── build.gradle                             # Project-level build config
├── settings.gradle                          # Project settings
└── README.md                                # This file
```

## Architecture

The app follows modern Android development practices:

- **Jetpack Compose**: Modern declarative UI framework
- **MVVM Pattern**: Model-View-ViewModel architecture
- **State Management**: Reactive state with Compose state
- **Game Loop**: Fixed timestep using LaunchedEffect
- **Touch Controls**: Custom button composables

## Controls

### Touch Controls
- **← →**: Move piece left/right
- **↻**: Rotate piece clockwise
- **↓**: Soft drop (accelerate piece)
- **Pause/Resume**: Toggle game pause
- **Reset**: Restart the game

### Game Features
- **Automatic Drop**: Pieces fall every 1 second (original timing)
- **Line Clearing**: Complete rows are removed
- **Scoring**: 100 points per line cleared
- **Level System**: Level increases every 10 lines
- **Game Over**: Detected when new piece can't be placed

## Build and Run

### Prerequisites
- Android Studio Arctic Fox (2020.3.1) or later
- Android SDK 24+ (API level 24)
- Kotlin 1.8.20+
- Gradle 7.0+

### Building the App

1. **Clone or download the project**
   ```bash
   git clone <repository-url>
   cd android-app
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open an existing project"
   - Navigate to the `android-app` folder and select it

3. **Sync Project**
   - Android Studio will automatically sync the project
   - Wait for Gradle sync to complete

4. **Build and Run**
   - Connect an Android device or start an emulator
   - Click the "Run" button (green play icon) or press Shift+F10
   - Select your target device and click "OK"

### Alternative: Command Line Build

```bash
# Navigate to project directory
cd android-app

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install on connected device
./gradlew installDebug
```

## Development

### Key Components

#### TetrisGameState.kt
- Manages all game logic and state
- Handles piece movement, rotation, and collision detection
- Implements original 1984 scoring system
- Contains piece definitions and randomizer

#### TetrisGame.kt
- Main UI composable
- Renders game board using Canvas
- Provides touch controls
- Displays score and game information

#### MainActivity.kt
- Entry point of the application
- Sets up Compose theme and content

### Modifying the Game

#### Game Logic
Edit `TetrisGameState.kt` to modify:
- Piece shapes and rotation
- Scoring system
- Game speed and timing
- Collision detection

#### Visual Design
Edit `TetrisGame.kt` and theme files to modify:
- Colors and styling
- UI layout and components
- Button appearance
- Game board rendering

#### Controls
Modify the `ControlButtons` composable in `TetrisGame.kt` to:
- Add new controls
- Change button layout
- Implement gesture controls
- Add haptic feedback

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
Each tetromino is defined as a 2D boolean array:
```kotlin
'I' to arrayOf(booleanArrayOf(true, true, true, true))
'O' to arrayOf(booleanArrayOf(true, true), booleanArrayOf(true, true))
'T' to arrayOf(booleanArrayOf(false, true, false), booleanArrayOf(true, true, true))
// etc.
```

### Game Loop
The game uses Compose's `LaunchedEffect` for the main loop:
```kotlin
LaunchedEffect(Unit) {
    while (true) {
        if (!gameState.isPaused && !gameState.isGameOver) {
            gameState.update()
        }
        delay(1000) // 1 second tick
    }
}
```

### State Management
Game state is managed using Compose state:
```kotlin
var board by mutableStateOf(Array(BOARD_HEIGHT) { Array(BOARD_WIDTH) { false } })
var score by mutableStateOf(0)
var isGameOver by mutableStateOf(false)
```

## Dependencies

- **AndroidX Core**: Core Android functionality
- **Jetpack Compose**: Modern UI framework
- **Material3**: Material Design components
- **Lifecycle**: Lifecycle management
- **Activity Compose**: Compose integration

## Target Devices

- **Minimum SDK**: API 24 (Android 7.0)
- **Target SDK**: API 34 (Android 14)
- **Screen Orientation**: Portrait only
- **Input**: Touch controls

## Performance

- **Frame Rate**: 60 FPS target
- **Memory Usage**: Minimal (simple 2D rendering)
- **Battery**: Optimized for extended gameplay
- **Storage**: < 10MB APK size

## License

This is an educational recreation of the original Tetris game. Tetris is a registered trademark of The Tetris Company.

## Credits

- Original Tetris created by Alexey Pajitnov (1984)
- This recreation is for educational purposes
- Monochrome green aesthetic inspired by the Elektronika 60 terminal
- Built with modern Android development tools 