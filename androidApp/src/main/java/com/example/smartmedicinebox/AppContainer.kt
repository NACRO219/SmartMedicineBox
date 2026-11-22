package com.example.smartmedicinebox

import android.content.Context
import com.example.smartmedicinebox.data.AppDatabase
import com.example.smartmedicinebox.domain.AuthRepository
import com.example.smartmedicinebox.domain.MedicineRepository
import com.example.smartmedicinebox.domain.SettingsRepository

/**
 * Creates the shared objects once for the whole Android app
 * (activity, broadcast receivers, services).
 */
object AppContainer {

    @Volatile
    private var database: AppDatabase? = null

    private fun database(context: Context): AppDatabase =
        database ?: synchronized(this) {
            database ?: buildAndroidDatabase(context).also { database = it }
        }

    @Volatile
    private var repository: MedicineRepository? = null

    fun repository(context: Context): MedicineRepository =
        repository ?: synchronized(this) {
            repository ?: MedicineRepository(
                database = database(context),
                scheduler = AndroidReminderScheduler(context.applicationContext)
            ).also { repository = it }
        }

    @Volatile
    private var authRepository: AuthRepository? = null

    fun authRepository(context: Context): AuthRepository =
        authRepository ?: synchronized(this) {
            authRepository ?: AuthRepository(database(context))
                .also { authRepository = it }
        }

    @Volatile
    private var settings: SettingsRepository? = null

    fun settings(context: Context): SettingsRepository =
        settings ?: synchronized(this) {
            settings ?: SettingsRepository(database(context))
                .also { settings = it }
        }

    @Volatile
    private var box: AndroidBoxConnection? = null

    fun box(context: Context): AndroidBoxConnection =
        box ?: synchronized(this) {
            box ?: AndroidBoxConnection(context.applicationContext)
                .also { box = it }
        }
}
