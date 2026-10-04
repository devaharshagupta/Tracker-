package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sadhana_entries")
data class SadhanaEntry(
  @PrimaryKey val date: String, // format: YYYY-MM-DD
  val japaRounds: Int = 0,
  val targetRounds: Int = 16,
  val wakingTime: String = "05:30 AM",
  val wokeBefore6Am: Boolean = true,
  val gitaReadingMinutes: Int = 0,
  val bhagavatamReadingMinutes: Int = 0,
  val lectureMinutes: Int = 0,
  val aartiDone: Boolean = false,
  val prasadamOnly: Boolean = true,
  val associationDone: Boolean = false,
  val reflectionNote: String = "",
  val scorePercentage: Int = 0
) {
  fun calculateScore(): Int {
    var points = 0
    val maxPoints = 100

    // Japa rounds contribute up to 35%
    val japaRatio = (japaRounds.toFloat() / targetRounds.coerceAtLeast(1)).coerceAtMost(1f)
    points += (japaRatio * 35).toInt()

    // Scripture reading (Gita/Bhagavatam) contributes up to 25% (target: 30 mins)
    val readingTotal = gitaReadingMinutes + bhagavatamReadingMinutes
    val readingRatio = (readingTotal.toFloat() / 30f).coerceAtMost(1f)
    points += (readingRatio * 25).toInt()

    // Lecture hearing up to 15% (target: 20 mins)
    val lectureRatio = (lectureMinutes.toFloat() / 20f).coerceAtMost(1f)
    points += (lectureRatio * 15).toInt()

    // Waking up before 6 AM (Brahma Muhurta / Morning) (10%)
    if (wokeBefore6Am) points += 10

    // Aarti / Puja (5%)
    if (aartiDone) points += 5

    // Honoring only Prasadam (5%)
    if (prasadamOnly) points += 5

    // Vaishnava Association / Satsang (5%)
    if (associationDone) points += 5

    return points.coerceIn(0, 100)
  }
}
