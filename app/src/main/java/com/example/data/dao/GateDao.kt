package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.GateMockTest
import com.example.data.entity.GateTopic
import com.example.data.entity.StudySession
import kotlinx.coroutines.flow.Flow

@Dao
interface GateDao {
  // Topics
  @Query("SELECT * FROM gate_topics ORDER BY subject ASC, id ASC")
  fun getAllTopics(): Flow<List<GateTopic>>

  @Query("SELECT DISTINCT subject FROM gate_topics ORDER BY subject ASC")
  fun getAllSubjects(): Flow<List<String>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTopic(topic: GateTopic): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTopics(topics: List<GateTopic>)

  @Update
  suspend fun updateTopic(topic: GateTopic)

  @Delete
  suspend fun deleteTopic(topic: GateTopic)

  @Query("UPDATE gate_topics SET status = :newStatus, lastRevisedDate = :revisedDate, revisionCount = revisionCount + 1 WHERE id = :id")
  suspend fun updateTopicStatus(id: Long, newStatus: String, revisedDate: Long)

  // Mock Tests
  @Query("SELECT * FROM gate_mock_tests ORDER BY date DESC")
  fun getAllMockTests(): Flow<List<GateMockTest>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMockTest(test: GateMockTest): Long

  @Delete
  suspend fun deleteMockTest(test: GateMockTest)

  // Study Sessions
  @Query("SELECT * FROM study_sessions WHERE date = :date ORDER BY timestamp DESC")
  fun getSessionsForDate(date: String): Flow<List<StudySession>>

  @Query("SELECT * FROM study_sessions ORDER BY timestamp DESC LIMIT 50")
  fun getRecentSessions(): Flow<List<StudySession>>

  @Query("SELECT SUM(durationMinutes) FROM study_sessions WHERE date = :date")
  fun getTotalMinutesForDate(date: String): Flow<Int?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertStudySession(session: StudySession): Long
}
