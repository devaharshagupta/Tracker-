package com.example.data.repository

import com.example.data.dao.CollegeDao
import com.example.data.dao.GateDao
import com.example.data.dao.SadhanaDao
import com.example.data.entity.CollegeAttendance
import com.example.data.entity.CollegeTask
import com.example.data.entity.GateMockTest
import com.example.data.entity.GateTopic
import com.example.data.entity.SadhanaEntry
import com.example.data.entity.StudySession
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppRepository(
  private val sadhanaDao: SadhanaDao,
  private val collegeDao: CollegeDao,
  private val gateDao: GateDao
) {
  fun getTodayDateString(): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
  }

  // --- Sadhana ---
  fun getSadhanaForDate(date: String): Flow<SadhanaEntry?> =
    sadhanaDao.getSadhanaForDate(date)

  fun getRecentSadhanaLogs(): Flow<List<SadhanaEntry>> =
    sadhanaDao.getRecentSadhanaLogs()

  suspend fun saveSadhanaEntry(entry: SadhanaEntry) {
    val scoredEntry = entry.copy(scorePercentage = entry.calculateScore())
    sadhanaDao.insertOrUpdateSadhana(scoredEntry)
  }

  suspend fun incrementJapaRound(currentEntry: SadhanaEntry?) {
    val today = getTodayDateString()
    val base = currentEntry ?: SadhanaEntry(date = today)
    val updated = base.copy(japaRounds = base.japaRounds + 1)
    saveSadhanaEntry(updated)
  }

  // --- College ---
  val allTasks: Flow<List<CollegeTask>> = collegeDao.getAllTasks()
  val pendingTasks: Flow<List<CollegeTask>> = collegeDao.getPendingTasks()
  val attendanceList: Flow<List<CollegeAttendance>> = collegeDao.getAllAttendance()

  suspend fun insertTask(task: CollegeTask) = collegeDao.insertTask(task)

  suspend fun toggleTaskCompleted(task: CollegeTask) {
    val newStatus = !task.isCompleted
    val completedAt = if (newStatus) System.currentTimeMillis() else null
    collegeDao.setTaskCompleted(task.id, newStatus, completedAt)
  }

  suspend fun deleteTask(task: CollegeTask) = collegeDao.deleteTask(task)

  suspend fun insertAttendance(attendance: CollegeAttendance) =
    collegeDao.insertAttendance(attendance)

  suspend fun markAttendancePresent(id: Long) = collegeDao.markPresent(id)

  suspend fun markAttendanceAbsent(id: Long) = collegeDao.markAbsent(id)

  suspend fun deleteAttendance(attendance: CollegeAttendance) =
    collegeDao.deleteAttendance(attendance)

  // --- GATE Prep ---
  val allGateTopics: Flow<List<GateTopic>> = gateDao.getAllTopics()
  val allSubjects: Flow<List<String>> = gateDao.getAllSubjects()
  val allMockTests: Flow<List<GateMockTest>> = gateDao.getAllMockTests()
  val recentStudySessions: Flow<List<StudySession>> = gateDao.getRecentSessions()

  fun getSessionsForDate(date: String): Flow<List<StudySession>> =
    gateDao.getSessionsForDate(date)

  fun getTotalStudyMinutesForDate(date: String): Flow<Int?> =
    gateDao.getTotalMinutesForDate(date)

  suspend fun updateTopicStatus(id: Long, newStatus: String) {
    gateDao.updateTopicStatus(id, newStatus, System.currentTimeMillis())
  }

  suspend fun insertTopic(topic: GateTopic) = gateDao.insertTopic(topic)

  suspend fun deleteTopic(topic: GateTopic) = gateDao.deleteTopic(topic)

  suspend fun insertMockTest(mockTest: GateMockTest) = gateDao.insertMockTest(mockTest)

  suspend fun deleteMockTest(mockTest: GateMockTest) = gateDao.deleteMockTest(mockTest)

  suspend fun logStudySession(category: String, topic: String, durationMinutes: Int) {
    val session = StudySession(
      category = category,
      subjectOrTopic = topic,
      durationMinutes = durationMinutes,
      date = getTodayDateString(),
      timestamp = System.currentTimeMillis()
    )
    gateDao.insertStudySession(session)
  }
}
