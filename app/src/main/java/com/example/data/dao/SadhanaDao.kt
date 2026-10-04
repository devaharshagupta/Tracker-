package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.SadhanaEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface SadhanaDao {
  @Query("SELECT * FROM sadhana_entries WHERE date = :date LIMIT 1")
  fun getSadhanaForDate(date: String): Flow<SadhanaEntry?>

  @Query("SELECT * FROM sadhana_entries ORDER BY date DESC LIMIT 30")
  fun getRecentSadhanaLogs(): Flow<List<SadhanaEntry>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdateSadhana(entry: SadhanaEntry)

  @Query("SELECT COUNT(*) FROM sadhana_entries WHERE japaRounds >= targetRounds")
  fun getTargetCompletedDaysCount(): Flow<Int>
}
