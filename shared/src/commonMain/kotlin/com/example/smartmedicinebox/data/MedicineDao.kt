package com.example.smartmedicinebox.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicineDao {

    @Query("SELECT * FROM medicines ORDER BY time ASC, name ASC")
    fun observeAll(): Flow<List<Medicine>>

    @Query("SELECT * FROM medicines")
    suspend fun getAll(): List<Medicine>

    @Query("SELECT * FROM medicines WHERE id = :id")
    suspend fun getById(id: Long): Medicine?

    @Insert
    suspend fun insert(medicine: Medicine): Long

    @Delete
    suspend fun delete(medicine: Medicine)
}
