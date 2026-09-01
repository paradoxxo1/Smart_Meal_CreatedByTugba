package com.example.data.repository

import com.example.data.model.LeaderboardUser
import com.example.data.model.UserLeague
import kotlin.random.Random

object LeaderboardRepository {

    // Gerçekçi ve ilgi çekici küresel yarışmacı havuzu
    private val globalCompetitors = listOf(
        Triple("ZekaŞampiyonu_TR", "🧙‍♂️", "🇹🇷") to 28450,
        Triple("BrainMaster_99", "👑", "🇩🇪") to 26100,
        Triple("Eda_AkılKüpü", "👩‍🔬", "🇹🇷") to 24320,
        Triple("MindVoyager", "🚀", "🇬🇧") to 22980,
        Triple("Kuantum_Düşünür", "⚡", "🇹🇷") to 21450,
        Triple("PuzzleNinja", "🥷", "🇯🇵") to 19800,
        Triple("MatematikCanavarı", "🦁", "🇦🇿") to 18420,
        Triple("SynapseSpark", "🧠", "🇺🇸") to 17150,
        Triple("DerinMantık", "🦉", "🇹🇷") to 15900,
        Triple("CyberEinstein", "🤖", "🇰🇷") to 14600,
        Triple("Büşra_T", "🎯", "🇹🇷") to 13200,
        Triple("LogicOverload", "🧩", "🇫🇷") to 11950,
        Triple("AteşliFikirler", "🔥", "🇹🇷") to 10500,
        Triple("AlphaMind", "🐺", "🇮🇹") to 9200,
        Triple("Mert_AkılOyunları", "🎓", "🇹🇷") to 8100,
        Triple("Sherlock_V2", "🔍", "🇬🇧") to 7200,
        Triple("Selin_Z", "⭐", "🇹🇷") to 6400,
        Triple("NexusThinker", "🌐", "🇪🇸") to 5500,
        Triple("HızlıZihin", "🐆", "🇹🇷") to 4600,
        Triple("CodeBreaker", "💻", "🇨🇦") to 3800,
        Triple("YeniDüşünür", "🌱", "🇹🇷") to 2400,
        Triple("AcemiUsta", "🐣", "🇹🇷") to 1200
    )

    fun getLeaderboard(
        userScore: Int,
        userIq: Double,
        userSolvedCount: Int,
        username: String = "Sen (Senin Profilin)"
    ): List<LeaderboardUser> {
        val userLeague = UserLeague.getLeagueForScore(userScore)

        val currentUser = LeaderboardUser(
            rank = 1, // dinamik hesaplanacak
            username = username,
            avatarEmoji = "⭐",
            score = userScore,
            estimatedIq = userIq,
            solvedCount = userSolvedCount,
            league = userLeague,
            countryFlag = "🇹🇷",
            isCurrentUser = true,
            titleBadge = userLeague.title
        )

        val allUsers = mutableListOf<LeaderboardUser>()
        allUsers.add(currentUser)

        // Rakipleri ekle
        for ((competitor, score) in globalCompetitors) {
            val (name, emoji, flag) = competitor
            val competitorIq = 90.0 + (score / 350.0).coerceAtMost(55.0)
            val solved = (score / 450).coerceIn(5, 65)
            val league = UserLeague.getLeagueForScore(score)

            allUsers.add(
                LeaderboardUser(
                    rank = 0,
                    username = name,
                    avatarEmoji = emoji,
                    score = score,
                    estimatedIq = competitorIq,
                    solvedCount = solved,
                    league = league,
                    countryFlag = flag,
                    isCurrentUser = false,
                    titleBadge = league.title
                )
            )
        }

        // Puana göre büyükten küçüğe sırala
        val sortedList = allUsers.sortedByDescending { it.score }

        // Sıralama numaralarını (rank) güncelle
        return sortedList.mapIndexed { index, user ->
            user.copy(rank = index + 1)
        }
    }
}
