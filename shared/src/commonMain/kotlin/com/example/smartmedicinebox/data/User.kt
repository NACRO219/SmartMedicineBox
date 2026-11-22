package com.example.smartmedicinebox.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * An account stored on this phone.
 *
 * The password itself is never stored: only a PBKDF2-HMAC-SHA256 hash of it,
 * with its own random salt (see domain/PasswordHasher.kt).
 *
 * Email and username are unique and compared without case
 * ("Sara@Mail.com" and "sara@mail.com" are the same account).
 */
@Entity(
    tableName = "users",
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["username"], unique = true)
    ]
)
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** Stored trimmed and lowercase. */
    @ColumnInfo(collate = ColumnInfo.NOCASE)
    val email: String,
    /** Stored as typed (for display); uniqueness ignores case. */
    @ColumnInfo(collate = ColumnInfo.NOCASE)
    val username: String,
    /** Hex-encoded PBKDF2 output. */
    val passwordHash: String,
    /** Hex-encoded random salt. */
    val passwordSalt: String,
    /** Stored per account so the cost can be raised later without breaking old logins. */
    val passwordIterations: Int,
    val createdAt: Long
)

/**
 * Who is logged in on this phone. At most one row (id = 1);
 * no row means nobody is logged in. Deleting the user logs them out.
 */
@Entity(
    tableName = "session",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class Session(
    @PrimaryKey
    val id: Int = SINGLE_ROW_ID,
    val userId: Long,
    val loggedInAt: Long
) {
    companion object {
        const val SINGLE_ROW_ID = 1
    }
}
