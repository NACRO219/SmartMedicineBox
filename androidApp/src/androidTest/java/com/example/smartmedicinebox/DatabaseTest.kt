package com.example.smartmedicinebox

import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.smartmedicinebox.data.AppDatabase
import com.example.smartmedicinebox.data.Intake
import com.example.smartmedicinebox.data.Medicine
import com.example.smartmedicinebox.domain.MedicineRepository
import com.example.smartmedicinebox.domain.ReminderScheduler
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Runs the real Room database on a device / emulator. */
@RunWith(AndroidJUnit4::class)
class DatabaseTest {

    private val context: Context =
        InstrumentationRegistry.getInstrumentation().targetContext

    private lateinit var db: AppDatabase
    private val scheduler = FakeScheduler()
    private lateinit var repository: MedicineRepository

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder<AppDatabase>(context).build()
        repository = MedicineRepository(db, scheduler)
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun addMedicineSavesItAndSchedulesReminder() = runBlocking {
        val saved = repository.addMedicine("Doliprane", "08:30", "Daily")

        assertTrue(saved.id > 0)
        assertEquals(saved, db.medicineDao().getById(saved.id))
        assertEquals(listOf(saved.id), scheduler.scheduled.map { it.id })
    }

    @Test
    fun medicinesAreSortedByTimeThenName() = runBlocking {
        repository.addMedicine("B", "20:00", "Daily")
        repository.addMedicine("Z", "08:00", "Daily")
        repository.addMedicine("A", "08:00", "Daily")

        val names = repository.medicines.first().map { it.name }
        assertEquals(listOf("A", "Z", "B"), names)
    }

    @Test
    fun deletingMedicineKeepsHistoryAndUnlinksIt() = runBlocking {
        val med = repository.addMedicine("Aspirin", "09:00", "Daily")
        repository.recordTaken(med.id, med.name, med.time)

        repository.deleteMedicine(med)

        assertNull(db.medicineDao().getById(med.id))
        assertEquals(listOf(med.id), scheduler.cancelled)

        val history = repository.history.first()
        assertEquals(1, history.size)
        assertEquals("Aspirin", history[0].medicineName)
        assertNull("foreign key should be SET NULL", history[0].medicineId)
    }

    @Test
    fun recordTakenForMissingMedicineIsStoredUnlinked() = runBlocking {
        repository.recordTaken(medicineId = 999, medicineName = "Ghost", scheduledTime = "10:00")

        val entry = repository.history.first().single()
        assertNull(entry.medicineId)
        assertEquals("Ghost", entry.medicineName)
        assertEquals(Intake.STATUS_TAKEN, entry.status)
    }

    @Test
    fun historyIsNewestFirst() = runBlocking {
        db.intakeDao().insert(Intake(medicineId = null, medicineName = "old", scheduledTime = null, takenAt = 1_000))
        db.intakeDao().insert(Intake(medicineId = null, medicineName = "new", scheduledTime = null, takenAt = 2_000))

        assertEquals(listOf("new", "old"), repository.history.first().map { it.medicineName })
    }

    /**
     * Opens a database file the same way the app does and checks that data saved by
     * the very old version of the app (SharedPreferences) is imported once.
     * Uses its own file name, so the user's real smartmed.db is never touched.
     */
    @Test
    fun legacyPreferencesAreImportedOnFirstCreate() = runBlocking {
        val realDbBefore = context.getDatabasePath(AppDatabase.DB_NAME).let { it.exists() to it.lastModified() }
        context.deleteDatabase(TEST_DB)
        context.getSharedPreferences("MedicineData", Context.MODE_PRIVATE).edit()
            .putString("medicines", "Vitamin C|07:15|Daily\nbroken line\nIron|21:00|Daily")
            .commit()
        context.getSharedPreferences("MedicineHistory", Context.MODE_PRIVATE).edit()
            .putString("history", "Vitamin C|01/11/2026 07:16:00\nUnknown|not a date")
            .commit()

        val fileDb = buildAndroidDatabase(context, TEST_DB)
        try {
            val medicines = fileDb.medicineDao().getAll().sortedBy { it.time }
            assertEquals(listOf("Vitamin C", "Iron"), medicines.map { it.name })

            val history = fileDb.intakeDao().observeAll().first()
            assertEquals(1, history.size)
            assertEquals(medicines[0].id, history[0].medicineId)

            val prefs = context.getSharedPreferences("MedicineData", Context.MODE_PRIVATE)
            assertNull("old data should be cleared", prefs.getString("medicines", null))
        } finally {
            fileDb.medicineDao().getAll().forEach { MedicineReminder.cancel(context, it.id) }
            fileDb.close()
            context.deleteDatabase(TEST_DB)
            // Don't leave sample data behind for the real app to import.
            context.getSharedPreferences("MedicineData", Context.MODE_PRIVATE).edit().clear().commit()
            context.getSharedPreferences("MedicineHistory", Context.MODE_PRIVATE).edit().clear().commit()
        }

        val realDbAfter = context.getDatabasePath(AppDatabase.DB_NAME).let { it.exists() to it.lastModified() }
        assertEquals("the user's real database must not be touched", realDbBefore, realDbAfter)
    }

    /** Data written through one database instance is still there after reopening the file. */
    @Test
    fun dataSurvivesClosingAndReopeningTheDatabase() = runBlocking {
        context.deleteDatabase(TEST_DB)
        try {
            val first = buildAndroidDatabase(context, TEST_DB)
            val repo = MedicineRepository(first, scheduler)
            val kept = repo.addMedicine("Kept", "12:00", "Daily")
            val removed = repo.addMedicine("Removed", "13:00", "Daily")
            repo.recordTaken(kept.id, kept.name, kept.time)
            repo.deleteMedicine(removed)
            first.close()

            val reopened = buildAndroidDatabase(context, TEST_DB)
            try {
                assertEquals(listOf("Kept"), reopened.medicineDao().getAll().map { it.name })
                assertEquals(1, reopened.intakeDao().observeAll().first().size)
            } finally {
                reopened.close()
            }
        } finally {
            context.deleteDatabase(TEST_DB)
        }
    }

    private companion object {
        const val TEST_DB = "test-smartmed.db"
    }

    private class FakeScheduler : ReminderScheduler {
        val scheduled = mutableListOf<Medicine>()
        val cancelled = mutableListOf<Long>()
        override fun schedule(medicine: Medicine) { scheduled += medicine }
        override fun cancel(medicineId: Long) { cancelled += medicineId }
    }
}
