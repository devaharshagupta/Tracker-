package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.entity.CollegeAttendance
import com.example.data.entity.SadhanaEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Sankalpa", appName)
  }

  @Test
  fun `sadhana score calculation works correctly`() {
    val fullSadhana = SadhanaEntry(
      date = "2026-10-04",
      japaRounds = 16,
      targetRounds = 16,
      wokeBefore6Am = true,
      gitaReadingMinutes = 30,
      bhagavatamReadingMinutes = 0,
      lectureMinutes = 20,
      aartiDone = true,
      prasadamOnly = true,
      associationDone = true
    )
    val score = fullSadhana.calculateScore()
    assertEquals(100, score)
  }

  @Test
  fun `college attendance advice calculates correctly`() {
    val attendance = CollegeAttendance(
      subjectName = "Operating Systems",
      attendedClasses = 28,
      totalClasses = 32,
      targetPercentage = 75
    )
    assertTrue(attendance.currentPercentage >= 75f)
    val advice = attendance.calculateAdvice()
    assertTrue(advice.contains("safely miss"))
  }
}
