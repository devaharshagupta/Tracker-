package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.entity.CollegeAttendance
import com.example.data.entity.CollegeTask
import com.example.data.entity.GateMockTest
import com.example.data.entity.GateTopic
import com.example.data.entity.GitaShlokaRepository
import com.example.data.entity.SadhanaEntry
import com.example.data.entity.StudySession
import com.example.data.repository.AppRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppTab(val title: String) {
  DASHBOARD("Daily Hub"),
  SADHANA("Sadhana"),
  COLLEGE("College"),
  GATE("GATE Prep"),
  FOCUS("Timer & Stats")
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
  private val repository: AppRepository
  val todayDateString: String

  // Navigation
  private val _currentTab = MutableStateFlow(AppTab.DASHBOARD)
  val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

  // Japa Mala interactive counter (1 to 108 beads)
  private val _currentBead = MutableStateFlow(1)
  val currentBead: StateFlow<Int> = _currentBead.asStateFlow()

  // Focus Timer
  private val _timerSeconds = MutableStateFlow(25 * 60)
  val timerSeconds: StateFlow<Int> = _timerSeconds.asStateFlow()

  private val _isTimerRunning = MutableStateFlow(false)
  val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

  private val _timerCategory = MutableStateFlow("GATE Prep")
  val timerCategory: StateFlow<String> = _timerCategory.asStateFlow()

  private val _timerTopic = MutableStateFlow("Operating Systems")
  val timerTopic: StateFlow<String> = _timerTopic.asStateFlow()

  private var timerJob: Job? = null

  // User notification/snackbar message
  private val _userMessage = MutableStateFlow<String?>(null)
  val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

  // Selected filter for College Tasks
  private val _taskFilter = MutableStateFlow("Pending") // "All", "Pending", "Completed"
  val taskFilter: StateFlow<String> = _taskFilter.asStateFlow()

  // Selected subject filter for GATE Topics
  private val _gateSubjectFilter = MutableStateFlow("All")
  val gateSubjectFilter: StateFlow<String> = _gateSubjectFilter.asStateFlow()

  init {
    val database = AppDatabase.getDatabase(application, viewModelScope)
    repository = AppRepository(
      sadhanaDao = database.sadhanaDao(),
      collegeDao = database.collegeDao(),
      gateDao = database.gateDao()
    )
    todayDateString = repository.getTodayDateString()
  }

  // --- Reactive UI States ---
  val todaySadhana: StateFlow<SadhanaEntry?> =
    repository.getSadhanaForDate(todayDateString)
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  val recentSadhanaList: StateFlow<List<SadhanaEntry>> =
    repository.getRecentSadhanaLogs()
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allTasks: StateFlow<List<CollegeTask>> =
    repository.allTasks
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val pendingTasks: StateFlow<List<CollegeTask>> =
    repository.pendingTasks
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val attendanceList: StateFlow<List<CollegeAttendance>> =
    repository.attendanceList
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val gateTopics: StateFlow<List<GateTopic>> =
    repository.allGateTopics
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val gateMockTests: StateFlow<List<GateMockTest>> =
    repository.allMockTests
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val todayStudySessions: StateFlow<List<StudySession>> =
    repository.getSessionsForDate(todayDateString)
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val recentStudySessions: StateFlow<List<StudySession>> =
    repository.recentStudySessions
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val todayShloka = GitaShlokaRepository.getTodayShloka()

  fun setTab(tab: AppTab) {
    _currentTab.value = tab
  }

  fun setTaskFilter(filter: String) {
    _taskFilter.value = filter
  }

  fun setGateSubjectFilter(subject: String) {
    _gateSubjectFilter.value = subject
  }

  fun clearUserMessage() {
    _userMessage.value = null
  }

  // --- Japa Mala / Chanting Actions ---
  fun onMalaBeadTapped() {
    vibrateDevice(isRoundComplete = false)
    if (_currentBead.value >= 108) {
      // Completed one round!
      _currentBead.value = 1
      vibrateDevice(isRoundComplete = true)
      viewModelScope.launch {
        repository.incrementJapaRound(todaySadhana.value)
        _userMessage.value = "Hare Krishna! 1 Round Completed on Japa Mala!"
      }
    } else {
      _currentBead.value += 1
    }
  }

  fun resetBeadCounter() {
    _currentBead.value = 1
  }

  fun updateSadhanaEntry(updated: SadhanaEntry) {
    viewModelScope.launch {
      repository.saveSadhanaEntry(updated)
      _userMessage.value = "Sadhana log updated!"
    }
  }

  fun incrementJapaRoundQuick() {
    vibrateDevice(isRoundComplete = true)
    viewModelScope.launch {
      repository.incrementJapaRound(todaySadhana.value)
    }
  }

  fun decrementJapaRoundQuick() {
    val current = todaySadhana.value ?: return
    if (current.japaRounds > 0) {
      viewModelScope.launch {
        repository.saveSadhanaEntry(current.copy(japaRounds = current.japaRounds - 1))
      }
    }
  }

  // --- College Actions ---
  fun toggleTaskCompleted(task: CollegeTask) {
    viewModelScope.launch {
      repository.toggleTaskCompleted(task)
      _userMessage.value = if (!task.isCompleted) "Task completed! Great job." else "Task marked pending."
    }
  }

  fun addNewTask(
    title: String,
    courseName: String,
    taskType: String,
    dueDate: Long,
    priority: String,
    notes: String
  ) {
    viewModelScope.launch {
      val task = CollegeTask(
        title = title,
        courseName = courseName,
        taskType = taskType,
        dueDate = dueDate,
        priority = priority,
        notes = notes
      )
      repository.insertTask(task)
      _userMessage.value = "Added task: $title"
    }
  }

  fun deleteTask(task: CollegeTask) {
    viewModelScope.launch {
      repository.deleteTask(task)
      _userMessage.value = "Task deleted"
    }
  }

  fun markAttendancePresent(attendance: CollegeAttendance) {
    viewModelScope.launch {
      repository.markAttendancePresent(attendance.id)
    }
  }

  fun markAttendanceAbsent(attendance: CollegeAttendance) {
    viewModelScope.launch {
      repository.markAttendanceAbsent(attendance.id)
    }
  }

  fun addNewAttendanceCourse(subjectCode: String, subjectName: String, attended: Int, total: Int, target: Int) {
    viewModelScope.launch {
      val att = CollegeAttendance(
        subjectCode = subjectCode,
        subjectName = subjectName,
        attendedClasses = attended,
        totalClasses = total,
        targetPercentage = target
      )
      repository.insertAttendance(att)
      _userMessage.value = "Added course: $subjectName"
    }
  }

  fun deleteAttendanceCourse(attendance: CollegeAttendance) {
    viewModelScope.launch {
      repository.deleteAttendance(attendance)
      _userMessage.value = "Course removed from attendance tracker"
    }
  }

  // --- GATE Actions ---
  fun updateGateTopicStatus(topic: GateTopic, newStatus: String) {
    viewModelScope.launch {
      repository.updateTopicStatus(topic.id, newStatus)
      _userMessage.value = "${topic.topicName} marked as $newStatus"
    }
  }

  fun addNewGateTopic(subject: String, topicName: String, weightage: String, status: String, notes: String) {
    viewModelScope.launch {
      val topic = GateTopic(
        subject = subject,
        topicName = topicName,
        weightage = weightage,
        status = status,
        notes = notes
      )
      repository.insertTopic(topic)
      _userMessage.value = "Added GATE topic: $topicName"
    }
  }

  fun deleteGateTopic(topic: GateTopic) {
    viewModelScope.launch {
      repository.deleteTopic(topic)
      _userMessage.value = "Topic deleted"
    }
  }

  fun addMockTestResult(
    testName: String,
    testType: String,
    marks: Float,
    total: Float,
    negative: Float,
    accuracy: Float,
    weakAreas: String
  ) {
    viewModelScope.launch {
      val mock = GateMockTest(
        testName = testName,
        testType = testType,
        marksScored = marks,
        totalMarks = total,
        negativeMarks = negative,
        accuracyPercent = accuracy,
        weakAreas = weakAreas,
        date = System.currentTimeMillis()
      )
      repository.insertMockTest(mock)
      _userMessage.value = "Logged Mock Test: $testName"
    }
  }

  fun deleteMockTest(test: GateMockTest) {
    viewModelScope.launch {
      repository.deleteMockTest(test)
      _userMessage.value = "Mock test entry deleted"
    }
  }

  // --- Timer Actions ---
  fun setTimerDurationMinutes(minutes: Int) {
    if (!_isTimerRunning.value) {
      _timerSeconds.value = minutes * 60
    }
  }

  fun setTimerCategory(cat: String) {
    _timerCategory.value = cat
  }

  fun setTimerTopic(topic: String) {
    _timerTopic.value = topic
  }

  fun startTimer() {
    if (_isTimerRunning.value) return
    _isTimerRunning.value = true
    val initialSeconds = _timerSeconds.value
    timerJob = viewModelScope.launch {
      while (_timerSeconds.value > 0 && _isTimerRunning.value) {
        delay(1000)
        _timerSeconds.value -= 1
      }
      if (_timerSeconds.value == 0) {
        _isTimerRunning.value = false
        val durationMins = (initialSeconds / 60).coerceAtLeast(1)
        repository.logStudySession(_timerCategory.value, _timerTopic.value, durationMins)
        vibrateDevice(isRoundComplete = true)
        _userMessage.value = "Time is up! Logged $durationMins minutes of ${_timerCategory.value}."
      }
    }
  }

  fun pauseTimer() {
    _isTimerRunning.value = false
    timerJob?.cancel()
    timerJob = null
  }

  fun resetTimer(minutes: Int = 25) {
    pauseTimer()
    _timerSeconds.value = minutes * 60
  }

  fun logManualStudySession(category: String, topic: String, minutes: Int) {
    viewModelScope.launch {
      repository.logStudySession(category, topic, minutes)
      _userMessage.value = "Logged $minutes mins focus session!"
    }
  }

  // Haptic feedback for tactile bead chanting
  private fun vibrateDevice(isRoundComplete: Boolean) {
    try {
      val context = getApplication<Application>()
      val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
      } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
      }

      if (vibrator != null && vibrator.hasVibrator()) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          if (isRoundComplete) {
            // Distinct double pulse for round completion
            val timings = longArrayOf(0, 150, 100, 200)
            val amplitudes = intArrayOf(0, 255, 0, 255)
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
          } else {
            // Subtle crisp click tick for bead
            vibrator.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
          }
        } else {
          @Suppress("DEPRECATION")
          vibrator.vibrate(if (isRoundComplete) 300L else 35L)
        }
      }
    } catch (_: Exception) {
      // Gracefully ignore if vibrator not available or permission denied in tests
    }
  }
}
