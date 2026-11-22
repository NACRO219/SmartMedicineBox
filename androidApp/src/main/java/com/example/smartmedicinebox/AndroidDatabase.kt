package com.example.smartmedicinebox

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.smartmedicinebox.data.AppDatabase
import com.example.smartmedicinebox.data.Intake
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Opens the shared Room database on Android (same file as before: smartmed.db).
 * The data lives in the app's private storage, so it stays between launches and
 * phone restarts; Android Auto Backup restores it after a reinstall
 * (see res/xml/data_extraction_rules.xml).
 *
 * [name] is only changed by tests, so they never touch the user's real data.
 */
fun buildAndroidDatabase(
    context: Context,
    name: String = AppDatabase.DB_NAME
): AppDatabase =
    Room.databaseBuilder<AppDatabase>(
        context = context.applicationContext,
        name = context.applicationContext.getDatabasePath(name).absolutePath
    )
        .addCallback(LegacyPreferencesImport(context.applicationContext))
        .build()

/**
 * Runs once, when the database file is first created: copies the data the
 * app previously kept in SharedPreferences into the new tables, re-schedules
 * the reminders with the new ids, then clears the old preferences.
 */
private class LegacyPreferencesImport(
    private val context: Context
) : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        val medicinePrefs = context.getSharedPreferences("MedicineData", Context.MODE_PRIVATE)
        val historyPrefs = context.getSharedPreferences("MedicineHistory", Context.MODE_PRIVATE)

        val idsByName = mutableMapOf<String, Long>()

        // Medicines: "name|HH:mm|frequency" per line
        medicinePrefs.getString("medicines", "").orEmpty()
            .lineSequence()
            .filter { it.isNotBlank() }
            .forEach { line ->
                val parts = line.split("|", limit = 3)
                if (parts.size != 3) return@forEach

                val (name, time, frequency) = parts

                // Old alarms were keyed by name.hashCode(); replace them with id-based ones.
                MedicineReminder.cancelLegacy(context, name)

                val values = ContentValues().apply {
                    put("name", name)
                    put("time", time)
                    put("frequency", frequency)
                    put("createdAt", System.currentTimeMillis())
                }
                val id = db.insert("medicines", SQLiteDatabase.CONFLICT_NONE, values)

                if (id != -1L) {
                    idsByName[name] = id
                    MedicineReminder.schedule(context, id, name, time)
                }
            }

        // History: "name|dd/MM/yyyy HH:mm:ss" per line
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())

        historyPrefs.getString("history", "").orEmpty()
            .lineSequence()
            .filter { it.isNotBlank() }
            .forEach { line ->
                val parts = line.split("|", limit = 2)
                val name = parts[0]
                val takenAt = parts.getOrNull(1)
                    ?.let { runCatching { format.parse(it)?.time }.getOrNull() }
                    ?: return@forEach

                val values = ContentValues().apply {
                    val medicineId = idsByName[name]
                    if (medicineId != null) put("medicineId", medicineId) else putNull("medicineId")
                    put("medicineName", name)
                    putNull("scheduledTime")
                    put("takenAt", takenAt)
                    put("status", Intake.STATUS_TAKEN)
                }
                db.insert("intakes", SQLiteDatabase.CONFLICT_NONE, values)
            }

        // commit(), not apply(): the old data must be gone from disk before the
        // process can die, otherwise it would be imported a second time.
        medicinePrefs.edit().remove("medicines").commit()
        historyPrefs.edit().remove("history").commit()
    }
}
