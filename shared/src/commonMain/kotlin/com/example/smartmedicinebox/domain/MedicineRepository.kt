package com.example.smartmedicinebox.domain

import com.example.smartmedicinebox.data.AppDatabase
import com.example.smartmedicinebox.data.Intake
import com.example.smartmedicinebox.data.Medicine
import kotlinx.coroutines.flow.Flow
import kotlin.time.Clock

/**
 * Single entry point for medicines and history.
 * Keeps the database and the reminders in sync.
 */
class MedicineRepository(
    private val database: AppDatabase,
    private val scheduler: ReminderScheduler
) {

    val medicines: Flow<List<Medicine>> = database.medicineDao().observeAll()

    /** Newest first. */
    val history: Flow<List<Intake>> = database.intakeDao().observeAll()

    suspend fun addMedicine(name: String, time: String, frequency: String): Medicine {
        val medicine = Medicine(name = name, time = time, frequency = frequency)
        val id = database.medicineDao().insert(medicine)
        val saved = medicine.copy(id = id)
        scheduler.schedule(saved)
        return saved
    }

    /** Removes the medicine and its reminder; its history entries are kept. */
    suspend fun deleteMedicine(medicine: Medicine) {
        scheduler.cancel(medicine.id)
        database.medicineDao().delete(medicine)
    }

    suspend fun recordTaken(medicineId: Long?, medicineName: String, scheduledTime: String?) {
        // The medicine may have been deleted after the reminder was shown;
        // keep the history entry but don't link it to a missing row.
        val existingId = medicineId?.takeIf {
            database.medicineDao().getById(it) != null
        }

        database.intakeDao().insert(
            Intake(
                medicineId = existingId,
                medicineName = medicineName,
                scheduledTime = scheduledTime,
                takenAt = Clock.System.now().toEpochMilliseconds()
            )
        )
    }

    /** Re-creates every reminder (e.g. after the phone restarts). */
    suspend fun rescheduleAll() {
        database.medicineDao().getAll().forEach { scheduler.schedule(it) }
    }
}
