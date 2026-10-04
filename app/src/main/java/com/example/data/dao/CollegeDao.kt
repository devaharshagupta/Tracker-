package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.CollegeAttendance
import com.example.data.entity.CollegeTask
import kotlinx.coroutines.flow.Flow

@Dao
interface CollegeDao {
  // Tasks
  @Query("SELECT * FROM college_tasks ORDER BY isCompleted ASC, dueDate ASC")
  fun getAllTasks(): Flow<List<CollegeTask>>

  @Query("SELECT * FROM college_tasks WHERE isCompleted = 0 ORDER BY dueDate ASC")
  fun getPendingTasks(): Flow<List<CollegeTask>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTask(task: CollegeTask): Long

  @Update
  suspend fun updateTask(task: CollegeTask)

  @Query("UPDATE college_tasks SET isCompleted = :isCompleted, completedAt = :completedAt WHERE id = :id")
  suspend fun setTaskCompleted(id: Long, isCompleted: Boolean, completedAt: Long?)

  @Delete
  suspend fun deleteTask(task: CollegeTask)

  // Attendance
  @Query("SELECT * FROM college_attendance ORDER BY subjectName ASC")
  fun getAllAttendance(): Flow<List<CollegeAttendance>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAttendance(attendance: CollegeAttendance): Long

  @Update
  suspend fun updateAttendance(attendance: CollegeAttendance)

  @Delete
  suspend fun deleteAttendance(attendance: CollegeAttendance)

  @Query("UPDATE college_attendance SET attendedClasses = attendedClasses + 1, totalClasses = totalClasses + 1 WHERE id = :id")
  suspend fun markPresent(id: Long)

  @Query("UPDATE college_attendance SET totalClasses = totalClasses + 1 WHERE id = :id")
  suspend fun markAbsent(id: Long)
}
