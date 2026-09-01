package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.LevelProgressEntity
import com.example.data.model.PuzzleCategory
import com.example.data.model.PuzzlesList
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.GameViewModel

@Composable
fun StatsScreen(
    viewModel: GameViewModel,
    progressList: List<LevelProgressEntity>,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppCustomColors.current
    val scrollState = rememberScrollState()
    var showResetConfirm by remember { mutableStateOf(false) }

    // Metrics calculations
    val completedLevels = progressList.count { it.isCompleted }
    val totalStars = progressList.sumOf { it.stars }
    val maxStars = 150
    val totalScore = progressList.sumOf { it.highScore }
    val completedPuzzlesList = progressList.filter { it.isCompleted }
    val avgTime = if (completedPuzzlesList.isNotEmpty()) {
        completedPuzzlesList.map { it.timeTakenSeconds }.average().toInt()
    } else 0

    // Calculate category-specific completion
    val mathTotal = PuzzlesList.puzzles.count { it.category == PuzzleCategory.MATH }
    val mathCompleted = progressList.count { progress ->
        val p = PuzzlesList.puzzles.find { it.id == progress.levelId }
        p?.category == PuzzleCategory.MATH && progress.isCompleted
    }

    val logicTotal = PuzzlesList.puzzles.count { it.category == PuzzleCategory.LOGIC }
    val logicCompleted = progressList.count { progress ->
        val p = PuzzlesList.puzzles.find { it.id == progress.levelId }
        p?.category == PuzzleCategory.LOGIC && progress.isCompleted
    }

    val memoryTotal = PuzzlesList.puzzles.count { it.category == PuzzleCategory.MEMORY }
    val memoryCompleted = progressList.count { progress ->
        val p = PuzzlesList.puzzles.find { it.id == progress.levelId }
        p?.category == PuzzleCategory.MEMORY && progress.isCompleted
    }

    val wordTotal = PuzzlesList.puzzles.count { it.category == PuzzleCategory.WORD }
    val wordCompleted = progressList.count { progress ->
        val p = PuzzlesList.puzzles.find { it.id == progress.levelId }
        p?.category == PuzzleCategory.WORD && progress.isCompleted
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        colors.cosmicBackground,
                        colors.cosmicSurfaceVariant.copy(alpha = if (colors.isDark) 0.5f else 0.8f)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(AppScreen.Home) },
                    modifier = Modifier.testTag("stats_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Geri Dön",
                        tint = if (colors.isDark) RoseTertiary else IndigoPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Bilişsel Analiz",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (colors.isDark) RoseTertiary else IndigoPrimary
                    )
                    Text(
                        text = "Zihinsel performans gelişim grafiğin",
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Metrics Summary Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatMiniCard(
                    title = "Egzersiz",
                    value = "$completedLevels/50",
                    icon = Icons.Default.Psychology,
                    iconColor = InfoCyan,
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )

                StatMiniCard(
                    title = "Toplam Yıldız",
                    value = "$totalStars/$maxStars",
                    icon = Icons.Default.Star,
                    iconColor = WarningAmber,
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatMiniCard(
                    title = "Toplam Skor",
                    value = "$totalScore",
                    icon = Icons.Default.EmojiEvents,
                    iconColor = CoralPinkAccent,
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )

                StatMiniCard(
                    title = "Ort. Hız",
                    value = "$avgTime sn",
                    icon = Icons.Default.Timer,
                    iconColor = SuccessMint,
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Cognitive Category Skills progress bars
            Text(
                text = "Kategori Gelişim Analizleri",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (colors.isDark) RoseTertiary else IndigoPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = colors.cosmicSurface),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.cosmicSurfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CategorySkillRow(
                        categoryName = "Mantıksal Akıl Yürütme",
                        completed = logicCompleted,
                        total = logicTotal,
                        barColor = InfoCyan,
                        colors = colors
                    )

                    CategorySkillRow(
                        categoryName = "Sayısal Zeka",
                        completed = mathCompleted,
                        total = mathTotal,
                        barColor = WarningAmber,
                        colors = colors
                    )

                    CategorySkillRow(
                        categoryName = "Görsel Hafıza",
                        completed = memoryCompleted,
                        total = memoryTotal,
                        barColor = CoralPinkAccent,
                        colors = colors
                    )

                    CategorySkillRow(
                        categoryName = "Sözel Akıcılık",
                        completed = wordCompleted,
                        total = wordTotal,
                        barColor = SuccessMint,
                        colors = colors
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Dangerous Area: Reset Button
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.cosmicSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, ErrorCrimson.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "İlerlemeyi Sıfırla",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = ErrorCrimson
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tüm tamamlanan seviyeleri, yıldızları ve yüksek skorları kalıcı olarak temizler.",
                        fontSize = 11.sp,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showResetConfirm = true },
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorCrimson),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(40.dp)
                            .testTag("reset_progress_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Sıfırla",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Verileri Sıfırla", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }

        // Reset progress confirm popup dialog
        if (showResetConfirm) {
            AlertDialog(
                onDismissRequest = { showResetConfirm = false },
                containerColor = colors.cosmicSurface,
                shape = RoundedCornerShape(20.dp),
                icon = {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Confirm Reset",
                        tint = ErrorCrimson,
                        modifier = Modifier.size(36.dp)
                    )
                },
                title = {
                    Text(
                        text = "Emin misiniz?",
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Text(
                        text = "Bütün oyun ilerlemeniz silinecektir. Bu işlem geri alınamaz!",
                        color = colors.textSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showResetConfirm = false
                            viewModel.resetAllProgress()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorCrimson)
                    ) {
                        Text("Evet, Sıfırla", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showResetConfirm = false },
                        colors = ButtonDefaults.textButtonColors(contentColor = colors.textSecondary)
                    ) {
                        Text("İptal Et")
                    }
                }
            )
        }
    }
}

@Composable
fun StatMiniCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    colors: AppCustomColors,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = colors.cosmicSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.cosmicSurfaceVariant),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(iconColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )
                Text(
                    text = value,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            }
        }
    }
}

@Composable
fun CategorySkillRow(
    categoryName: String,
    completed: Int,
    total: Int,
    barColor: Color,
    colors: AppCustomColors
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = categoryName,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Text(
                text = "$completed / $total",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = barColor
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        val fraction = if (total > 0) completed / total.toFloat() else 0f
        val animatedFraction by animateFloatAsState(
            targetValue = fraction,
            animationSpec = tween(durationMillis = 800),
            label = "fraction"
        )
        LinearProgressIndicator(
            progress = { animatedFraction },
            color = barColor,
            trackColor = colors.cosmicSurfaceVariant,
            strokeCap = StrokeCap.Round,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        )
    }
}
