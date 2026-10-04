package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.CollegeAttendance
import com.example.data.entity.CollegeTask
import com.example.ui.theme.PeacockNavy
import com.example.ui.theme.SaffronPrimary
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CollegeScreen(
  viewModel: MainViewModel,
  modifier: Modifier = Modifier
) {
  var selectedSubTab by remember { mutableIntStateOf(0) } // 0: Tasks, 1: Attendance
  var showAddTaskDialog by remember { mutableStateOf(false) }
  var showAddAttendanceDialog by remember { mutableStateOf(false) }

  val allTasks by viewModel.allTasks.collectAsState()
  val attendanceList by viewModel.attendanceList.collectAsState()
  val taskFilter by viewModel.taskFilter.collectAsState()

  val filteredTasks = remember(allTasks, taskFilter) {
    when (taskFilter) {
      "Pending" -> allTasks.filter { !it.isCompleted }
      "Completed" -> allTasks.filter { it.isCompleted }
      else -> allTasks
    }
  }

  Box(modifier = modifier.fillMaxSize()) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Header
      TabRow(
        selectedTabIndex = selectedSubTab,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = PeacockNavy
      ) {
        Tab(
          selected = selectedSubTab == 0,
          onClick = { selectedSubTab = 0 },
          text = { Text("College Tasks & Works (${allTasks.count { !it.isCompleted }})", fontWeight = FontWeight.Bold) }
        )
        Tab(
          selected = selectedSubTab == 1,
          onClick = { selectedSubTab = 1 },
          text = { Text("Attendance Tracker", fontWeight = FontWeight.Bold) }
        )
      }

      if (selectedSubTab == 0) {
        // --- Tasks Section ---
        TasksSection(
          tasks = filteredTasks,
          activeFilter = taskFilter,
          onFilterSelect = { viewModel.setTaskFilter(it) },
          onToggleTask = { viewModel.toggleTaskCompleted(it) },
          onDeleteTask = { viewModel.deleteTask(it) }
        )
      } else {
        // --- Attendance Section ---
        AttendanceSection(
          attendanceList = attendanceList,
          onMarkPresent = { viewModel.markAttendancePresent(it) },
          onMarkAbsent = { viewModel.markAttendanceAbsent(it) },
          onDeleteCourse = { viewModel.deleteAttendanceCourse(it) }
        )
      }
    }

    // Floating Action Button
    FloatingActionButton(
      onClick = {
        if (selectedSubTab == 0) showAddTaskDialog = true else showAddAttendanceDialog = true
      },
      containerColor = PeacockNavy,
      contentColor = Color.White,
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(16.dp)
        .testTag("college_fab_add")
    ) {
      Icon(Icons.Default.Add, contentDescription = "Add New")
    }
  }

  // Add Task Dialog
  if (showAddTaskDialog) {
    AddTaskDialog(
      onDismiss = { showAddTaskDialog = false },
      onConfirm = { title, course, type, dueDays, priority, notes ->
        val dueMillis = System.currentTimeMillis() + (dueDays * 24 * 60 * 60 * 1000L)
        viewModel.addNewTask(title, course, type, dueMillis, priority, notes)
        showAddTaskDialog = false
      }
    )
  }

  // Add Attendance Course Dialog
  if (showAddAttendanceDialog) {
    AddAttendanceCourseDialog(
      onDismiss = { showAddAttendanceDialog = false },
      onConfirm = { code, name, attended, total, target ->
        viewModel.addNewAttendanceCourse(code, name, attended, total, target)
        showAddAttendanceDialog = false
      }
    )
  }
}

@Composable
fun TasksSection(
  tasks: List<CollegeTask>,
  activeFilter: String,
  onFilterSelect: (String) -> Unit,
  onToggleTask: (CollegeTask) -> Unit,
  onDeleteTask: (CollegeTask) -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 80.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf("Pending", "Completed", "All").forEach { filter ->
          FilterChip(
            selected = activeFilter == filter,
            onClick = { onFilterSelect(filter) },
            label = { Text(filter) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
              selectedLabelColor = PeacockNavy
            )
          )
        }
      }
    }

    if (tasks.isEmpty()) {
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = if (activeFilter == "Pending") "No pending college tasks!" else "No tasks found",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Tap the + button to add assignments, lab reports, or project milestones.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    } else {
      items(tasks, key = { it.id }) { task ->
        TaskItemCard(
          task = task,
          onToggle = { onToggleTask(task) },
          onDelete = { onDeleteTask(task) }
        )
      }
    }
  }
}

@Composable
fun TaskItemCard(
  task: CollegeTask,
  onToggle: () -> Unit,
  onDelete: () -> Unit
) {
  val dateFormatted = remember(task.dueDate) {
    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(task.dueDate))
  }
  val daysLeft = remember(task.dueDate) {
    ((task.dueDate - System.currentTimeMillis()) / (24 * 60 * 60 * 1000L)).toInt()
  }

  val priorityColor = when (task.priority) {
    "High" -> Color(0xFFEF4444)
    "Medium" -> Color(0xFFF59E0B)
    else -> Color(0xFF10B981)
  }

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (task.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = if (task.isCompleted) 0.dp else 2.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.Top
    ) {
      Checkbox(
        checked = task.isCompleted,
        onCheckedChange = { onToggle() },
        colors = CheckboxDefaults.colors(checkedColor = PeacockNavy)
      )

      Spacer(modifier = Modifier.width(8.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.padding(bottom = 2.dp)
          ) {
            Text(
              text = task.courseName,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = PeacockNavy,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }

          Surface(
            shape = RoundedCornerShape(6.dp),
            color = priorityColor.copy(alpha = 0.15f),
            modifier = Modifier.padding(bottom = 2.dp)
          ) {
            Text(
              text = "${task.priority} Priority",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = priorityColor,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = task.title,
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.SemiBold,
          textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
          color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
        )

        if (task.notes.isNotBlank()) {
          Text(
            text = task.notes,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
          )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Event,
              contentDescription = null,
              tint = if (daysLeft <= 1 && !task.isCompleted) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (daysLeft < 0) "Overdue by ${-daysLeft} days" else if (daysLeft == 0) "Due Today!" else "Due in $daysLeft days ($dateFormatted)",
              style = MaterialTheme.typography.labelSmall,
              color = if (daysLeft <= 1 && !task.isCompleted) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant,
              fontWeight = if (daysLeft <= 1) FontWeight.Bold else FontWeight.Normal
            )
          }

          IconButton(
            onClick = onDelete,
            modifier = Modifier.size(24.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = "Delete",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
fun AttendanceSection(
  attendanceList: List<CollegeAttendance>,
  onMarkPresent: (CollegeAttendance) -> Unit,
  onMarkAbsent: (CollegeAttendance) -> Unit,
  onDeleteCourse: (CollegeAttendance) -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "College Attendance Tracker",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = PeacockNavy
          )
          Text(
            text = "Target is 75% minimum criteria. Real-time bunk / attendance recovery guidance.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    items(attendanceList, key = { it.id }) { item ->
      AttendanceCard(
        item = item,
        onMarkPresent = { onMarkPresent(item) },
        onMarkAbsent = { onMarkAbsent(item) },
        onDelete = { onDeleteCourse(item) }
      )
    }
  }
}

@Composable
fun AttendanceCard(
  item: CollegeAttendance,
  onMarkPresent: () -> Unit,
  onMarkAbsent: () -> Unit,
  onDelete: () -> Unit
) {
  val percent = item.currentPercentage
  val target = item.targetPercentage
  val statusColor = when (item.statusColorType) {
    "SAFE" -> Color(0xFF10B981)
    "WARNING" -> Color(0xFFF59E0B)
    else -> Color(0xFFEF4444)
  }

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          if (item.subjectCode.isNotBlank()) {
            Text(
              text = item.subjectCode,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = PeacockNavy
            )
          }
          Text(
            text = item.subjectName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Text(
            text = "${item.attendedClasses} / ${item.totalClasses} classes attended (Target: $target%)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Surface(
          shape = RoundedCornerShape(12.dp),
          color = statusColor.copy(alpha = 0.15f),
          modifier = Modifier.padding(start = 8.dp)
        ) {
          Text(
            text = String.format(Locale.US, "%.1f%%", percent),
            color = statusColor,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      LinearProgressIndicator(
        progress = { (percent / 100f).coerceIn(0f, 1f) },
        modifier = Modifier
          .fillMaxWidth()
          .height(8.dp)
          .clip(RoundedCornerShape(4.dp)),
        color = statusColor,
        trackColor = statusColor.copy(alpha = 0.2f)
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Advice pill
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = item.calculateAdvice(),
          style = MaterialTheme.typography.bodySmall,
          fontWeight = FontWeight.Medium,
          color = if (percent < target) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(8.dp)
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onDelete,
          modifier = Modifier.size(32.dp)
        ) {
          Icon(Icons.Default.Delete, contentDescription = "Delete Course", tint = MaterialTheme.colorScheme.outline)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedButton(
            onClick = onMarkAbsent,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.height(36.dp)
          ) {
            Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Absent", color = Color(0xFFEF4444), fontSize = 12.sp)
          }

          Button(
            onClick = onMarkPresent,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
            modifier = Modifier.height(36.dp)
          ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Present", color = Color.White, fontSize = 12.sp)
          }
        }
      }
    }
  }
}

@Composable
fun AddTaskDialog(
  onDismiss: () -> Unit,
  onConfirm: (title: String, course: String, type: String, dueDays: Int, priority: String, notes: String) -> Unit
) {
  var title by remember { mutableStateOf("") }
  var course by remember { mutableStateOf("") }
  var type by remember { mutableStateOf("Assignment") }
  var priority by remember { mutableStateOf("High") }
  var dueDays by remember { mutableIntStateOf(3) }
  var notes by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Add College Work / Task") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Task Title *") },
          placeholder = { Text("e.g. Lab 3 Implementation") },
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = course,
          onValueChange = { course = it },
          label = { Text("Course / Subject *") },
          placeholder = { Text("e.g. Operating Systems") },
          modifier = Modifier.fillMaxWidth()
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf("High", "Medium", "Low").forEach { p ->
            FilterChip(
              selected = priority == p,
              onClick = { priority = p },
              label = { Text(p) }
            )
          }
        }
        OutlinedTextField(
          value = dueDays.toString(),
          onValueChange = { dueDays = it.toIntOrNull() ?: 1 },
          label = { Text("Due in (days from today)") },
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text("Notes / Requirements (Optional)") },
          modifier = Modifier.fillMaxWidth(),
          maxLines = 2
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (title.isNotBlank() && course.isNotBlank()) {
            onConfirm(title, course, type, dueDays, priority, notes)
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = PeacockNavy)
      ) {
        Text("Save Task")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

@Composable
fun AddAttendanceCourseDialog(
  onDismiss: () -> Unit,
  onConfirm: (code: String, name: String, attended: Int, total: Int, target: Int) -> Unit
) {
  var code by remember { mutableStateOf("") }
  var name by remember { mutableStateOf("") }
  var attendedStr by remember { mutableStateOf("0") }
  var totalStr by remember { mutableStateOf("0") }
  var targetStr by remember { mutableStateOf("75") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Add Course for Attendance") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
          value = code,
          onValueChange = { code = it },
          label = { Text("Course Code (Optional)") },
          placeholder = { Text("e.g. CS501") },
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Course Name *") },
          placeholder = { Text("e.g. Computer Networks") },
          modifier = Modifier.fillMaxWidth()
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = attendedStr,
            onValueChange = { attendedStr = it },
            label = { Text("Attended") },
            modifier = Modifier.weight(1f)
          )
          OutlinedTextField(
            value = totalStr,
            onValueChange = { totalStr = it },
            label = { Text("Total") },
            modifier = Modifier.weight(1f)
          )
        }
        OutlinedTextField(
          value = targetStr,
          onValueChange = { targetStr = it },
          label = { Text("Target % (Standard: 75)") },
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotBlank()) {
            val attended = attendedStr.toIntOrNull() ?: 0
            val total = totalStr.toIntOrNull() ?: 0
            val target = targetStr.toIntOrNull() ?: 75
            onConfirm(code, name, attended, total, target)
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = PeacockNavy)
      ) {
        Text("Add Course")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}
