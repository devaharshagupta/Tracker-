package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "college_tasks")
data class CollegeTask(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String,
  val courseName: String,
  val taskType: String = "Assignment", // Assignment, Lab Report, Project Work, Quiz/Exam, Seminar
  val dueDate: Long, // timestamp millis
  val priority: String = "Medium", // High, Medium, Low
  val isCompleted: Boolean = false,
  val notes: String = "",
  val completedAt: Long? = null
)

@Entity(tableName = "college_attendance")
data class CollegeAttendance(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val subjectCode: String = "",
  val subjectName: String,
  val attendedClasses: Int = 0,
  val totalClasses: Int = 0,
  val targetPercentage: Int = 75
) {
  val currentPercentage: Float
    get() = if (totalClasses == 0) 100f else (attendedClasses.toFloat() / totalClasses.toFloat()) * 100f

  val statusColorType: String
    get() = when {
      currentPercentage >= targetPercentage -> "SAFE"
      currentPercentage >= targetPercentage - 5 -> "WARNING"
      else -> "CRITICAL"
    }

  /**
   * Returns how many more classes can be safely missed without dropping below target%,
   * or how many consecutive classes must be attended to reach target%.
   */
  fun calculateAdvice(): String {
    if (totalClasses == 0) return "No classes logged yet."
    val curr = currentPercentage
    val target = targetPercentage.toFloat()

    return if (curr >= target) {
      // (attended) / (total + x) >= target/100
      // 100 * attended >= target * total + target * x
      // target * x <= 100 * attended - target * total
      // x <= (100 * attended - target * total) / target
      val bunkable = ((100f * attendedClasses - target * totalClasses) / target).toInt()
      if (bunkable > 0) {
        "You can safely miss $bunkable upcoming class${if (bunkable > 1) "es" else ""}."
      } else {
        "On the borderline! Do not miss next class."
      }
    } else {
      // (attended + y) / (total + y) >= target/100
      // 100 * attended + 100 * y >= target * total + target * y
      // y * (100 - target) >= target * total - 100 * attended
      val needed = kotlin.math.ceil((target * totalClasses - 100f * attendedClasses) / (100f - target)).toInt()
      "Attend the next $needed class${if (needed > 1) "es" else ""} without absence to recover."
    }
  }
}
