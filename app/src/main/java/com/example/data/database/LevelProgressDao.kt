package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LevelProgressDao {
    @Query("SELECT * FROM level_progress ORDER BY levelId ASC")
    fun getAllProgress(): Flow<List<LevelProgressEntity>>

    @Query("SELECT * FROM level_progress WHERE levelId = :levelId LIMIT 1")
    fun getProgressById(levelId: Int): Flow<LevelProgressEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: LevelProgressEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(progresses: List<LevelProgressEntity>)

    @Query("UPDATE level_progress SET isUnlocked = 1 WHERE levelId = :levelId")
    suspend fun unlockLevel(levelId: Int)

    @Query("UPDATE level_progress SET isCompleted = :isCompleted, stars = :stars, highScore = :highScore, timeTakenSeconds = :timeTakenSeconds, completedAt = :completedAt WHERE levelId = :levelId")
    suspend fun updateLevelSuccess(
        levelId: Int,
        isCompleted: Boolean,
        stars: Int,
        highScore: Int,
        timeTakenSeconds: Int,
        completedAt: Long
    )

    @Query("UPDATE level_progress SET isCompleted = 0, stars = 0, highScore = 0, timeTakenSeconds = 0, isUnlocked = CASE WHEN levelId = 1 THEN 1 ELSE 0 END")
    suspend fun resetProgress()
}
