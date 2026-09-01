package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.LevelProgressEntity
import com.example.data.model.LeaderboardUser
import com.example.data.model.UserLeague
import com.example.data.repository.LeaderboardRepository
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.GameViewModel
import com.example.util.SoundManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    viewModel: GameViewModel,
    progressList: List<LevelProgressEntity>,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppCustomColors.current

    val completedLevels = progressList.count { it.isCompleted }
    val totalScore = progressList.sumOf { it.highScore }
    val estimatedIQ = 80.0 + (completedLevels * 2.4).coerceAtMost(120.0)

    var selectedTab by remember { mutableStateOf(0) } // 0: Genel IQ, 1: Ligler & Kademeler
    val leaderboardUsers = remember(totalScore, completedLevels, estimatedIQ) {
        LeaderboardRepository.getLeaderboard(
            userScore = totalScore,
            userIq = estimatedIQ,
            userSolvedCount = completedLevels,
            username = "Sen (Senin Profilin)"
        )
    }

    val currentUser = leaderboardUsers.find { it.isCurrentUser }
    val top3 = leaderboardUsers.take(3)
    val remainingUsers = leaderboardUsers.drop(3)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🏆", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Liderlik Sıralaması",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = colors.textPrimary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        SoundManager.playTap()
                        viewModel.navigateTo(AppScreen.Home)
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri",
                            tint = colors.textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.cosmicBackground
                )
            )
        },
        containerColor = colors.cosmicBackground,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Switcher
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = colors.cosmicSurface,
                contentColor = IndigoPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = {
                        SoundManager.playTap()
                        selectedTab = 0
                    },
                    text = {
                        Text(
                            text = "🌐 Küresel Sıralama",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        SoundManager.playTap()
                        selectedTab = 1
                    },
                    text = {
                        Text(
                            text = "🛡️ Ligler & Kademeler",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
            }

            if (selectedTab == 0) {
                // ==========================================
                // TAB 1: KÜRESEL LİDERLİK TABLOSU & PODYUM
                // ==========================================
                Box(modifier = Modifier.weight(1f)) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 100.dp, start = 16.dp, end = 16.dp, top = 8.dp)
                    ) {
                        // 1. PODYUM (Top 3 Oyuncu)
                        if (top3.size >= 3) {
                            item {
                                PodiumView(top3 = top3, colors = colors)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "DİĞER YARIŞMACILAR",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textSecondary,
                                    letterSpacing = 1.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                                )
                            }
                        }

                        // 2. LİSTE (4. ve Sonraki Sıralar)
                        items(remainingUsers) { user ->
                            LeaderboardUserRow(user = user, colors = colors)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    // ALT SABİT KULLANICI KARTI (Oyuncunun Anlık Sıralaması)
                    if (currentUser != null) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(20.dp),
                            color = IndigoPrimary,
                            shadowElevation = 12.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Sıra Rozeti
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color.White.copy(alpha = 0.25f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "#${currentUser.rank}",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Kullanıcı Bilgisi
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = currentUser.avatarEmoji, fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = currentUser.username,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Text(
                                        text = "${currentUser.league.badgeIcon} ${currentUser.league.title} • ${currentUser.solvedCount} Soru",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }

                                // Skor & IQ
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${currentUser.score} Puan",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp,
                                        color = WarningAmber
                                    )
                                    Text(
                                        text = "IQ: ${String.format("%.1f", currentUser.estimatedIq)}",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // ==========================================
                // TAB 2: LİGLER VE KADEMELER
                // ==========================================
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    item {
                        Text(
                            text = "LİG SİSTEMİ VE DERECELER",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = colors.textPrimary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Soru çözdükçe ve puan kazandıkça lig atlayın, Zeka Ustası tacını kazanın!",
                            fontSize = 12.sp,
                            color = colors.textSecondary,
                            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                        )
                    }

                    items(UserLeague.values().toList()) { league ->
                        val isUserCurrentLeague = currentUser?.league == league
                        val isUnlocked = totalScore >= league.minScore

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isUserCurrentLeague) IndigoPrimary.copy(alpha = 0.15f) else colors.cosmicSurface
                            ),
                            shape = RoundedCornerShape(18.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isUserCurrentLeague) 2.dp else 1.dp,
                                color = if (isUserCurrentLeague) IndigoPrimary else colors.cosmicSurfaceVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(Color(league.colorHex).copy(alpha = 0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = league.badgeIcon, fontSize = 24.sp)
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = league.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = colors.textPrimary
                                        )
                                        if (isUserCurrentLeague) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Surface(
                                                color = IndigoPrimary,
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text(
                                                    text = "MEVCUT LİGİNİZ",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Gereken Puan: ${league.minScore}+ Puan",
                                        fontSize = 12.sp,
                                        color = colors.textSecondary
                                    )
                                }

                                Text(
                                    text = if (isUnlocked) "AÇILDI ✓" else "KİLİTLİ 🔒",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isUnlocked) SuccessMint else colors.textSecondary
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
fun PodiumView(top3: List<LeaderboardUser>, colors: AppCustomColors) {
    val first = top3.getOrNull(0)
    val second = top3.getOrNull(1)
    val third = top3.getOrNull(2)

    Card(
        colors = CardDefaults.cardColors(containerColor = colors.cosmicSurface),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.cosmicSurfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "👑 HAFTALIK ŞAMPİYONLAR PODYUMU",
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                color = WarningAmber,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                // 2. Sıra (Gümüş)
                if (second != null) {
                    PodiumPillar(
                        user = second,
                        place = 2,
                        pillarHeight = 90.dp,
                        crownEmoji = "🥈",
                        accentColor = Color(0xFF94A3B8),
                        colors = colors,
                        modifier = Modifier.weight(1f)
                    )
                }

                // 1. Sıra (Altın - En Yüksek)
                if (first != null) {
                    PodiumPillar(
                        user = first,
                        place = 1,
                        pillarHeight = 120.dp,
                        crownEmoji = "👑",
                        accentColor = Color(0xFFF59E0B),
                        colors = colors,
                        modifier = Modifier.weight(1.15f)
                    )
                }

                // 3. Sıra (Bronz)
                if (third != null) {
                    PodiumPillar(
                        user = third,
                        place = 3,
                        pillarHeight = 75.dp,
                        crownEmoji = "🥉",
                        accentColor = Color(0xFFCD7F32),
                        colors = colors,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun PodiumPillar(
    user: LeaderboardUser,
    place: Int,
    pillarHeight: androidx.compose.ui.unit.Dp,
    crownEmoji: String,
    accentColor: Color,
    colors: AppCustomColors,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = crownEmoji, fontSize = 22.sp)
        Spacer(modifier = Modifier.height(4.dp))

        // Avatar
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(accentColor.copy(alpha = 0.2f), CircleShape)
                .border(2.dp, accentColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = user.avatarEmoji, fontSize = 22.sp)
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = user.username,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = "${user.score}p",
            fontWeight = FontWeight.Black,
            fontSize = 11.sp,
            color = accentColor
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Sütun (Pillar)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(pillarHeight)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(accentColor.copy(alpha = 0.6f), accentColor.copy(alpha = 0.2f))
                    ),
                    RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "#$place",
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                color = Color.White
            )
        }
    }
}

@Composable
fun LeaderboardUserRow(user: LeaderboardUser, colors: AppCustomColors) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (user.isCurrentUser) IndigoPrimary.copy(alpha = 0.15f) else colors.cosmicSurface
        ),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (user.isCurrentUser) 1.5.dp else 1.dp,
            color = if (user.isCurrentUser) IndigoPrimary else colors.cosmicSurfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank Number
            Text(
                text = "#${user.rank}",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = colors.textSecondary,
                modifier = Modifier.width(34.dp)
            )

            // Avatar Emoji & Flag
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(colors.cosmicSurfaceVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = user.avatarEmoji, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Name & League
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user.username,
                        fontWeight = if (user.isCurrentUser) FontWeight.Black else FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = if (user.isCurrentUser) IndigoPrimary else colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = user.countryFlag, fontSize = 12.sp)
                }
                Text(
                    text = "${user.league.badgeIcon} ${user.league.title}",
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )
            }

            // Score & IQ
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${user.score} Puan",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = WarningAmber
                )
                Text(
                    text = "IQ: ${String.format("%.1f", user.estimatedIq)}",
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )
            }
        }
    }
}
