package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.LevelProgressEntity
import com.example.data.model.PuzzleCategory
import com.example.data.model.PuzzlesList
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelSelectScreen(
    viewModel: GameViewModel,
    progressList: List<LevelProgressEntity>,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppCustomColors.current
    var selectedCategoryFilter by remember { mutableStateOf<PuzzleCategory?>(null) }

    // Filter levels by selected category
    val displayedPuzzles = remember(selectedCategoryFilter) {
        if (selectedCategoryFilter == null) {
            PuzzlesList.puzzles
        } else {
            PuzzlesList.puzzles.filter { it.category == selectedCategoryFilter }
        }
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
                    modifier = Modifier.testTag("back_button_level_select")
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
                        text = "Seviye Seçimi",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (colors.isDark) RoseTertiary else IndigoPrimary
                    )
                    Text(
                        text = "Bilişsel becerilerini geliştir",
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }
            }

            // Category Filtering Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // "Tümü" filter button
                FilterChip(
                    selected = selectedCategoryFilter == null,
                    onClick = { selectedCategoryFilter = null },
                    label = { 
                        Text(
                            "Tümü", 
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedCategoryFilter == null) Color.White else colors.textPrimary
                        ) 
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = colors.cosmicSurface,
                        labelColor = colors.textSecondary,
                        selectedContainerColor = IndigoPrimary,
                        selectedLabelColor = Color.White
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedCategoryFilter == null,
                        borderColor = colors.cosmicSurfaceVariant,
                        selectedBorderColor = IndigoPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // Category chips (MATH, LOGIC, MEMORY, WORD)
                PuzzleCategory.values().forEach { category ->
                    val isCatSelected = selectedCategoryFilter == category
                    FilterChip(
                        selected = isCatSelected,
                        onClick = { selectedCategoryFilter = category },
                        label = { 
                            Text(
                                category.displayName, 
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCatSelected) Color.White else colors.textPrimary
                            ) 
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = colors.cosmicSurface,
                            labelColor = colors.textSecondary,
                            selectedContainerColor = getCategoryColor(category),
                            selectedLabelColor = Color.White
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isCatSelected,
                            borderColor = colors.cosmicSurfaceVariant,
                            selectedBorderColor = getCategoryColor(category)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Level Selection Grid (4 Columns)
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("levels_grid"),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(displayedPuzzles) { puzzle ->
                    val progress = progressList.find { it.levelId == puzzle.id }
                    val isUnlocked = progress?.isUnlocked == true
                    val isCompleted = progress?.isCompleted == true
                    val stars = progress?.stars ?: 0
                    val score = progress?.highScore ?: 0

                    LevelGridItem(
                        levelId = puzzle.id,
                        category = puzzle.category,
                        isUnlocked = isUnlocked,
                        isCompleted = isCompleted,
                        stars = stars,
                        score = score,
                        colors = colors,
                        onClick = {
                            if (isUnlocked) {
                                viewModel.navigateTo(AppScreen.Gameplay(puzzle.id))
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LevelGridItem(
    levelId: Int,
    category: PuzzleCategory,
    isUnlocked: Boolean,
    isCompleted: Boolean,
    stars: Int,
    score: Int,
    colors: AppCustomColors,
    onClick: () -> Unit
) {
    val categoryColor = getCategoryColor(category)

    Box(
        modifier = Modifier
            .aspectRatio(0.85f)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isUnlocked) colors.cosmicSurface else colors.cosmicSurface.copy(alpha = 0.4f))
            .clickable(enabled = isUnlocked, onClick = onClick)
            .border(
                width = 1.5.dp,
                color = when {
                    isCompleted -> categoryColor.copy(alpha = 0.8f)
                    isUnlocked -> IndigoPrimary
                    else -> colors.cosmicSurfaceVariant
                },
                shape = RoundedCornerShape(16.dp)
            )
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            // Top: Small category dots indicator
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(if (isUnlocked) categoryColor else Color.Gray, CircleShape)
            )

            // Center: Locked Icon or Level Number
            if (!isUnlocked) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Kilitli",
                    tint = colors.textMuted,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    text = "$levelId",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isCompleted) (if (colors.isDark) RoseTertiary else IndigoPrimary) else colors.textPrimary,
                    textAlign = TextAlign.Center
                )
            }

            // Bottom: Stars or score / category text
            if (isUnlocked && isCompleted) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) { index ->
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Yıldız",
                            tint = if (index < stars) WarningAmber else colors.textMuted,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            } else if (isUnlocked) {
                Text(
                    text = "AÇIK",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = SuccessMint,
                    letterSpacing = 0.5.sp
                )
            } else {
                Text(
                    text = "KİLİTLİ",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textMuted,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

fun getCategoryColor(category: PuzzleCategory): Color {
    return when (category) {
        PuzzleCategory.MATH -> WarningAmber
        PuzzleCategory.LOGIC -> InfoCyan
        PuzzleCategory.MEMORY -> CoralPinkAccent
        PuzzleCategory.WORD -> SuccessMint
    }
}
