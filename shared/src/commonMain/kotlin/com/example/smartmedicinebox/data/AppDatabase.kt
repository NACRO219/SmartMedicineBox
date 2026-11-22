package com.example.smartmedicinebox.data

import androidx.room.AutoMigration
import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

/**
 * Shared database definition (Android + iPhone).
 * Each platform opens the file itself:
 *  - Android: androidApp/.../AndroidDatabase.kt
 *  - iPhone:  shared/src/iosMain/.../DatabaseBuilder.ios.kt
 *
 * Versions (schemas in shared/schemas/):
 *  1 - medicines + intakes
 *  2 - adds users + session (accounts). Existing medicines and history are kept.
 */
@Database(
    entities = [Medicine::class, Intake::class, User::class, Session::class],
    version = 2,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ]
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun medicineDao(): MedicineDao
    abstract fun intakeDao(): IntakeDao
    abstract fun userDao(): UserDao

    companion object {
        const val DB_NAME = "smartmed.db"
    }
}

// Room generates the "actual" implementation for every platform.
@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
