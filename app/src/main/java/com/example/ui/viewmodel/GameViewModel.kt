package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.database.LevelProgressEntity
import com.example.data.model.GameMode
import com.example.data.model.Puzzle
import com.example.data.model.PuzzleCategory
import com.example.data.model.PuzzleType
import com.example.data.model.PuzzlesList
import com.example.data.repository.LevelRepository
import com.example.util.SoundManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface AppScreen {
    object Home : AppScreen
    object LevelSelect : AppScreen
    data class Gameplay(val levelId: Int, val mode: GameMode = GameMode.CLASSIC) : AppScreen
    object Stats : AppScreen
    object Leaderboard : AppScreen
}

sealed interface GameplayState {
    object Idle : GameplayState
    object Memoizing : GameplayState
    object Playing : GameplayState
    data class Success(
        val stars: Int,
        val score: Int,
        val timeTaken: Int,
        val modeMessage: String = ""
    ) : GameplayState
    data class Failed(
        val reason: String = "Yanlış Cevap",
        val isGameOver: Boolean = false
    ) : GameplayState
}

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: LevelRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = LevelRepository(database.levelProgressDao())
        
        viewModelScope.launch {
            repository.checkAndInitializeLevels()
        }
    }

    val allProgress: StateFlow<List<LevelProgressEntity>> = repository.allProgress
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _currentScreen = MutableStateFlow<AppScreen>(AppScreen.Home)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Active Game Mode
    var currentGameMode by mutableStateOf(GameMode.CLASSIC)
        private set

    // Dark Mode Theme State (Persisted in memory, defaults to true for sleek gaming feel)
    var isDarkTheme by mutableStateOf(true)
        private set

    fun toggleDarkTheme() {
        isDarkTheme = !isDarkTheme
    }

    // Gameplay States
    var activePuzzle by mutableStateOf<Puzzle?>(null)
        private set

    var gameplayState by mutableStateOf<GameplayState>(GameplayState.Idle)
        private set

    var timerSeconds by mutableStateOf(0)
        private set

    var hintUsed by mutableStateOf(false)
        private set

    // --- MODE SPECIFIC STATES ---
    // Zombie Mode State
    var zombieLives by mutableStateOf(3)
        private set
    var zombieCountdownSeconds by mutableStateOf(25)
        private set

    // Bomb Defusal Mode State
    var bombTimerSeconds by mutableStateOf(60)
        private set
    var wiresDefused by mutableStateOf(0)
        private set
    val totalWires = 5

    // Space Mode State
    var rocketAltitudeKm by mutableStateOf(0)
        private set
    var spaceFuel by mutableStateOf(100)
        private set
    val targetAltitudeKm = 100

    // Text & Inputs
    var userInput by mutableStateOf("")
        private set

    var selectedOption by mutableStateOf<String?>(null)
        private set

    // Anagram / Word Unscramble specific state
    var selectedLettersList = mutableStateOf<List<LetterItem>>(emptyList())
    var availableLettersList = mutableStateOf<List<LetterItem>>(emptyList())

    // Grid Memory specific state
    var activeGridTiles by mutableStateOf<Set<Int>>(emptySet())
        private set
    var userTappedGridTiles by mutableStateOf<Set<Int>>(emptySet())
        private set
    var gridDimension by mutableStateOf(3)
        private set

    // Number Memory specific state
    var memoryDigitsToShow by mutableStateOf("")
        private set

    private var timerJob: Job? = null
    private var modeCountdownJob: Job? = null

    data class LetterItem(val id: Int, val char: Char, val originalIndex: Int)

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
        if (screen is AppScreen.Gameplay) {
            currentGameMode = screen.mode
            // Reset mode stats if starting a new mode game from level 1 or custom entry
            if (screen.mode == GameMode.ZOMBIE && zombieLives <= 0) {
                zombieLives = 3
            } else if (screen.mode == GameMode.BOMB_DEFUSAL && (wiresDefused >= totalWires || bombTimerSeconds <= 0)) {
                bombTimerSeconds = 60
                wiresDefused = 0
            } else if (screen.mode == GameMode.SPACE_ESCAPE && (rocketAltitudeKm >= targetAltitudeKm || spaceFuel <= 0)) {
                rocketAltitudeKm = 0
                spaceFuel = 100
            }
            startLevel(screen.levelId)
        } else {
            stopGameplayTimer()
        }
    }

    fun selectGameModeAndPlay(mode: GameMode) {
        currentGameMode = mode
        when (mode) {
            GameMode.CLASSIC -> {
                val nextLevelId = allProgress.value.firstOrNull { !it.isCompleted && it.isUnlocked }?.levelId ?: 1
                navigateTo(AppScreen.Gameplay(nextLevelId, GameMode.CLASSIC))
            }
            GameMode.ZOMBIE -> {
                zombieLives = 3
                zombieCountdownSeconds = 25
                navigateTo(AppScreen.Gameplay(1, GameMode.ZOMBIE))
            }
            GameMode.BOMB_DEFUSAL -> {
                bombTimerSeconds = 60
                wiresDefused = 0
                navigateTo(AppScreen.Gameplay(1, GameMode.BOMB_DEFUSAL))
            }
            GameMode.SPACE_ESCAPE -> {
                rocketAltitudeKm = 0
                spaceFuel = 100
                navigateTo(AppScreen.Gameplay(1, GameMode.SPACE_ESCAPE))
            }
        }
    }

    private fun startLevel(levelId: Int) {
        val puzzle = PuzzlesList.puzzles.find { it.id == levelId } ?: return
        activePuzzle = puzzle
        gameplayState = GameplayState.Idle
        timerSeconds = 0
        hintUsed = false
        userInput = ""
        selectedOption = null
        userTappedGridTiles = emptySet()

        if (currentGameMode == GameMode.ZOMBIE) {
            zombieCountdownSeconds = 25
        }

        viewModelScope.launch {
            when (puzzle.type) {
                PuzzleType.GRID_MEMORY -> {
                    gameplayState = GameplayState.Memoizing
                    val size = puzzle.extraData.toIntOrNull() ?: 3
                    gridDimension = size
                    val tilesToRemember = puzzle.correctAnswer.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
                    activeGridTiles = tilesToRemember
                    
                    delay(2500)
                    gameplayState = GameplayState.Playing
                    startGameplayTimer()
                }
                PuzzleType.NUMBER_MEMORY -> {
                    gameplayState = GameplayState.Memoizing
                    memoryDigitsToShow = puzzle.correctAnswer
                    
                    delay(3000)
                    gameplayState = GameplayState.Playing
                    startGameplayTimer()
                }
                PuzzleType.WORD_UNSCRAMBLE -> {
                    gameplayState = GameplayState.Playing
                    val chars = puzzle.correctAnswer.toList()
                    val shuffled = chars.shuffled()
                    availableLettersList.value = shuffled.mapIndexed { idx, char -> LetterItem(idx, char, idx) }
                    selectedLettersList.value = emptyList()
                    startGameplayTimer()
                }
                else -> {
                    gameplayState = GameplayState.Playing
                    startGameplayTimer()
                }
            }
        }
    }

    private fun startGameplayTimer() {
        stopGameplayTimer()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                timerSeconds++
            }
        }

        // Mode specific timer loops
        if (currentGameMode == GameMode.ZOMBIE) {
            modeCountdownJob = viewModelScope.launch {
                while (zombieCountdownSeconds > 0) {
                    delay(1000)
                    zombieCountdownSeconds--
                }
                // Time up in zombie mode!
                onZombieTimeout()
            }
        } else if (currentGameMode == GameMode.BOMB_DEFUSAL) {
            modeCountdownJob = viewModelScope.launch {
                while (bombTimerSeconds > 0) {
                    delay(1000)
                    bombTimerSeconds--
                }
                // Bomb exploded!
                onBombExploded()
            }
        }
    }

    private fun stopGameplayTimer() {
        timerJob?.cancel()
        timerJob = null
        modeCountdownJob?.cancel()
        modeCountdownJob = null
    }

    private fun onZombieTimeout() {
        zombieLives--
        if (zombieLives <= 0) {
            stopGameplayTimer()
            gameplayState = GameplayState.Failed(
                reason = "Zombiler beynini ele geçirdi! Tüm canların tükendi.",
                isGameOver = true
            )
        } else {
            stopGameplayTimer()
            gameplayState = GameplayState.Failed(
                reason = "Süre doldu! Bir zombi beyninden bir ısırık aldı (-1 🧠). Kalan Can: $zombieLives",
                isGameOver = false
            )
        }
    }

    private fun onBombExploded() {
        stopGameplayTimer()
        gameplayState = GameplayState.Failed(
            reason = "BOMM! Saatli bomba patladı! Süre bitti.",
            isGameOver = true
        )
    }

    fun useHint() {
        hintUsed = true
    }

    fun selectOption(option: String) {
        if (gameplayState != GameplayState.Playing) return
        selectedOption = option
        checkAnswer(option)
    }

    fun onKeypadPress(digit: String) {
        if (gameplayState != GameplayState.Playing) return
        if (userInput.length < 15) {
            userInput += digit
        }
    }

    fun onKeypadBackspace() {
        if (gameplayState != GameplayState.Playing) return
        if (userInput.isNotEmpty()) {
            userInput = userInput.dropLast(1)
        }
    }

    fun submitNumericInput() {
        if (gameplayState != GameplayState.Playing) return
        checkAnswer(userInput.trim())
    }

    fun toggleGridTile(tileIndex: Int) {
        if (gameplayState != GameplayState.Playing) return
        val currentTapped = userTappedGridTiles.toMutableSet()
        if (currentTapped.contains(tileIndex)) {
            currentTapped.remove(tileIndex)
        } else {
            currentTapped.add(tileIndex)
        }
        userTappedGridTiles = currentTapped

        val correctTiles = activeGridTiles
        if (currentTapped.size == correctTiles.size) {
            if (currentTapped == correctTiles) {
                onSuccess()
            } else {
                val hasMistake = currentTapped.any { !correctTiles.contains(it) }
                if (hasMistake) {
                    onFailure("Hatalı karo seçimi!")
                }
            }
        }
    }

    fun tapAnagramLetter(letterItem: LetterItem, fromSelected: Boolean) {
        if (gameplayState != GameplayState.Playing) return
        if (fromSelected) {
            selectedLettersList.value = selectedLettersList.value.filter { it.id != letterItem.id }
            availableLettersList.value = availableLettersList.value + letterItem
        } else {
            availableLettersList.value = availableLettersList.value.filter { it.id != letterItem.id }
            selectedLettersList.value = selectedLettersList.value + letterItem
        }

        val puzzle = activePuzzle ?: return
        val totalLength = puzzle.correctAnswer.length
        if (selectedLettersList.value.size == totalLength) {
            val assembledWord = selectedLettersList.value.map { it.char }.joinToString("")
            checkAnswer(assembledWord)
        }
    }

    fun clearAnagram() {
        if (gameplayState != GameplayState.Playing) return
        val puzzle = activePuzzle ?: return
        val chars = puzzle.correctAnswer.toList()
        val shuffled = chars.shuffled()
        availableLettersList.value = shuffled.mapIndexed { idx, char -> LetterItem(idx, char, idx) }
        selectedLettersList.value = emptyList()
    }

    private fun checkAnswer(answer: String) {
        val puzzle = activePuzzle ?: return
        if (answer.equals(puzzle.correctAnswer, ignoreCase = true)) {
            onSuccess()
        } else {
            onFailure("Yanlış cevap verdiniz!")
        }
    }

    private fun onSuccess() {
        stopGameplayTimer()
        val puzzle = activePuzzle ?: return
        val timeTaken = timerSeconds
        val stars = when {
            timeTaken < 15 -> 3
            timeTaken < 45 -> 2
            else -> 1
        }
        
        var score = 1000 - (timeTaken * 8)
        if (hintUsed) score -= 150
        if (score < 300) score = 300

        var modeMessage = ""

        when (currentGameMode) {
            GameMode.CLASSIC -> {
                modeMessage = "Tebrikler! Seviye tamamlandı."
            }
            GameMode.ZOMBIE -> {
                modeMessage = "🧟 Zombiler alkışlıyor ve geri çekildi! Beynini başarıyla korudun!"
            }
            GameMode.BOMB_DEFUSAL -> {
                wiresDefused++
                bombTimerSeconds = (bombTimerSeconds + 10).coerceAtMost(90) // +10s bonus
                modeMessage = "💣 Tel kesildi! ($wiresDefused/$totalWires Tel) +10sn Süre Bonusu eklendi!"
            }
            GameMode.SPACE_ESCAPE -> {
                rocketAltitudeKm = (rocketAltitudeKm + 20).coerceAtMost(targetAltitudeKm)
                spaceFuel = (spaceFuel + 10).coerceAtMost(100)
                modeMessage = "🚀 İticiler ateşlendi! İrtifa: $rocketAltitudeKm km / $targetAltitudeKm km"
            }
        }

        gameplayState = GameplayState.Success(stars, score, timeTaken, modeMessage)

        // Persist to Room for classic progression
        if (currentGameMode == GameMode.CLASSIC) {
            viewModelScope.launch {
                repository.saveLevelCompletion(
                    levelId = puzzle.id,
                    stars = stars,
                    score = score,
                    timeTaken = timeTaken
                )
            }
        }
    }

    private fun onFailure(reason: String) {
        stopGameplayTimer()
        when (currentGameMode) {
            GameMode.CLASSIC -> {
                gameplayState = GameplayState.Failed(reason = reason, isGameOver = false)
            }
            GameMode.ZOMBIE -> {
                zombieLives--
                val isGameOver = zombieLives <= 0
                val msg = if (isGameOver) {
                    "🧟 Grrr... Zombiler beynini afiyetle yedi! Tüm beyin canların (🧠) tükendi."
                } else {
                    "🧟 Zombi homurtusu! -1 Beyin Canı kaybettin. Kalan Can: $zombieLives 🧠"
                }
                gameplayState = GameplayState.Failed(reason = msg, isGameOver = isGameOver)
            }
            GameMode.BOMB_DEFUSAL -> {
                bombTimerSeconds = (bombTimerSeconds - 8).coerceAtLeast(0)
                val isGameOver = bombTimerSeconds <= 0
                val msg = if (isGameOver) {
                    "💣 Geri sayım bitti! BOMM! Bomba infilak etti!"
                } else {
                    "⚠️ Yanlış hamle! -8 sn Ceza! Kalan süre: $bombTimerSeconds sn"
                }
                gameplayState = GameplayState.Failed(reason = msg, isGameOver = isGameOver)
            }
            GameMode.SPACE_ESCAPE -> {
                spaceFuel = (spaceFuel - 20).coerceAtLeast(0)
                val isGameOver = spaceFuel <= 0
                val msg = if (isGameOver) {
                    "🚀 Basınç ve yakıt tükendi! Görev başarısız oldu."
                } else {
                    "⚠️ Sistem arızası: -%20 Yakıt kaybı! Kalan Yakıt: %$spaceFuel"
                }
                gameplayState = GameplayState.Failed(reason = msg, isGameOver = isGameOver)
            }
        }
    }

    fun restartActiveLevel() {
        if (currentGameMode == GameMode.ZOMBIE && zombieLives <= 0) {
            zombieLives = 3
        } else if (currentGameMode == GameMode.BOMB_DEFUSAL && bombTimerSeconds <= 0) {
            bombTimerSeconds = 60
            wiresDefused = 0
        } else if (currentGameMode == GameMode.SPACE_ESCAPE && spaceFuel <= 0) {
            rocketAltitudeKm = 0
            spaceFuel = 100
        }
        activePuzzle?.let {
            startLevel(it.id)
        }
    }

    fun playNextLevel() {
        val currentId = activePuzzle?.id ?: return
        if (currentGameMode == GameMode.BOMB_DEFUSAL && wiresDefused >= totalWires) {
            // Bomb defused celebration! Back to home or next challenge
            navigateTo(AppScreen.Home)
            return
        }
        if (currentGameMode == GameMode.SPACE_ESCAPE && rocketAltitudeKm >= targetAltitudeKm) {
            // Rocket reached orbit! Back to home
            navigateTo(AppScreen.Home)
            return
        }

        if (currentId < 50) {
            navigateTo(AppScreen.Gameplay(currentId + 1, currentGameMode))
        } else {
            navigateTo(AppScreen.LevelSelect)
        }
    }

    fun resetAllProgress() {
        viewModelScope.launch {
            repository.resetAllProgress()
            navigateTo(AppScreen.Home)
        }
    }
}
