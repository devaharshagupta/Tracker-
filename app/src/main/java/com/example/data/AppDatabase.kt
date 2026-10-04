package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.CollegeDao
import com.example.data.dao.GateDao
import com.example.data.dao.SadhanaDao
import com.example.data.entity.CollegeAttendance
import com.example.data.entity.CollegeTask
import com.example.data.entity.GateMockTest
import com.example.data.entity.GateTopic
import com.example.data.entity.SadhanaEntry
import com.example.data.entity.StudySession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Database(
  entities = [
    SadhanaEntry::class,
    CollegeTask::class,
    CollegeAttendance::class,
    GateTopic::class,
    GateMockTest::class,
    StudySession::class
  ],
  version = 1,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun sadhanaDao(): SadhanaDao
  abstract fun collegeDao(): CollegeDao
  abstract fun gateDao(): GateDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "sankalpa_database"
        )
          .addCallback(DatabaseCallback(scope))
          .build()
        INSTANCE = instance
        instance
      }
    }

    private class DatabaseCallback(
      private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
      override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        INSTANCE?.let { database ->
          scope.launch(Dispatchers.IO) {
            populateInitialData(database)
          }
        }
      }
    }

    suspend fun populateInitialData(db: AppDatabase) {
      val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

      // 1. Initial Sadhana Entry for Today
      val initialSadhana = SadhanaEntry(
        date = today,
        japaRounds = 4,
        targetRounds = 16,
        wakingTime = "05:15 AM",
        wokeBefore6Am = true,
        gitaReadingMinutes = 15,
        bhagavatamReadingMinutes = 0,
        lectureMinutes = 20,
        aartiDone = true,
        prasadamOnly = true,
        associationDone = true,
        reflectionNote = "Chanted 4 peaceful morning rounds before college. Mind felt steady and focused."
      )
      db.sadhanaDao().insertOrUpdateSadhana(
        initialSadhana.copy(scorePercentage = initialSadhana.calculateScore())
      )

      // 2. Initial College Attendance
      val attendanceList = listOf(
        CollegeAttendance(subjectCode = "CS501", subjectName = "Operating Systems", attendedClasses = 28, totalClasses = 32, targetPercentage = 75),
        CollegeAttendance(subjectCode = "CS502", subjectName = "Database Management Systems", attendedClasses = 24, totalClasses = 30, targetPercentage = 75),
        CollegeAttendance(subjectCode = "CS503", subjectName = "Computer Networks", attendedClasses = 21, totalClasses = 28, targetPercentage = 75),
        CollegeAttendance(subjectCode = "CS504", subjectName = "Design & Analysis of Algorithms", attendedClasses = 29, totalClasses = 31, targetPercentage = 75),
        CollegeAttendance(subjectCode = "CS505P", subjectName = "DBMS & OS Lab", attendedClasses = 12, totalClasses = 12, targetPercentage = 75)
      )
      for (att in attendanceList) {
        db.collegeDao().insertAttendance(att)
      }

      // 3. Initial College Tasks
      val now = System.currentTimeMillis()
      val dayMillis = 24 * 60 * 60 * 1000L
      val tasks = listOf(
        CollegeTask(
          title = "OS Lab Assignment 4: Process Synchronization Semaphore Implementation",
          courseName = "Operating Systems",
          taskType = "Lab Report",
          dueDate = now + (2 * dayMillis),
          priority = "High",
          isCompleted = false,
          notes = "Complete producer-consumer solution in C with POSIX mutex."
        ),
        CollegeTask(
          title = "DBMS Assignment: SQL Queries, Normalization up to BCNF",
          courseName = "Database Management Systems",
          taskType = "Assignment",
          dueDate = now + (4 * dayMillis),
          priority = "High",
          isCompleted = false,
          notes = "Solve practice questions 1-15 from textbook."
        ),
        CollegeTask(
          title = "Computer Networks Seminar Presentation: TCP Congestion Control",
          courseName = "Computer Networks",
          taskType = "Seminar",
          dueDate = now + (6 * dayMillis),
          priority = "Medium",
          isCompleted = false,
          notes = "Prepare 8-slide presentation comparing Reno and Cubic algorithms."
        )
      )
      for (task in tasks) {
        db.collegeDao().insertTask(task)
      }

      // 4. Initial GATE Topics Roadmap
      val gateTopics = listOf(
        GateTopic(subject = "Algorithms", topicName = "Asymptotic Analysis & Recurrences", weightage = "High", status = "Mastered", revisionCount = 2),
        GateTopic(subject = "Algorithms", topicName = "Divide & Conquer (Sorting, Searching)", weightage = "High", status = "Revised 1x", revisionCount = 1),
        GateTopic(subject = "Algorithms", topicName = "Greedy Techniques & Dynamic Programming", weightage = "High", status = "Practice", revisionCount = 0),
        GateTopic(subject = "Algorithms", topicName = "Graph Algorithms (BFS, DFS, Dijkstra, MST)", weightage = "High", status = "Theory", revisionCount = 0),
        GateTopic(subject = "Data Structures", topicName = "Arrays, Stacks, Queues & Linked Lists", weightage = "High", status = "Mastered", revisionCount = 3),
        GateTopic(subject = "Data Structures", topicName = "Binary Trees, BST, AVL & Heaps", weightage = "High", status = "Revised 1x", revisionCount = 1),
        GateTopic(subject = "Operating Systems", topicName = "Process Scheduling & CPU Schedulers", weightage = "High", status = "PYQs Done", revisionCount = 1),
        GateTopic(subject = "Operating Systems", topicName = "Deadlocks, Bankers Algorithm & Prevention", weightage = "High", status = "Practice", revisionCount = 0),
        GateTopic(subject = "Operating Systems", topicName = "Memory Management: Paging & Virtual Memory", weightage = "High", status = "Theory", revisionCount = 0),
        GateTopic(subject = "Operating Systems", topicName = "File Systems & Disk Scheduling", weightage = "Medium", status = "Not Started", revisionCount = 0),
        GateTopic(subject = "Database Management", topicName = "ER Modeling & Relational Algebra", weightage = "High", status = "Revised 1x", revisionCount = 1),
        GateTopic(subject = "Database Management", topicName = "SQL & Normal Forms (1NF, 2NF, 3NF, BCNF)", weightage = "High", status = "Practice", revisionCount = 0),
        GateTopic(subject = "Database Management", topicName = "Transactions & Concurrency Control (ACID, Serializability)", weightage = "High", status = "Theory", revisionCount = 0),
        GateTopic(subject = "Computer Networks", topicName = "OSI & TCP/IP Layer Architecture", weightage = "Medium", status = "Revised 1x", revisionCount = 1),
        GateTopic(subject = "Computer Networks", topicName = "Subnetting, CIDR, Routing Algorithms", weightage = "High", status = "Theory", revisionCount = 0),
        GateTopic(subject = "Computer Networks", topicName = "TCP Flow & Congestion Control, Sliding Window", weightage = "High", status = "Not Started", revisionCount = 0),
        GateTopic(subject = "Theory of Computation", topicName = "Regular Languages, DFA & NFA Minimization", weightage = "High", status = "PYQs Done", revisionCount = 2),
        GateTopic(subject = "Theory of Computation", topicName = "Context-Free Languages & Pushdown Automata", weightage = "High", status = "Theory", revisionCount = 0),
        GateTopic(subject = "Theory of Computation", topicName = "Turing Machines & Decidability / Undecidability", weightage = "High", status = "Not Started", revisionCount = 0),
        GateTopic(subject = "Engineering Maths", topicName = "Linear Algebra (Matrices, Eigenvalues, Rank)", weightage = "High", status = "Revised 1x", revisionCount = 1),
        GateTopic(subject = "Engineering Maths", topicName = "Probability & Statistics, Bayes Theorem", weightage = "High", status = "Practice", revisionCount = 0),
        GateTopic(subject = "Engineering Maths", topicName = "Combinatorics & Graph Theory (Discrete Maths)", weightage = "High", status = "Theory", revisionCount = 0),
        GateTopic(subject = "General Aptitude", topicName = "Numerical Ability & Quantitative Reasoning", weightage = "High", status = "Practice", revisionCount = 1),
        GateTopic(subject = "General Aptitude", topicName = "Spatial Aptitude & Verbal Ability", weightage = "Medium", status = "Not Started", revisionCount = 0)
      )
      db.gateDao().insertTopics(gateTopics)

      // 5. Initial Mock Test sample
      db.gateDao().insertMockTest(
        GateMockTest(
          testName = "Algorithms & Data Structures Subject Mock 1",
          testType = "Subject Test",
          marksScored = 38.5f,
          totalMarks = 50f,
          negativeMarks = 2.66f,
          accuracyPercent = 84f,
          weakAreas = "Dynamic Programming memoization states & graph topological sort corner cases"
        )
      )

      // 6. Initial Study Sessions
      db.gateDao().insertStudySession(
        StudySession(
          category = "GATE Prep",
          subjectOrTopic = "Operating Systems - Process Synchronization",
          durationMinutes = 75,
          date = today,
          timestamp = now - (3 * 3600 * 1000L)
        )
      )
      db.gateDao().insertStudySession(
        StudySession(
          category = "Krishna Sadhana",
          subjectOrTopic = "Bhagavad Gita Chapter 2 Verse 47-50",
          durationMinutes = 25,
          date = today,
          timestamp = now - (6 * 3600 * 1000L)
        )
      )
    }
  }
}
