package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class GameMode(
    val title: String,
    val subtitle: String,
    val description: String,
    val emoji: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val backgroundColorTop: Color,
    val backgroundColorBottom: Color,
    val surfaceColor: Color
) {
    CLASSIC(
        title = "Klasik Zeka",
        subtitle = "Süresiz & Rahat",
        description = "50 farklı seviyede sakin ve konsantre zeka egzersizi yapın.",
        emoji = "🧠",
        primaryColor = Color(0xFF6366F1),
        secondaryColor = Color(0xFFA855F7),
        backgroundColorTop = Color(0xFF0F172A),
        backgroundColorBottom = Color(0xFF1E1B4B),
        surfaceColor = Color(0xFF1E293B)
    ),
    ZOMBIE(
        title = "Zombi İstilası",
        subtitle = "Beynini Kurtar!",
        description = "Zombiler yaklaşmadan soruları çöz! 3 Beyin Canı (🧠) ve 25 sn süre.",
        emoji = "🧟",
        primaryColor = Color(0xFF10B981), // Toxic Emerald
        secondaryColor = Color(0xFF84CC16), // Toxic Lime
        backgroundColorTop = Color(0xFF09140E), // Toxic Dark Green
        backgroundColorBottom = Color(0xFF160808), // Dark Blood
        surfaceColor = Color(0xFF132219)
    ),
    BOMB_DEFUSAL(
        title = "Bomba İmha",
        subtitle = "Zamanla Yarış!",
        description = "Saatli bombayı durdur! Her doğru cevap bir teli keser, yanlış cevap süreyi azaltır.",
        emoji = "💣",
        primaryColor = Color(0xFFEF4444), // Crimson Danger
        secondaryColor = Color(0xFFF97316), // Hazard Orange
        backgroundColorTop = Color(0xFF180808), // Dark Hazard Red
        backgroundColorBottom = Color(0xFF0F0F12), // Carbon Black
        surfaceColor = Color(0xFF241212)
    ),
    SPACE_ESCAPE(
        title = "Uzay Görevi",
        subtitle = "Roketi Fırlat!",
        description = "Sistemleri onararak roketi uzaya fırlat! Yörüngeye ulaşmak için irtifa kazan.",
        emoji = "🚀",
        primaryColor = Color(0xFF06B6D4), // Cyan Thruster
        secondaryColor = Color(0xFF3B82F6), // Sci-fi Blue
        backgroundColorTop = Color(0xFF060B18), // Deep Cosmos
        backgroundColorBottom = Color(0xFF0F172A), // Dark Navy
        surfaceColor = Color(0xFF0F2238)
    )
}
