package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "level_progress")
data class LevelProgressEntity(
    @PrimaryKey val levelId: Int,
    val isUnlocked: Boolean,
    val isCompleted: Boolean,
    val stars: Int,
    val highScore: Int,
    val timeTakenSeconds: Int,
    val completedAt: Long = 0L
)
