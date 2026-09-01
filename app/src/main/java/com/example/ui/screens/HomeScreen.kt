package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.database.LevelProgressEntity
import com.example.data.model.GameMode
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.GameViewModel
import com.example.util.SoundManager

@Composable
fun HomeScreen(
    viewModel: GameViewModel,
    progressList: List<LevelProgressEntity>,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppCustomColors.current
    val scrollState = rememberScrollState()
    var isMuted by remember { mutableStateOf(SoundManager.isMuted()) }

    // Calculate user progress values
    val completedLevels = progressList.count { it.isCompleted }
    val totalScore = progressList.sumOf { it.highScore }
    // User starts with 80 IQ and gains 2.4 IQ per completed level (Max 200 IQ)
    val estimatedIQ = 80 + (completedLevels * 2.4f).coerceAtMost(120f)
    
    // Find next playable level ID
    val nextLevelId = progressList.firstOrNull { !it.isCompleted && it.isUnlocked }?.levelId ?: 1

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
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Controls Bar: Dark/Light Mode Switcher & Sound Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Theme Toggle Pill
                Surface(
                    onClick = {
                        SoundManager.playTap()
                        viewModel.toggleDarkTheme()
                    },
                    shape = RoundedCornerShape(20.dp),
                    color = colors.cosmicSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.cosmicSurfaceVariant),
                    modifier = Modifier.testTag("theme_toggle_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (colors.isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = if (colors.isDark) "Karanlık Tema Aktif" else "Aydınlık Tema Aktif",
                            tint = if (colors.isDark) WarningAmber else IndigoPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (colors.isDark) "Karanlık" else "Aydınlık",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                    }
                }

                // Sound Toggle Button
                IconButton(
                    onClick = {
                        val newMuted = !isMuted
                        isMuted = newMuted
                        SoundManager.setMuted(newMuted)
                        if (!newMuted) SoundManager.playTap()
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(colors.cosmicSurface, CircleShape)
                        .border(1.dp, colors.cosmicSurfaceVariant, CircleShape)
                        .testTag("sound_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = if (isMuted) "Sesi Aç" else "Sesi Kapat",
                        tint = if (isMuted) colors.textMuted else IndigoPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Brand Header with Neon Brain Icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "IQ Masters Logo",
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "IQ MASTERS",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (colors.isDark) RoseTertiary else IndigoPrimary,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Beyin Egzersizleri & Oyun Modları",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textSecondary,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Premium IQ Score Card
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.cosmicSurface),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.cosmicSurfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = if (colors.isDark) 8.dp else 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("iq_score_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "MEVCUT IQ SEVİYESİ",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = IndigoPrimary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = String.format("%.1f", estimatedIQ),
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Black,
                        color = if (colors.isDark) RoseTertiary else Color(0xFF0F172A)
                    )
                    
                    // Progress Indicator
                    Spacer(modifier = Modifier.height(12.dp))
                    val progressFraction = completedLevels / 50f
                    val animatedProgress by animateFloatAsState(
                        targetValue = progressFraction,
                        animationSpec = tween(durationMillis = 1000),
                        label = "progress"
                    )
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        color = IndigoPrimary,
                        trackColor = colors.cosmicSurfaceVariant,
                        strokeCap = StrokeCap.Round,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Seviye İlerlemesi: $completedLevels / 50",
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                        Text(
                            text = "Toplam Puan: $totalScore",
                            fontSize = 12.sp,
                            color = WarningAmber,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ==========================================
            // OYUN MODLARI SEÇİM ALANI (GAME MODES)
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "OYUN MODLARI",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = if (colors.isDark) RoseTertiary else Color(0xFF0F172A),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = IndigoPrimary.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "YENİ",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = IndigoPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 1. Zombi Modu Kartı (Öne Çıkan)
            GameModeFeaturedCard(
                mode = GameMode.ZOMBIE,
                badge = "POPÜLER & EĞLENCELİ",
                onPlayClick = {
                    SoundManager.playTap()
                    viewModel.selectGameModeAndPlay(GameMode.ZOMBIE)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Bomba İmha & Uzay Görevi Yan Yana Mod Kartları
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Bomba İmha Modu
                GameModeCompactCard(
                    mode = GameMode.BOMB_DEFUSAL,
                    modifier = Modifier.weight(1f),
                    onPlayClick = {
                        SoundManager.playTap()
                        viewModel.selectGameModeAndPlay(GameMode.BOMB_DEFUSAL)
                    }
                )

                // Uzay Görevi Modu
                GameModeCompactCard(
                    mode = GameMode.SPACE_ESCAPE,
                    modifier = Modifier.weight(1f),
                    onPlayClick = {
                        SoundManager.playTap()
                        viewModel.selectGameModeAndPlay(GameMode.SPACE_ESCAPE)
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Klasik Mod Başlat Butonu
            Button(
                onClick = { 
                    SoundManager.playTap()
                    viewModel.selectGameModeAndPlay(GameMode.CLASSIC)
                },
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("workout_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = "🧠", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (completedLevels == 50) "Klasik Mod: Tekrar Antrenman" else "Klasik Mod: Seviye $nextLevelId'den Başla",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sub-Navigation Section Card Grid (Seviyeler, Sıralama, İstatistikler)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Levels Grid Card
                Card(
                    onClick = { 
                        SoundManager.playTap()
                        viewModel.navigateTo(AppScreen.LevelSelect) 
                    },
                    colors = CardDefaults.cardColors(containerColor = colors.cosmicSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.cosmicSurfaceVariant),
                    modifier = Modifier
                        .weight(1f)
                        .height(100.dp)
                        .testTag("nav_levels_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Assignment,
                            contentDescription = "Seviyeler",
                            tint = InfoCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "65 BULMACA",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            maxLines = 1
                        )
                        Text(
                            text = "Seviyeler",
                            fontSize = 10.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                // 2. Leaderboard Card (Liderlik Tablosu & Ligler)
                Card(
                    onClick = { 
                        SoundManager.playTap()
                        viewModel.navigateTo(AppScreen.Leaderboard) 
                    },
                    colors = CardDefaults.cardColors(containerColor = colors.cosmicSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarningAmber.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .weight(1f)
                        .height(100.dp)
                        .testTag("nav_leaderboard_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Liderlik Sıralaması",
                            tint = WarningAmber,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "SIRALAMA",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            maxLines = 1
                        )
                        Text(
                            text = "Lig & Podyum",
                            fontSize = 10.sp,
                            color = WarningAmber
                        )
                    }
                }

                // 3. Stats Dashboard Card
                Card(
                    onClick = { 
                        SoundManager.playTap()
                        viewModel.navigateTo(AppScreen.Stats) 
                    },
                    colors = CardDefaults.cardColors(containerColor = colors.cosmicSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.cosmicSurfaceVariant),
                    modifier = Modifier
                        .weight(1f)
                        .height(100.dp)
                        .testTag("nav_stats_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = "İstatistikler",
                            tint = CoralPinkAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "ANALİZ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            maxLines = 1
                        )
                        Text(
                            text = "Grafik",
                            fontSize = 10.sp,
                            color = colors.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Cognitive Categories Display (Turkish translations)
            Text(
                text = "Bilişsel Egzersiz Alanları",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (colors.isDark) RoseTertiary else Color(0xFF0F172A),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                textAlign = TextAlign.Start
            )

            CategoryRow(
                title = "Mantıksal Akıl Yürütme",
                description = "Analitik düşünme, örüntü kavrama ve karar verme.",
                icon = Icons.Default.Extension,
                accentColor = InfoCyan,
                colors = colors
            )

            CategoryRow(
                title = "Sayısal Zeka",
                description = "Hızlı aritmetik, denklem kurma ve sayı dizileri.",
                icon = Icons.Default.EmojiEvents,
                accentColor = WarningAmber,
                colors = colors
            )

            CategoryRow(
                title = "Görsel Hafıza",
                description = "Konumsal bellek, Stroop testleri ve odaklanma.",
                icon = Icons.Default.Psychology,
                accentColor = CoralPinkAccent,
                colors = colors
            )

            CategoryRow(
                title = "Sözel Akıcılık",
                description = "Sözcük ilişkileri, anagramlar ve kelime haznesi.",
                icon = Icons.Default.School,
                accentColor = SuccessMint,
                colors = colors
            )

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun GameModeFeaturedCard(
    mode: GameMode,
    badge: String,
    onPlayClick: () -> Unit
) {
    Card(
        onClick = onPlayClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = mode.surfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.5.dp,
                brush = Brush.horizontalGradient(
                    listOf(mode.primaryColor.copy(alpha = 0.8f), mode.secondaryColor.copy(alpha = 0.8f))
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .testTag("mode_card_${mode.name.lowercase()}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = mode.emoji, fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = mode.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = mode.primaryColor
                        )
                        Text(
                            text = mode.subtitle,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = mode.secondaryColor
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = mode.primaryColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = badge,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = mode.primaryColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = mode.description,
                fontSize = 12.sp,
                color = Color(0xFFE2E8F0),
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🧠 🧠 🧠", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "3 Can | 25sn Sayaç", fontSize = 11.sp, color = Color(0xFF94A3B8))
                }

                Button(
                    onClick = onPlayClick,
                    colors = ButtonDefaults.buttonColors(containerColor = mode.primaryColor),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Oyna",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun GameModeCompactCard(
    mode: GameMode,
    modifier: Modifier = Modifier,
    onPlayClick: () -> Unit
) {
    Card(
        onClick = onPlayClick,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = mode.surfaceColor),
        modifier = modifier
            .height(160.dp)
            .border(
                width = 1.dp,
                color = mode.primaryColor.copy(alpha = 0.5f),
                shape = RoundedCornerShape(18.dp)
            )
            .testTag("mode_card_${mode.name.lowercase()}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(text = mode.emoji, fontSize = 24.sp)
                Surface(
                    shape = CircleShape,
                    color = mode.primaryColor.copy(alpha = 0.2f),
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Oyna",
                            tint = mode.primaryColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Column {
                Text(
                    text = mode.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = mode.primaryColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = mode.subtitle,
                    fontSize = 11.sp,
                    color = mode.secondaryColor,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (mode == GameMode.BOMB_DEFUSAL) "60sn Sayaç & Tel Kesme" else "100km İrtifa Hedefi",
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun CategoryRow(
    title: String,
    description: String,
    icon: ImageVector,
    accentColor: Color,
    colors: AppCustomColors
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(colors.cosmicSurface, RoundedCornerShape(16.dp))
            .border(1.dp, colors.cosmicSurfaceVariant, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(accentColor.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 11.sp,
                color = colors.textSecondary,
                lineHeight = 15.sp
            )
        }
    }
}

val CoralPinkAccent = Color(0xFFEC4899)
