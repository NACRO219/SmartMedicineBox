package com.example.smartmedicinebox.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface IntakeDao {

    /** Newest first. */
    @Query("SELECT * FROM intakes ORDER BY takenAt DESC")
    fun observeAll(): Flow<List<Intake>>

    @Insert
    suspend fun insert(intake: Intake): Long
}
