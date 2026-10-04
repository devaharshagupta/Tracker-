package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gate_topics")
data class GateTopic(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val subject: String,
  val topicName: String,
  val weightage: String = "High", // High, Medium, Low
  val status: String = "Not Started", // Not Started, Theory, Practice, PYQs Done, Revised 1x, Revised 2x, Mastered
  val notes: String = "",
  val lastRevisedDate: Long? = null,
  val revisionCount: Int = 0
)

@Entity(tableName = "gate_mock_tests")
data class GateMockTest(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val testName: String,
  val testType: String = "Full Length Mock", // Full Length Mock, Subject Test, Sectional Quiz
  val marksScored: Float,
  val totalMarks: Float = 100f,
  val negativeMarks: Float = 0f,
  val accuracyPercent: Float = 0f,
  val weakAreas: String = "",
  val date: Long = System.currentTimeMillis()
)

@Entity(tableName = "study_sessions")
data class StudySession(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val category: String, // "GATE Prep", "College Work", "Krishna Sadhana"
  val subjectOrTopic: String,
  val durationMinutes: Int,
  val date: String, // YYYY-MM-DD
  val timestamp: Long = System.currentTimeMillis()
)
