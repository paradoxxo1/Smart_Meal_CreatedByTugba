package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GameMode
import com.example.data.model.Puzzle
import com.example.data.model.PuzzleCategory
import com.example.data.model.PuzzleType
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.GameViewModel
import com.example.ui.viewmodel.GameplayState
import com.example.util.SoundManager

@Composable
fun GameplayScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val puzzle = viewModel.activePuzzle ?: return
    val gameState = viewModel.gameplayState
    val currentMode = viewModel.currentGameMode
    val scrollState = rememberScrollState()
    val shakeOffset = remember { Animatable(0f) }

    var showHintDialog by remember { mutableStateOf(false) }

    // Sound, vibration, and screen shake animation triggered on game state change
    LaunchedEffect(gameState) {
        when (gameState) {
            is GameplayState.Success -> {
                when (currentMode) {
                    GameMode.ZOMBIE -> SoundManager.playZombieApplause()
                    GameMode.BOMB_DEFUSAL -> SoundManager.playBombDefused()
                    GameMode.SPACE_ESCAPE -> SoundManager.playRocketBoost()
                    GameMode.CLASSIC -> SoundManager.playSuccess()
                }
            }
            is GameplayState.Failed -> {
                when (currentMode) {
                    GameMode.ZOMBIE -> SoundManager.playZombieGroan()
                    GameMode.BOMB_DEFUSAL -> SoundManager.playBombExplosion()
                    GameMode.SPACE_ESCAPE -> SoundManager.playSpaceAlarm()
                    GameMode.CLASSIC -> SoundManager.playError()
                }
                SoundManager.vibrateError(context)
                shakeOffset.snapTo(0f)
                shakeOffset.animateTo(
                    targetValue = 0f,
                    animationSpec = keyframes {
                        durationMillis = 400
                        0f at 0
                        (-24f) at 40
                        22f at 90
                        (-18f) at 150
                        15f at 210
                        (-10f) at 270
                        6f at 330
                        0f at 400
                    }
                )
            }
            else -> {}
        }
    }

    // Bomb Defusal mode real-time ticking tension sound
    LaunchedEffect(viewModel.bombTimerSeconds, gameState, currentMode) {
        if (currentMode == GameMode.BOMB_DEFUSAL && gameState is GameplayState.Playing && viewModel.bombTimerSeconds > 0) {
            SoundManager.playBombTick(isCritical = viewModel.bombTimerSeconds <= 15)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        currentMode.backgroundColorTop,
                        currentMode.backgroundColorBottom
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Atmospheric dynamic background overlays per mode
        if (currentMode == GameMode.ZOMBIE) {
            ZombieAtmosphericBackground()
        } else if (currentMode == GameMode.BOMB_DEFUSAL) {
            BombDefusalAtmosphericBackground(timerSeconds = viewModel.bombTimerSeconds)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header: Geri Dön, Seviye No / Mod Başlığı, İpucu, Timer
            GameplayHeader(
                levelId = puzzle.id,
                category = puzzle.category,
                mode = currentMode,
                timerSeconds = viewModel.timerSeconds,
                onBackClick = { 
                    SoundManager.playTap()
                    if (currentMode == GameMode.CLASSIC) {
                        viewModel.navigateTo(AppScreen.LevelSelect)
                    } else {
                        viewModel.navigateTo(AppScreen.Home)
                    }
                },
                onHintClick = {
                    SoundManager.playTap()
                    showHintDialog = true
                    viewModel.useHint()
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Mode-specific Top HUD Indicator (Zombie Brains, Bomb LED, Space Altitude)
            ModeSpecificHUD(viewModel = viewModel, mode = currentMode)

            Spacer(modifier = Modifier.height(10.dp))

            // Main Puzzle Display Card
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                when (gameState) {
                    is GameplayState.Memoizing -> {
                        MemoizingLayout(
                            puzzle = puzzle,
                            digits = viewModel.memoryDigitsToShow,
                            gridSize = viewModel.gridDimension,
                            activeTiles = viewModel.activeGridTiles
                        )
                    }
                    is GameplayState.Playing -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .offset(x = shakeOffset.value.dp)
                                .verticalScroll(scrollState),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Top
                        ) {
                            // Question Text & Visualizer Card
                            QuestionDisplayCard(puzzle = puzzle, mode = currentMode)

                            Spacer(modifier = Modifier.height(4.dp))

                            // Dynamic Interactive Puzzle Input Method
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                when (puzzle.type) {
                                    PuzzleType.MULTIPLE_CHOICE, PuzzleType.STROOP_COLOR -> {
                                        MultipleChoiceInput(
                                            options = puzzle.options,
                                            selectedOption = viewModel.selectedOption,
                                            mode = currentMode,
                                            onOptionSelect = {
                                                SoundManager.playTap()
                                                viewModel.selectOption(it)
                                            }
                                        )
                                    }
                                    PuzzleType.NUMERIC_INPUT, PuzzleType.NUMBER_MEMORY -> {
                                        NumericKeyboardInput(
                                            currentInput = viewModel.userInput,
                                            mode = currentMode,
                                            onKeyPress = {
                                                SoundManager.playTap()
                                                viewModel.onKeypadPress(it)
                                            },
                                            onBackspace = {
                                                SoundManager.playTap()
                                                viewModel.onKeypadBackspace()
                                            },
                                            onSubmit = {
                                                viewModel.submitNumericInput()
                                            }
                                        )
                                    }
                                    PuzzleType.GRID_MEMORY -> {
                                        InteractiveGridInput(
                                            gridSize = viewModel.gridDimension,
                                            tappedTiles = viewModel.userTappedGridTiles,
                                            mode = currentMode,
                                            onTileClick = {
                                                SoundManager.playTap()
                                                viewModel.toggleGridTile(it)
                                            }
                                        )
                                    }
                                    PuzzleType.WORD_UNSCRAMBLE -> {
                                        WordUnscrambleInput(
                                            selectedLetters = viewModel.selectedLettersList.value,
                                            availableLetters = viewModel.availableLettersList.value,
                                            mode = currentMode,
                                            onLetterClick = { item, fromSelected ->
                                                SoundManager.playTap()
                                                viewModel.tapAnagramLetter(item, fromSelected)
                                            },
                                            onClearClick = {
                                                SoundManager.playTap()
                                                viewModel.clearAnagram()
                                            }
                                        )
                                    }
                                }
                            }

                            // Secondary Hint Pill
                            Surface(
                                onClick = {
                                    SoundManager.playTap()
                                    showHintDialog = true
                                    viewModel.useHint()
                                },
                                shape = RoundedCornerShape(20.dp),
                                color = currentMode.surfaceColor,
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, WarningAmber.copy(alpha = 0.8f)),
                                modifier = Modifier
                                    .padding(vertical = 6.dp)
                                    .testTag("hint_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lightbulb,
                                        contentDescription = "İpucu",
                                        tint = WarningAmber,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "İpucu Al",
                                        fontSize = 13.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }
                    else -> {}
                }
            }
        }

        // --- SUCCESS MODAL OVERLAY ---
        if (gameState is GameplayState.Success) {
            OverlayBackground()
            SuccessPopup(
                stars = gameState.stars,
                score = gameState.score,
                timeTaken = gameState.timeTaken,
                mode = currentMode,
                modeMessage = gameState.modeMessage,
                onNextClick = { viewModel.playNextLevel() },
                onMenuClick = { 
                    if (currentMode == GameMode.CLASSIC) {
                        viewModel.navigateTo(AppScreen.LevelSelect)
                    } else {
                        viewModel.navigateTo(AppScreen.Home)
                    }
                }
            )
        }

        // --- FAILURE MODAL OVERLAY ---
        if (gameState is GameplayState.Failed) {
            OverlayBackground()
            FailurePopup(
                reason = gameState.reason,
                isGameOver = gameState.isGameOver,
                mode = currentMode,
                onRetryClick = { viewModel.restartActiveLevel() },
                onMenuClick = { 
                    if (currentMode == GameMode.CLASSIC) {
                        viewModel.navigateTo(AppScreen.LevelSelect)
                    } else {
                        viewModel.navigateTo(AppScreen.Home)
                    }
                }
            )
        }

        // --- HINT DIALOG MODAL ---
        if (showHintDialog) {
            AlertDialog(
                onDismissRequest = { showHintDialog = false },
                containerColor = Color(0xFF1E293B),
                shape = RoundedCornerShape(20.dp),
                icon = {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = "İpucu",
                        tint = WarningAmber,
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = {
                    Text(
                        text = "İpucu Bulundu!",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Text(
                        text = puzzle.hint,
                        color = Color(0xFFF1F5F9),
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = { showHintDialog = false },
                        colors = ButtonDefaults.textButtonColors(contentColor = currentMode.primaryColor)
                    ) {
                        Text("Anladım", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            )
        }
    }
}

@Composable
fun ModeSpecificHUD(viewModel: GameViewModel, mode: GameMode) {
    when (mode) {
        GameMode.CLASSIC -> {
            LinearProgressIndicator(
                progress = { (viewModel.activePuzzle?.id ?: 1) / 50f },
                color = getCategoryColor(viewModel.activePuzzle?.category ?: PuzzleCategory.MATH),
                trackColor = Color(0xFF1E293B),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
            )
        }
        GameMode.ZOMBIE -> {
            Card(
                colors = CardDefaults.cardColors(containerColor = mode.surfaceColor),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, mode.primaryColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Beyin Canı:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            repeat(3) { index ->
                                val hasLife = index < viewModel.zombieLives
                                Text(
                                    text = if (hasLife) "🧠" else "💀",
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                )
                            }
                        }

                        val sec = viewModel.zombieCountdownSeconds
                        val isCritical = sec <= 7
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isCritical) Icons.Default.Warning else Icons.Default.Timer,
                                contentDescription = "Sayaç",
                                tint = if (isCritical) Color(0xFFEF4444) else mode.primaryColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$sec sn",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isCritical) Color(0xFFEF4444) else mode.primaryColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Pulsating countdown progress
                    LinearProgressIndicator(
                        progress = { (viewModel.zombieCountdownSeconds / 25f).coerceIn(0f, 1f) },
                        color = if (viewModel.zombieCountdownSeconds <= 7) Color(0xFFEF4444) else mode.primaryColor,
                        trackColor = Color.Black.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape)
                    )
                }
            }
        }
        GameMode.BOMB_DEFUSAL -> {
            Card(
                colors = CardDefaults.cardColors(containerColor = mode.surfaceColor),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, mode.primaryColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "KESİLEN TELLER", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = mode.secondaryColor)
                        Spacer(modifier = Modifier.height(2.dp))
                        Row {
                            repeat(viewModel.totalWires) { idx ->
                                val isCut = idx < viewModel.wiresDefused
                                Text(
                                    text = if (isCut) "✂️" else "⚡",
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(horizontal = 1.dp)
                                )
                            }
                        }
                    }

                    // Digital Bomb LED Display
                    Surface(
                        color = Color.Black,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, mode.primaryColor)
                    ) {
                        val sec = viewModel.bombTimerSeconds
                        val m = sec / 60
                        val s = sec % 60
                        Text(
                            text = String.format("%02d:%02d", m, s),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = mode.primaryColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
        GameMode.SPACE_ESCAPE -> {
            Card(
                colors = CardDefaults.cardColors(containerColor = mode.surfaceColor),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, mode.primaryColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🚀", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "İrtifa: ${viewModel.rocketAltitudeKm} / ${viewModel.targetAltitudeKm} km",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = mode.primaryColor
                            )
                        }

                        Text(
                            text = "Yakıt: %${viewModel.spaceFuel}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (viewModel.spaceFuel < 30) Color(0xFFEF4444) else mode.secondaryColor
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { (viewModel.rocketAltitudeKm / 100f).coerceIn(0f, 1f) },
                        color = mode.primaryColor,
                        trackColor = Color.Black.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape)
                    )
                }
            }
        }
    }
}

@Composable
fun GameplayHeader(
    levelId: Int,
    category: PuzzleCategory,
    mode: GameMode,
    timerSeconds: Int,
    onBackClick: () -> Unit,
    onHintClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(38.dp)
                    .background(Color(0xFF1E293B), CircleShape)
                    .border(1.dp, mode.primaryColor.copy(alpha = 0.5f), CircleShape)
                    .testTag("gameplay_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Çık",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = if (mode == GameMode.CLASSIC) "SEVİYE $levelId" else "${mode.emoji} ${mode.title.uppercase()}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = mode.primaryColor
                )
                Text(
                    text = if (mode == GameMode.CLASSIC) category.displayName else "Soru #$levelId • ${category.displayName}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE2E8F0)
                )
            }
        }

        // Action Buttons: Hint + Timer Badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                onClick = onHintClick,
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, WarningAmber.copy(alpha = 0.8f)),
                modifier = Modifier.testTag("header_hint_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = "İpucu",
                        tint = WarningAmber,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "İpucu",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            if (mode == GameMode.CLASSIC) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, mode.primaryColor.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Süre",
                            tint = mode.primaryColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        val minutes = timerSeconds / 60
                        val seconds = timerSeconds % 60
                        Text(
                            text = String.format("%02d:%02d", minutes, seconds),
                            fontSize = 11.sp,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MemoizingLayout(
    puzzle: Puzzle,
    digits: String,
    gridSize: Int,
    activeTiles: Set<Int>
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "HAFIZANA KAZI!",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = WarningAmber,
            letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Gösterilen deseni aklında tutmaya çalış.",
            fontSize = 13.sp,
            color = Color(0xFFE2E8F0),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        if (puzzle.type == PuzzleType.NUMBER_MEMORY) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, IndigoPrimary.copy(alpha = 0.6f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = digits,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 4.sp
                    )
                }
            }
        } else if (puzzle.type == PuzzleType.GRID_MEMORY) {
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .background(Color(0xFF1E293B), RoundedCornerShape(16.dp))
                    .border(2.dp, IndigoPrimary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (row in 0 until gridSize) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            for (col in 0 until gridSize) {
                                val idx = row * gridSize + col
                                val isHighlighted = activeTiles.contains(idx)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .background(
                                            color = if (isHighlighted) WarningAmber else Color(0xFF334155),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MultipleChoiceInput(
    options: List<String>,
    selectedOption: String?,
    mode: GameMode,
    onOptionSelect: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        options.forEach { option ->
            val isSelected = selectedOption == option
            // Stand out clearly: slate blue background with bright white text when unselected, vibrant mode color when selected
            val btnBgColor = if (isSelected) mode.primaryColor else Color(0xFF243048)
            val textColor = if (isSelected) Color.Black else Color.White

            Card(
                colors = CardDefaults.cardColors(containerColor = btnBgColor),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    if (isSelected) mode.primaryColor else mode.primaryColor.copy(alpha = 0.6f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clickable { onOptionSelect(option) }
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = option,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
            }
        }
    }
}

@Composable
fun NumericKeyboardInput(
    currentInput: String,
    mode: GameMode,
    onKeyPress: (String) -> Unit,
    onBackspace: () -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display Area with crisp bright text
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .border(2.dp, mode.primaryColor, RoundedCornerShape(14.dp)),
        ) {
            Box(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currentInput.ifEmpty { "Cevabınızı giriniz..." },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (currentInput.isEmpty()) Color(0xFF94A3B8) else Color.White,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Cevabınızı tuşlayıp yeşil (✓) butonuna basarak onaylayın",
            fontSize = 11.sp,
            color = Color(0xFFE2E8F0),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // High contrast Tactile Keypad
        val keys = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("C", "0", "OK")
        )

        // Clear, bright slate-blue button background for maximum contrast on all dark backgrounds
        val keyBgColor = Color(0xFF243048)
        val keyBorderColor = mode.primaryColor.copy(alpha = 0.6f)

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            keys.forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    row.forEach { key ->
                        val isSpecial = key == "OK" || key == "C"
                        val btnColor = when (key) {
                            "OK" -> SuccessMint
                            "C" -> ErrorCrimson
                            else -> keyBgColor
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(btnColor)
                                .border(
                                    width = 1.5.dp,
                                    color = if (isSpecial) Color.Transparent else keyBorderColor,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    when (key) {
                                        "OK" -> onSubmit()
                                        "C" -> onBackspace()
                                        else -> onKeyPress(key)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (key == "C") {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                                        contentDescription = "Sil",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            } else if (key == "OK") {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Onayla",
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            } else {
                                Text(
                                    text = key,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InteractiveGridInput(
    gridSize: Int,
    tappedTiles: Set<Int>,
    mode: GameMode,
    onTileClick: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .size(260.dp)
            .background(Color(0xFF0F172A), RoundedCornerShape(16.dp))
            .border(2.dp, mode.primaryColor.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (row in 0 until gridSize) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    for (col in 0 until gridSize) {
                        val idx = row * gridSize + col
                        val isTapped = tappedTiles.contains(idx)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    color = if (isTapped) mode.primaryColor else Color(0xFF243048)
                                )
                                .border(
                                    width = 1.5.dp,
                                    color = if (isTapped) Color.White else Color(0xFF475569),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { onTileClick(idx) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WordUnscrambleInput(
    selectedLetters: List<GameViewModel.LetterItem>,
    availableLetters: List<GameViewModel.LetterItem>,
    mode: GameMode,
    onLetterClick: (GameViewModel.LetterItem, Boolean) -> Unit,
    onClearClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Assembled Letters Display Area
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .border(2.dp, mode.primaryColor, RoundedCornerShape(16.dp))
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (selectedLetters.isEmpty()) {
                    Text(
                        text = "Harflere dokunarak birleştirin...",
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8)
                    )
                } else {
                    selectedLetters.forEach { item ->
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(mode.primaryColor, RoundedCornerShape(8.dp))
                                .border(1.dp, Color.White, RoundedCornerShape(8.dp))
                                .clickable { onLetterClick(item, true) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item.char.toString(),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // High contrast Available letter bubbles
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            availableLetters.forEach { item ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(48.dp)
                        .background(Color(0xFF243048), CircleShape)
                        .border(1.5.dp, mode.primaryColor.copy(alpha = 0.8f), CircleShape)
                        .clickable { onLetterClick(item, false) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.char.toString(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Reset Button
        IconButton(
            onClick = onClearClick,
            modifier = Modifier
                .size(42.dp)
                .background(Color(0xFF1E293B), CircleShape)
                .border(1.5.dp, mode.primaryColor, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Temizle",
                tint = mode.primaryColor
            )
        }
    }
}

@Composable
fun OverlayBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
    )
}

@Composable
fun SuccessPopup(
    stars: Int,
    score: Int,
    timeTaken: Int,
    mode: GameMode,
    modeMessage: String,
    onNextClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .width(320.dp)
                .padding(16.dp)
                .border(2.dp, mode.primaryColor.copy(alpha = 0.8f), RoundedCornerShape(24.dp)),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = mode.emoji, fontSize = 48.sp)

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "HARİKA BAŞARI!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = mode.primaryColor
                )

                if (modeMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = modeMessage,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFCBD5E1),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Stars display
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) { index ->
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Star",
                            tint = if (index < stars) WarningAmber else Color(0xFF64748B),
                            modifier = Modifier
                                .size(32.dp)
                                .padding(horizontal = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Kazanılan Puan:", fontSize = 12.sp, color = Color(0xFFCBD5E1))
                    Text("$score", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WarningAmber)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Tamamlama Süresi:", fontSize = 12.sp, color = Color(0xFFCBD5E1))
                    Text("$timeTaken sn", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onNextClick,
                    colors = ButtonDefaults.buttonColors(containerColor = mode.primaryColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("success_next_level")
                ) {
                    Text(
                        text = "Sonraki Seviye / Görev",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                TextButton(
                    onClick = onMenuClick,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFCBD5E1))
                ) {
                    Text("Ana Sayfaya Dön", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun FailurePopup(
    reason: String,
    isGameOver: Boolean,
    mode: GameMode,
    onRetryClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .width(320.dp)
                .padding(16.dp)
                .border(2.dp, ErrorCrimson.copy(alpha = 0.8f), RoundedCornerShape(24.dp)),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isGameOver) "💀" else "⚠️",
                    fontSize = 48.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (isGameOver) "GÖREV BAŞARISIZ!" else "BİR DAHA DENE!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = ErrorCrimson
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = reason,
                    fontSize = 14.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onRetryClick,
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorCrimson),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("failure_retry_button")
                ) {
                    Text(
                        text = if (isGameOver) "Yeniden Başlat" else "Tekrar Dene",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                TextButton(
                    onClick = onMenuClick,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFCBD5E1))
                ) {
                    Text("Ana Sayfaya Dön", fontSize = 12.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuestionDisplayCard(puzzle: Puzzle, mode: GameMode) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF152033)),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .border(1.5.dp, mode.primaryColor.copy(alpha = 0.7f), RoundedCornerShape(18.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Category & Title Tag
            Surface(
                color = getCategoryColor(puzzle.category).copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, getCategoryColor(puzzle.category))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${puzzle.category.displayName.uppercase()} • ${puzzle.title}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = getCategoryColor(puzzle.category),
                        letterSpacing = 0.6.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Render according to content type
            if (puzzle.type == PuzzleType.STROOP_COLOR) {
                val colorVal = puzzle.extraData.removePrefix("0x").toLong(16)
                Text(
                    text = puzzle.question,
                    fontSize = 46.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(colorVal),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Surface(
                    color = WarningAmber.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = "🎯 DİKKAT: Kelimenin anlamına değil, YAZILDIĞI RENGİ seçin!",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = WarningAmber,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            } else if (puzzle.question.contains("➔")) {
                val lines = puzzle.question.split("\n\n")
                val description = lines.firstOrNull { !it.contains("➔") } ?: ""
                val sequenceLine = lines.firstOrNull { it.contains("➔") } ?: puzzle.question
                val sequenceItems = sequenceLine.split("➔").map { it.trim() }

                if (description.isNotEmpty()) {
                    Text(
                        text = description,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = 5
                ) {
                    sequenceItems.forEachIndexed { index, item ->
                        val isTarget = item == "?" || item.contains("?")
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .defaultMinSize(minWidth = 40.dp, minHeight = 40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (isTarget) WarningAmber.copy(alpha = 0.25f) else Color.White
                                    )
                                    .border(
                                        width = if (isTarget) 2.dp else 1.dp,
                                        color = if (isTarget) WarningAmber else Color(0xFFCBD5E1),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = item,
                                    fontSize = if (isTarget) 20.sp else 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isTarget) WarningAmber else Color(0xFF0F172A),
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            if (index < sequenceItems.size - 1) {
                                Text(
                                    text = " ➔ ",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                )
                            }
                        }
                    }
                }
            } else if (puzzle.question.contains(" = ?") || puzzle.question.contains(" =?")) {
                val lines = puzzle.question.split("\n\n")
                val description = lines.firstOrNull { !it.contains("=") } ?: ""
                val equationLine = lines.firstOrNull { it.contains("=") } ?: puzzle.question

                if (description.isNotEmpty()) {
                    Text(
                        text = description,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                Surface(
                    color = Color(0xFF0F172A),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, mode.primaryColor)
                ) {
                    Text(
                        text = equationLine,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = InfoCyan,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
                    )
                }
            } else if (puzzle.type == PuzzleType.WORD_UNSCRAMBLE) {
                val cleanQuestion = puzzle.question.replace(Regex("\n\n[A-ZÇĞİÖŞÜ -]+$"), "")
                Text(
                    text = cleanQuestion,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = LavenderSecondary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LavenderSecondary)
                ) {
                    Text(
                        text = "🎯 GÖREV: Aşağıdaki harflere sırayla dokunarak kelimeyi oluşturun",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFDDD6FE),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            } else if (puzzle.type == PuzzleType.NUMBER_MEMORY) {
                Text(
                    text = "Aklınızda tuttuğunuz ${puzzle.correctAnswer.length} basamaklı sayıyı giriniz:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = WarningAmber.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber)
                ) {
                    Text(
                        text = "🧠 Sayıyı tuşlayıp yeşil onay (✓) butonuna basınız",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = WarningAmber,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            } else if (puzzle.type == PuzzleType.GRID_MEMORY) {
                Text(
                    text = "Aklınızda tuttuğunuz karoların üzerine dokunun:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = InfoCyan.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, InfoCyan)
                ) {
                    Text(
                        text = "🎯 Hafızanızdaki ${puzzle.correctAnswer.split(",").size} karoya dokunarak deseni oluşturun",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = InfoCyan,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            } else {
                Text(
                    text = puzzle.question,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
            }
        }
    }
}

@Composable
fun ZombieAtmosphericBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "zombie_bg_anim")
    
    // Pulsating eerie eye glow
    val eyeGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "eyeGlow"
    )

    // Creeping fog floating offset
    val fogOffset by infiniteTransition.animateFloat(
        initialValue = -30f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fogOffset"
    )

    // Lurking breathing motion
    val zombieSway by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "zombieSway"
    )

    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val w = size.width
        val h = size.height

        // 1. Spooky full moon in upper right
        val moonCenter = Offset(w * 0.82f, h * 0.12f)
        drawCircle(
            color = Color(0x184ADE80), // sickly green halo
            radius = 65.dp.toPx(),
            center = moonCenter
        )
        drawCircle(
            color = Color(0x3522C55E),
            radius = 42.dp.toPx(),
            center = moonCenter
        )
        drawCircle(
            color = Color(0xFF86EFAC).copy(alpha = 0.25f),
            radius = 28.dp.toPx(),
            center = moonCenter
        )

        // 2. Creeping toxic mist & fog layers across screen
        val fogPath1 = Path().apply {
            moveTo(0f, h * 0.65f + fogOffset)
            cubicTo(w * 0.3f, h * 0.60f - fogOffset, w * 0.7f, h * 0.70f + fogOffset, w, h * 0.62f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(
            path = fogPath1,
            color = Color(0x1A14532D) // Dark eerie green fog
        )

        val fogPath2 = Path().apply {
            moveTo(0f, h * 0.80f - fogOffset)
            cubicTo(w * 0.4f, h * 0.85f + fogOffset, w * 0.6f, h * 0.75f - fogOffset, w, h * 0.82f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(
            path = fogPath2,
            color = Color(0x22052E16)
        )

        // 3. Left Side: Lurking Zombie Silhouette
        val leftZombieX = -10.dp.toPx() + zombieSway
        val leftZombieY = h * 0.42f
        val leftZombiePath = Path().apply {
            // Head
            moveTo(leftZombieX + 35.dp.toPx(), leftZombieY)
            lineTo(leftZombieX + 75.dp.toPx(), leftZombieY + 5.dp.toPx())
            lineTo(leftZombieX + 70.dp.toPx(), leftZombieY + 45.dp.toPx())
            lineTo(leftZombieX + 30.dp.toPx(), leftZombieY + 42.dp.toPx())
            close()
            // Neck & Torso
            moveTo(leftZombieX + 35.dp.toPx(), leftZombieY + 42.dp.toPx())
            lineTo(leftZombieX + 90.dp.toPx(), leftZombieY + 55.dp.toPx())
            lineTo(leftZombieX + 110.dp.toPx(), leftZombieY + 140.dp.toPx())
            lineTo(leftZombieX, leftZombieY + 160.dp.toPx())
            close()
            // Outstretched Reaching Arm
            moveTo(leftZombieX + 70.dp.toPx(), leftZombieY + 65.dp.toPx())
            lineTo(leftZombieX + 130.dp.toPx(), leftZombieY + 75.dp.toPx())
            lineTo(leftZombieX + 150.dp.toPx(), leftZombieY + 70.dp.toPx()) // Claw hand
            lineTo(leftZombieX + 135.dp.toPx(), leftZombieY + 88.dp.toPx())
            lineTo(leftZombieX + 65.dp.toPx(), leftZombieY + 85.dp.toPx())
            close()
        }
        drawPath(path = leftZombiePath, color = Color(0x60030712)) // Dark zombie silhouette

        // Left Zombie Glowing Eyes
        drawCircle(
            color = Color(0xFFEF4444).copy(alpha = eyeGlow), // Glowing red eyes
            radius = 3.5.dp.toPx(),
            center = Offset(leftZombieX + 54.dp.toPx(), leftZombieY + 22.dp.toPx())
        )
        drawCircle(
            color = Color(0xFFEF4444).copy(alpha = eyeGlow),
            radius = 3.5.dp.toPx(),
            center = Offset(leftZombieX + 66.dp.toPx(), leftZombieY + 24.dp.toPx())
        )

        // 4. Right Side: Lurking Zombie Silhouette
        val rightZombieX = w - 40.dp.toPx() - zombieSway
        val rightZombieY = h * 0.58f
        val rightZombiePath = Path().apply {
            // Head
            moveTo(rightZombieX - 15.dp.toPx(), rightZombieY)
            lineTo(rightZombieX + 30.dp.toPx(), rightZombieY - 5.dp.toPx())
            lineTo(rightZombieX + 25.dp.toPx(), rightZombieY + 40.dp.toPx())
            lineTo(rightZombieX - 20.dp.toPx(), rightZombieY + 38.dp.toPx())
            close()
            // Torso & Reaching Arms
            moveTo(rightZombieX - 15.dp.toPx(), rightZombieY + 38.dp.toPx())
            lineTo(rightZombieX - 85.dp.toPx(), rightZombieY + 65.dp.toPx()) // Reaching left into screen
            lineTo(rightZombieX - 110.dp.toPx(), rightZombieY + 60.dp.toPx()) // Claw fingers
            lineTo(rightZombieX - 80.dp.toPx(), rightZombieY + 80.dp.toPx())
            lineTo(rightZombieX + 40.dp.toPx(), rightZombieY + 120.dp.toPx())
            lineTo(rightZombieX + 40.dp.toPx(), rightZombieY + 160.dp.toPx())
            lineTo(rightZombieX - 40.dp.toPx(), rightZombieY + 160.dp.toPx())
            close()
        }
        drawPath(path = rightZombiePath, color = Color(0x60030712))

        // Right Zombie Glowing Toxic Green Eyes
        drawCircle(
            color = Color(0xFF22C55E).copy(alpha = eyeGlow),
            radius = 3.5.dp.toPx(),
            center = Offset(rightZombieX - 5.dp.toPx(), rightZombieY + 18.dp.toPx())
        )
        drawCircle(
            color = Color(0xFF22C55E).copy(alpha = eyeGlow),
            radius = 3.5.dp.toPx(),
            center = Offset(rightZombieX + 8.dp.toPx(), rightZombieY + 17.dp.toPx())
        )

        // 5. Bottom Gravestone & Fence Silhouettes
        val fencePath = Path().apply {
            moveTo(0f, h)
            lineTo(0f, h - 35.dp.toPx())
            lineTo(20.dp.toPx(), h - 55.dp.toPx()) // Tombstone 1
            lineTo(45.dp.toPx(), h - 55.dp.toPx())
            lineTo(48.dp.toPx(), h - 25.dp.toPx())
            lineTo(80.dp.toPx(), h - 30.dp.toPx())
            lineTo(85.dp.toPx(), h - 70.dp.toPx()) // Tombstone 2 Cross shape
            lineTo(95.dp.toPx(), h - 70.dp.toPx())
            lineTo(100.dp.toPx(), h - 25.dp.toPx())
            lineTo(w - 110.dp.toPx(), h - 25.dp.toPx())
            lineTo(w - 100.dp.toPx(), h - 60.dp.toPx()) // Right Tombstone
            lineTo(w - 75.dp.toPx(), h - 60.dp.toPx())
            lineTo(w - 70.dp.toPx(), h - 30.dp.toPx())
            lineTo(w, h - 35.dp.toPx())
            lineTo(w, h)
            close()
        }
        drawPath(path = fencePath, color = Color(0x75020617))
    }
}

@Composable
fun BombDefusalAtmosphericBackground(timerSeconds: Int) {
    val isCritical = timerSeconds <= 15
    val infiniteTransition = rememberInfiniteTransition(label = "bomb_bg_anim")

    // Emergency Red Strobe when time is running low
    val alertAlpha by infiniteTransition.animateFloat(
        initialValue = if (isCritical) 0.15f else 0.03f,
        targetValue = if (isCritical) 0.55f else 0.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isCritical) 350 else 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alertAlpha"
    )

    // Radar scan rotation
    val scanAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanAngle"
    )

    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val w = size.width
        val h = size.height

        // 1. Digital Grid & Circuit Traces
        val gridSpacing = 40.dp.toPx()
        val gridColor = Color(0xFFF97316).copy(alpha = 0.05f)

        var x = 0f
        while (x < w) {
            drawLine(
                color = gridColor,
                start = Offset(x, 0f),
                end = Offset(x, h),
                strokeWidth = 1.dp.toPx()
            )
            x += gridSpacing
        }

        var y = 0f
        while (y < h) {
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1.dp.toPx()
            )
            y += gridSpacing
        }

        // 2. Central Radar Target Dial
        val center = Offset(w / 2f, h * 0.45f)
        drawCircle(
            color = if (isCritical) Color(0xFFEF4444).copy(alpha = alertAlpha * 0.4f) else Color(0xFFF97316).copy(alpha = 0.08f),
            radius = 120.dp.toPx(),
            center = center,
            style = Stroke(width = 1.5.dp.toPx())
        )
        drawCircle(
            color = if (isCritical) Color(0xFFEF4444).copy(alpha = alertAlpha * 0.3f) else Color(0xFFF97316).copy(alpha = 0.05f),
            radius = 70.dp.toPx(),
            center = center,
            style = Stroke(width = 1.dp.toPx())
        )

        // 3. Corner Caution Hazard Stripes
        val stripeColor = if (isCritical) Color(0xFFEF4444).copy(alpha = alertAlpha) else Color(0xFFEAB308).copy(alpha = 0.25f)
        val stripeWidth = 6.dp.toPx()

        // Top Left Hazard Corner
        for (i in 0..5) {
            val offset = i * 16.dp.toPx()
            drawLine(
                color = stripeColor,
                start = Offset(0f, offset),
                end = Offset(offset, 0f),
                strokeWidth = stripeWidth
            )
        }

        // Top Right Hazard Corner
        for (i in 0..5) {
            val offset = i * 16.dp.toPx()
            drawLine(
                color = stripeColor,
                start = Offset(w - offset, 0f),
                end = Offset(w, offset),
                strokeWidth = stripeWidth
            )
        }

        // 4. Critical Alert Red Emergency Vignette
        if (isCritical) {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFFEF4444).copy(alpha = alertAlpha * 0.5f)
                    ),
                    center = center,
                    radius = w * 0.8f
                ),
                size = size
            )
        }
    }
}
