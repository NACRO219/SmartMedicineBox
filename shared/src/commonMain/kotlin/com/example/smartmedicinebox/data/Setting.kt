package com.example.smartmedicinebox.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** App preferences as key / value pairs (e.g. "theme" -> "dark"). */
@Entity(tableName = "settings")
data class Setting(
    @PrimaryKey val key: String,
    val value: String
)

@Dao
interface SettingDao {

    /** The stored value, or null if never set. Updates live. */
    @Query("SELECT value FROM settings WHERE `key` = :key")
    fun observe(key: String): Flow<String?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(setting: Setting)
}
