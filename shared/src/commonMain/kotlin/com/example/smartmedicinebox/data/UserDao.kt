package com.example.smartmedicinebox.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    /** Fails if the email or username is already used (unique indices). */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: User): Long

    /** Case-insensitive (column collation NOCASE). */
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): User?

    /** Case-insensitive (column collation NOCASE). */
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun findByUsername(username: String): User?

    /** The logged-in user, or null when logged out. Updates live. */
    @Query(
        """
        SELECT users.* FROM users
        INNER JOIN session ON session.userId = users.id
        WHERE session.id = 1
        """
    )
    fun observeLoggedInUser(): Flow<User?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSession(session: Session)

    @Query("DELETE FROM session")
    suspend fun clearSession()
}
