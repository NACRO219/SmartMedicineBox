package com.example.smartmedicinebox

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.smartmedicinebox.data.AppDatabase
import com.example.smartmedicinebox.domain.AuthRepository
import com.example.smartmedicinebox.domain.AuthResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Accounts on a real device / emulator. Never touches the user's real smartmed.db. */
@RunWith(AndroidJUnit4::class)
class AuthTest {

    private val context: Context =
        InstrumentationRegistry.getInstrumentation().targetContext

    private lateinit var db: AppDatabase
    private lateinit var auth: AuthRepository

    private val strong = "Sm@rtMed2026"

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder<AppDatabase>(context).build()
        auth = AuthRepository(db)
    }

    @After
    fun tearDown() = db.close()

    private fun AuthResult.errorOrNull() = (this as? AuthResult.Error)?.message

    @Test
    fun signUpCreatesAccountAndLogsIn() = runBlocking {
        val result = auth.signUp("  Sara.B@Mail.com ", "Sara_B", strong, strong)

        val user = (result as AuthResult.Success).user
        assertEquals("sara.b@mail.com", user.email)
        assertEquals("Sara_B", user.username)
        assertEquals(user.id, auth.currentUser.first()?.id)
    }

    @Test
    fun passwordIsNeverStoredInClear() = runBlocking {
        auth.signUp("sara@mail.com", "sara", strong, strong)
        val user = db.userDao().findByEmail("sara@mail.com")!!

        assertFalse(user.passwordHash.contains(strong))
        assertFalse(user.passwordSalt.contains(strong))
        assertEquals(64, user.passwordHash.length)
        assertTrue(user.passwordIterations >= 100_000)
    }

    @Test
    fun signUpRejectsWeakPasswordBadEmailBadUsernameAndMismatch() = runBlocking {
        assertNotNull(auth.signUp("sara@mail.com", "sara", "password", "password").errorOrNull())
        assertNotNull(auth.signUp("sara@mail.com", "sara", "Password1", "Password1").errorOrNull()) // no symbol
        assertNotNull(auth.signUp("not-an-email", "sara", strong, strong).errorOrNull())
        assertNotNull(auth.signUp("sara@mail.com", "s", strong, strong).errorOrNull())
        assertEquals(
            "Les mots de passe ne correspondent pas.",
            auth.signUp("sara@mail.com", "sara", strong, strong + "x").errorOrNull()
        )
        assertNull(auth.currentUser.first())
        assertNull(db.userDao().findByEmail("sara@mail.com"))
    }

    @Test
    fun emailAndUsernameMustBeUniqueIgnoringCase() = runBlocking {
        auth.signUp("sara@mail.com", "Sara", strong, strong)

        assertEquals(
            "Un compte existe déjà avec cette adresse e-mail.",
            auth.signUp("SARA@MAIL.COM", "other", strong, strong).errorOrNull()
        )
        assertEquals(
            "Ce nom d'utilisateur est déjà pris.",
            auth.signUp("other@mail.com", "sARa", strong, strong).errorOrNull()
        )
    }

    @Test
    fun logInWithEmailOrUsernameAnyCase() = runBlocking {
        val id = (auth.signUp("sara@mail.com", "Sara", strong, strong) as AuthResult.Success).user.id
        auth.logOut()
        assertNull(auth.currentUser.first())

        assertEquals(id, (auth.logIn("SARA@mail.com", strong) as AuthResult.Success).user.id)
        auth.logOut()
        assertEquals(id, (auth.logIn("  sara ", strong) as AuthResult.Success).user.id)
        assertEquals(id, auth.currentUser.first()?.id)
    }

    @Test
    fun wrongPasswordOrUnknownUserGivesTheSameError() = runBlocking {
        auth.signUp("sara@mail.com", "sara", strong, strong)
        auth.logOut()

        val wrongPassword = auth.logIn("sara", "Sm@rtMed2025").errorOrNull()
        val unknownUser = auth.logIn("nobody", strong).errorOrNull()
        assertEquals("Identifiant ou mot de passe incorrect.", wrongPassword)
        assertEquals(wrongPassword, unknownUser)
        assertNull(auth.currentUser.first())
    }

    @Test
    fun loginIsRememberedAfterReopeningTheDatabase() = runBlocking {
        context.deleteDatabase(TEST_DB)
        try {
            val first = buildAndroidDatabase(context, TEST_DB)
            AuthRepository(first).signUp("sara@mail.com", "sara", strong, strong)
            first.close()

            val reopened = buildAndroidDatabase(context, TEST_DB)
            try {
                assertEquals("sara", AuthRepository(reopened).currentUser.first()?.username)
            } finally {
                reopened.close()
            }
        } finally {
            context.deleteDatabase(TEST_DB)
        }
    }

    /**
     * A phone that already has the app (database version 1, before accounts)
     * gets the update: medicines and history must still be there.
     */
    @Test
    fun upgradeFromVersion1KeepsMedicinesAndHistory() = runBlocking {
        context.deleteDatabase(TEST_DB)
        try {
            createVersion1Database(TEST_DB)

            val upgraded = buildAndroidDatabase(context, TEST_DB)
            try {
                assertEquals(listOf("Doliprane"), upgraded.medicineDao().getAll().map { it.name })
                val history = upgraded.intakeDao().observeAll().first()
                assertEquals(1, history.size)
                assertEquals("Doliprane", history[0].medicineName)

                // The new tables work too.
                val auth = AuthRepository(upgraded)
                assertNull(auth.currentUser.first())
                assertTrue(auth.signUp("sara@mail.com", "sara", strong, strong) is AuthResult.Success)
            } finally {
                upgraded.close()
            }
        } finally {
            context.deleteDatabase(TEST_DB)
        }
    }

    /** Exactly what version 1 of the app created (shared/schemas/.../1.json). */
    private fun createVersion1Database(name: String) {
        val file = context.getDatabasePath(name)
        file.parentFile?.mkdirs()
        SQLiteDatabase.openOrCreateDatabase(file, null).use { db ->
            db.execSQL("CREATE TABLE IF NOT EXISTS `medicines` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `time` TEXT NOT NULL, `frequency` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)")
            db.execSQL("CREATE TABLE IF NOT EXISTS `intakes` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `medicineId` INTEGER, `medicineName` TEXT NOT NULL, `scheduledTime` TEXT, `takenAt` INTEGER NOT NULL, `status` TEXT NOT NULL, FOREIGN KEY(`medicineId`) REFERENCES `medicines`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL )")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_intakes_medicineId` ON `intakes` (`medicineId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_intakes_takenAt` ON `intakes` (`takenAt`)")
            db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
            db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '645e173206f4a0433997811be5617354')")
            db.execSQL("INSERT INTO medicines (name, time, frequency, createdAt) VALUES ('Doliprane', '08:00', 'Daily', 0)")
            db.execSQL("INSERT INTO intakes (medicineId, medicineName, scheduledTime, takenAt, status) VALUES (1, 'Doliprane', '08:00', 1000, 'TAKEN')")
            db.version = 1
        }
    }

    private companion object {
        const val TEST_DB = "test-auth-smartmed.db"
    }
}
