package com.example.data.model

import com.example.R

enum class UserLeague(
    val title: String,
    val minScore: Int,
    val badgeIcon: String,
    val colorHex: Long
) {
    BRONZE("Bronz Lig", 0, "🥉", 0xFFCD7F32),
    SILVER("Gümüş Lig", 3000, "🥈", 0xFF94A3B8),
    GOLD("Altın Lig", 7500, "🥇", 0xFFF59E0B),
    DIAMOND("Elmas Lig", 15000, "💎", 0xFF38BDF8),
    MASTER("Zeka Ustası", 25000, "👑", 0xFFA855F7);

    companion object {
        fun getLeagueForScore(score: Int): UserLeague {
            return when {
                score >= MASTER.minScore -> MASTER
                score >= DIAMOND.minScore -> DIAMOND
                score >= GOLD.minScore -> GOLD
                score >= SILVER.minScore -> SILVER
                else -> BRONZE
            }
        }
    }
}

data class LeaderboardUser(
    val rank: Int,
    val username: String,
    val avatarEmoji: String,
    val score: Int,
    val estimatedIq: Double,
    val solvedCount: Int,
    val league: UserLeague,
    val countryFlag: String,
    val isCurrentUser: Boolean = false,
    val titleBadge: String = ""
)
