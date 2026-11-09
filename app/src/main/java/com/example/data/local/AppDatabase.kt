package com.example.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "feedback_records")
data class FeedbackEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pilgrimName: String,
    val category: String,
    val rating: Int,
    val comment: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_preferences")
data class UserProgressEntity(
    @PrimaryKey val key: String,
    val stringValue: String
)

@Dao
interface FeedbackDao {
    @Query("SELECT * FROM feedback_records ORDER BY timestamp DESC")
    fun getAllFeedback(): Flow<List<FeedbackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedback(feedback: FeedbackEntity): Long

    @Query("DELETE FROM feedback_records WHERE id = :id")
    suspend fun deleteFeedback(id: Long)
}

@Dao
interface UserProgressDao {
    @Query("SELECT stringValue FROM user_preferences WHERE key = :key")
    suspend fun getValue(key: String): String?

    @Query("SELECT * FROM user_preferences")
    fun getAllPreferences(): Flow<List<UserProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePreference(item: UserProgressEntity)
}

@Database(entities = [FeedbackEntity::class, UserProgressEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun feedbackDao(): FeedbackDao
    abstract fun userProgressDao(): UserProgressDao
}
