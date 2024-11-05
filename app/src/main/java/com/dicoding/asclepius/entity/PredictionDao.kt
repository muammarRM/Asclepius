package com.dicoding.asclepius.entity

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PredictionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(prediction: Prediction)

    @Query("SELECT * FROM predictions ORDER BY id DESC")
    suspend fun getAllPredictions(): List<Prediction>
}
