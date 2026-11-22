package com.example.smartmedicinebox.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One history entry: a dose of a medicine.
 *
 * [medicineName] is stored as a copy so the history stays readable after the
 * medicine is deleted (the foreign key is then set to null, history is kept).
 */
@Entity(
    tableName = "intakes",
    foreignKeys = [
        ForeignKey(
            entity = Medicine::class,
            parentColumns = ["id"],
            childColumns = ["medicineId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("medicineId"),
        Index("takenAt")
    ]
)
data class Intake(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val medicineId: Long?,
    val medicineName: String,
    /** The planned time ("HH:mm") of the reminder that was confirmed, if known. */
    val scheduledTime: String?,
    /** Epoch millis when the dose was confirmed. */
    val takenAt: Long,
    val status: String = STATUS_TAKEN
) {
    companion object {
        const val STATUS_TAKEN = "TAKEN"
    }
}
