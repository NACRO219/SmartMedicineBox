package com.example.smartmedicinebox.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.time.Clock

/**
 * A medicine with its daily reminder time.
 * The [id] is used as the AlarmManager / notification request code,
 * so two medicines with the same name no longer collide.
 */
@Entity(tableName = "medicines")
data class Medicine(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    /** Reminder time in "HH:mm" format. */
    val time: String,
    val frequency: String,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds()
)
