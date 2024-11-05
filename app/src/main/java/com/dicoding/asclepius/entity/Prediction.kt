package com.dicoding.asclepius.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "predictions")
data class Prediction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val imageUri: String,
    val result: String,
    val confidence: Float
)
