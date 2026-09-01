package com.example.data.repository

import com.example.data.database.LevelProgressDao
import com.example.data.database.LevelProgressEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class LevelRepository(private val dao: LevelProgressDao) {

    val allProgress: Flow<List<LevelProgressEntity>> = dao.getAllProgress()

    // Initialize the 50 levels if not already populated
    suspend fun checkAndInitializeLevels() {
        val currentList = dao.getAllProgress().map { it }.firstOrNull() ?: emptyList()
        if (currentList.size < 50) {
            val defaultProgress = (1..50).map { id ->
                LevelProgressEntity(
                    levelId = id,
                    isUnlocked = id == 1, // Unlock only level 1 initially
                    isCompleted = false,
                    stars = 0,
                    highScore = 0,
                    timeTakenSeconds = 0,
                    completedAt = 0L
                )
            }
            dao.insertAll(defaultProgress)
        }
    }

    fun getProgressForLevel(levelId: Int): Flow<LevelProgressEntity?> {
        return dao.getProgressById(levelId)
    }

    suspend fun saveLevelCompletion(levelId: Int, stars: Int, score: Int, timeTaken: Int) {
        dao.updateLevelSuccess(
            levelId = levelId,
            isCompleted = true,
            stars = stars,
            highScore = score,
            timeTakenSeconds = timeTaken,
            completedAt = System.currentTimeMillis()
        )
        // Auto unlock next level
        if (levelId < 50) {
            dao.unlockLevel(levelId + 1)
        }
    }

    suspend fun resetAllProgress() {
        dao.resetProgress()
    }
}
