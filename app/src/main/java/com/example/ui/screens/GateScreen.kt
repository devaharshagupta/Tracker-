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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.GateMockTest
import com.example.data.entity.GateTopic
import com.example.ui.theme.GateContainer
import com.example.ui.theme.GateEmerald
import com.example.ui.theme.OnGateContainer
import com.example.ui.theme.PeacockNavy
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GateScreen(
  viewModel: MainViewModel,
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableIntStateOf(0) } // 0: Syllabus, 1: Mocks
  var showAddTopicDialog by remember { mutableStateOf(false) }
  var showAddMockDialog by remember { mutableStateOf(false) }

  val gateTopics by viewModel.gateTopics.collectAsState()
  val mockTests by viewModel.gateMockTests.collectAsState()
  val subjectFilter by viewModel.gateSubjectFilter.collectAsState()

  val subjects = remember(gateTopics) {
    listOf("All") + gateTopics.map { it.subject }.distinct()
  }

  val filteredTopics = remember(gateTopics, subjectFilter) {
    if (subjectFilter == "All") gateTopics else gateTopics.filter { it.subject == subjectFilter }
  }

  val masteredCount = gateTopics.count { it.status == "Mastered" || it.status.startsWith("Revised") }
  val totalTopics = gateTopics.size.coerceAtLeast(1)
  val overallProgress = masteredCount.toFloat() / totalTopics.toFloat()

  Box(modifier = modifier.fillMaxSize()) {
    Column(modifier = Modifier.fillMaxSize()) {
      TabRow(
        selectedTabIndex = selectedTab,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = GateEmerald
      ) {
        Tab(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          text = { Text("Syllabus Roadmap ($masteredCount/$totalTopics)", fontWeight = FontWeight.Bold) }
        )
        Tab(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          text = { Text("Mock Tests (${mockTests.size})", fontWeight = FontWeight.Bold) }
        )
      }

      if (selectedTab == 0) {
        // --- Syllabus Roadmap ---
        SyllabusSection(
          topics = filteredTopics,
          subjects = subjects,
          selectedSubject = subjectFilter,
          onSelectSubject = { viewModel.setGateSubjectFilter(it) },
          overallProgress = overallProgress,
          masteredCount = masteredCount,
          totalTopics = totalTopics,
          onUpdateStatus = { topic, status -> viewModel.updateGateTopicStatus(topic, status) },
          onDeleteTopic = { viewModel.deleteGateTopic(it) }
        )
      } else {
        // --- Mock Test Analytics ---
        MockTestsSection(
          mockTests = mockTests,
          onDeleteMock = { viewModel.deleteMockTest(it) }
        )
      }
    }

    FloatingActionButton(
      onClick = {
        if (selectedTab == 0) showAddTopicDialog = true else showAddMockDialog = true
      },
      containerColor = GateEmerald,
      contentColor = Color.White,
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(16.dp)
        .testTag("gate_fab_add")
    ) {
      Icon(Icons.Default.Add, contentDescription = "Add Item")
    }
  }

  if (showAddTopicDialog) {
    AddGateTopicDialog(
      existingSubjects = subjects.filter { it != "All" },
      onDismiss = { showAddTopicDialog = false },
      onConfirm = { subject, topic, weightage, status, notes ->
        viewModel.addNewGateTopic(subject, topic, weightage, status, notes)
        showAddTopicDialog = false
      }
    )
  }

  if (showAddMockDialog) {
    AddMockTestDialog(
      onDismiss = { showAddMockDialog = false },
      onConfirm = { name, type, marks, total, negative, acc, weak ->
        viewModel.addMockTestResult(name, type, marks, total, negative, acc, weak)
        showAddMockDialog = false
      }
    )
  }
}

@Composable
fun SyllabusSection(
  topics: List<GateTopic>,
  subjects: List<String>,
  selectedSubject: String,
  onSelectSubject: (String) -> Unit,
  overallProgress: Float,
  masteredCount: Int,
  totalTopics: Int,
  onUpdateStatus: (GateTopic, String) -> Unit,
  onDeleteTopic: (GateTopic) -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 80.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // Overall Syllabus Progress Card
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GateContainer),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "GATE Preparation Roadmap",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = OnGateContainer
              )
              Text(
                text = "$masteredCount of $totalTopics topics mastered / revised",
                style = MaterialTheme.typography.bodySmall,
                color = OnGateContainer.copy(alpha = 0.8f)
              )
            }
            Text(
              text = "${(overallProgress * 100).toInt()}%",
              fontWeight = FontWeight.ExtraBold,
              fontSize = 22.sp,
              color = OnGateContainer
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          LinearProgressIndicator(
            progress = { overallProgress },
            modifier = Modifier
              .fillMaxWidth()
              .height(8.dp)
              .clip(RoundedCornerShape(4.dp)),
            color = GateEmerald,
            trackColor = Color.White.copy(alpha = 0.6f)
          )
        }
      }
    }

    // Subject Filter Chips
    item {
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        items(subjects) { subj ->
          FilterChip(
            selected = selectedSubject == subj,
            onClick = { onSelectSubject(subj) },
            label = { Text(subj, fontSize = 12.sp) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = GateContainer,
              selectedLabelColor = OnGateContainer
            )
          )
        }
      }
    }

    items(topics, key = { it.id }) { topic ->
      GateTopicCard(
        topic = topic,
        onUpdateStatus = { newStatus -> onUpdateStatus(topic, newStatus) },
        onDelete = { onDeleteTopic(topic) }
      )
    }
  }
}

@Composable
fun GateTopicCard(
  topic: GateTopic,
  onUpdateStatus: (String) -> Unit,
  onDelete: () -> Unit
) {
  var menuExpanded by remember { mutableStateOf(false) }

  val statusList = listOf(
    "Not Started",
    "Theory",
    "Practice",
    "PYQs Done",
    "Revised 1x",
    "Revised 2x",
    "Mastered"
  )

  val statusColor = when (topic.status) {
    "Mastered" -> Color(0xFF10B981)
    "Revised 2x", "Revised 1x" -> Color(0xFF0D9488)
    "PYQs Done" -> Color(0xFF3B82F6)
    "Practice" -> Color(0xFFF59E0B)
    "Theory" -> Color(0xFF8B5CF6)
    else -> Color(0xFF94A3B8)
  }

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
          ) {
            Text(
              text = topic.subject,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }

          if (topic.weightage == "High") {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = Color(0xFFFEF2F2)
            ) {
              Text(
                text = "High Weightage",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFDC2626),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }
        }

        IconButton(
          onClick = onDelete,
          modifier = Modifier.size(24.dp)
        ) {
          Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp))
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = topic.topicName,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.SemiBold
      )

      if (topic.notes.isNotBlank()) {
        Text(
          text = topic.notes,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(top = 2.dp)
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Status Selector Dropdown
        Box {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = statusColor.copy(alpha = 0.15f),
            modifier = Modifier.clickable { menuExpanded = true }
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(statusColor)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = topic.status,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = statusColor
              )
            }
          }

          DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false }
          ) {
            statusList.forEach { s ->
              DropdownMenuItem(
                text = { Text(s) },
                onClick = {
                  onUpdateStatus(s)
                  menuExpanded = false
                }
              )
            }
          }
        }

        if (topic.revisionCount > 0) {
          Text(
            text = "Revised ${topic.revisionCount}x",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }
}

@Composable
fun MockTestsSection(
  mockTests: List<GateMockTest>,
  onDeleteMock: (GateMockTest) -> Unit
) {
  val avgScore = remember(mockTests) {
    if (mockTests.isEmpty()) 0f else mockTests.map { (it.marksScored / it.totalMarks) * 100f }.average().toFloat()
  }
  val highestScore = remember(mockTests) {
    mockTests.maxOfOrNull { it.marksScored } ?: 0f
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Summary Cards
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = GateContainer),
          modifier = Modifier.weight(1f)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text("Avg Score", style = MaterialTheme.typography.labelSmall, color = OnGateContainer)
            Text(
              text = String.format(Locale.US, "%.1f%%", avgScore),
              fontWeight = FontWeight.ExtraBold,
              fontSize = 20.sp,
              color = OnGateContainer
            )
          }
        }

        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
          modifier = Modifier.weight(1f)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text("Best Score", style = MaterialTheme.typography.labelSmall, color = PeacockNavy)
            Text(
              text = String.format(Locale.US, "%.1f", highestScore),
              fontWeight = FontWeight.ExtraBold,
              fontSize = 20.sp,
              color = PeacockNavy
            )
          }
        }

        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          modifier = Modifier.weight(1f)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text("Tests Given", style = MaterialTheme.typography.labelSmall)
            Text(
              text = "${mockTests.size}",
              fontWeight = FontWeight.ExtraBold,
              fontSize = 20.sp
            )
          }
        }
      }
    }

    if (mockTests.isEmpty()) {
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              imageVector = Icons.Default.Assessment,
              contentDescription = null,
              tint = GateEmerald,
              modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "No mock tests logged yet",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Tap the + button below to log your subject tests and full length mock test marks.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    } else {
      items(mockTests, key = { it.id }) { mock ->
        MockTestCard(mock = mock, onDelete = { onDeleteMock(mock) })
      }
    }
  }
}

@Composable
fun MockTestCard(
  mock: GateMockTest,
  onDelete: () -> Unit
) {
  val dateFormatted = remember(mock.date) {
    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(mock.date))
  }
  val percent = (mock.marksScored / mock.totalMarks) * 100f

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
          Text(
            text = mock.testType,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = GateEmerald
          )
          Text(
            text = mock.testName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = dateFormatted,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Surface(
          shape = RoundedCornerShape(12.dp),
          color = GateContainer,
          modifier = Modifier.padding(start = 8.dp)
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
          ) {
            Text(
              text = "${mock.marksScored} / ${mock.totalMarks.toInt()}",
              fontWeight = FontWeight.ExtraBold,
              color = OnGateContainer,
              fontSize = 15.sp
            )
            Text(
              text = String.format(Locale.US, "%.1f%%", percent),
              fontSize = 11.sp,
              color = OnGateContainer.copy(alpha = 0.8f)
            )
          }
        }
      }

      if (mock.negativeMarks > 0 || mock.accuracyPercent > 0) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
          if (mock.negativeMarks > 0) {
            Text(
              text = "Negative: -${mock.negativeMarks}",
              style = MaterialTheme.typography.labelSmall,
              color = Color(0xFFEF4444)
            )
          }
          if (mock.accuracyPercent > 0) {
            Text(
              text = "Accuracy: ${mock.accuracyPercent}%",
              style = MaterialTheme.typography.labelSmall,
              color = Color(0xFF10B981)
            )
          }
        }
      }

      if (mock.weakAreas.isNotBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = "Weak Areas / Mistakes: ${mock.weakAreas}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(8.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
      ) {
        IconButton(
          onClick = onDelete,
          modifier = Modifier.size(28.dp)
        ) {
          Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp))
        }
      }
    }
  }
}

@Composable
fun AddGateTopicDialog(
  existingSubjects: List<String>,
  onDismiss: () -> Unit,
  onConfirm: (subject: String, topic: String, weightage: String, status: String, notes: String) -> Unit
) {
  var subject by remember { mutableStateOf(existingSubjects.firstOrNull() ?: "Engineering Maths") }
  var topic by remember { mutableStateOf("") }
  var weightage by remember { mutableStateOf("High") }
  var status by remember { mutableStateOf("Not Started") }
  var notes by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Add GATE Topic") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
          value = subject,
          onValueChange = { subject = it },
          label = { Text("Subject *") },
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = topic,
          onValueChange = { topic = it },
          label = { Text("Topic Name *") },
          placeholder = { Text("e.g. Memory Management") },
          modifier = Modifier.fillMaxWidth()
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf("High", "Medium", "Low").forEach { w ->
            FilterChip(
              selected = weightage == w,
              onClick = { weightage = w },
              label = { Text(w) }
            )
          }
        }
        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text("Reference Book / Notes (Optional)") },
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (subject.isNotBlank() && topic.isNotBlank()) {
            onConfirm(subject, topic, weightage, status, notes)
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = GateEmerald)
      ) {
        Text("Save Topic")
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
fun AddMockTestDialog(
  onDismiss: () -> Unit,
  onConfirm: (name: String, type: String, marks: Float, total: Float, negative: Float, acc: Float, weak: String) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var type by remember { mutableStateOf("Subject Test") }
  var marksStr by remember { mutableStateOf("") }
  var totalStr by remember { mutableStateOf("50") }
  var negativeStr by remember { mutableStateOf("0") }
  var accuracyStr by remember { mutableStateOf("80") }
  var weakAreas by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Log Mock Test Result") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Test Name *") },
          placeholder = { Text("e.g. OS Mock Test 1") },
          modifier = Modifier.fillMaxWidth()
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf("Subject Test", "Full Mock").forEach { t ->
            FilterChip(
              selected = type == t,
              onClick = { type = t },
              label = { Text(t) }
            )
          }
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = marksStr,
            onValueChange = { marksStr = it },
            label = { Text("Marks Scored *") },
            modifier = Modifier.weight(1f)
          )
          OutlinedTextField(
            value = totalStr,
            onValueChange = { totalStr = it },
            label = { Text("Total Marks") },
            modifier = Modifier.weight(1f)
          )
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = negativeStr,
            onValueChange = { negativeStr = it },
            label = { Text("Negative Marks") },
            modifier = Modifier.weight(1f)
          )
          OutlinedTextField(
            value = accuracyStr,
            onValueChange = { accuracyStr = it },
            label = { Text("Accuracy %") },
            modifier = Modifier.weight(1f)
          )
        }
        OutlinedTextField(
          value = weakAreas,
          onValueChange = { weakAreas = it },
          label = { Text("Weak Areas / Misconceptions") },
          placeholder = { Text("e.g. Paging calculation formula mistake") },
          modifier = Modifier.fillMaxWidth(),
          maxLines = 2
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val marks = marksStr.toFloatOrNull() ?: 0f
          val total = totalStr.toFloatOrNull() ?: 100f
          val neg = negativeStr.toFloatOrNull() ?: 0f
          val acc = accuracyStr.toFloatOrNull() ?: 0f
          if (name.isNotBlank()) {
            onConfirm(name, type, marks, total, neg, acc, weakAreas)
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = GateEmerald)
      ) {
        Text("Save Result")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}
