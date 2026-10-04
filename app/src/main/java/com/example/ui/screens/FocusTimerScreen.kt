package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.StudySession
import com.example.ui.theme.GateContainer
import com.example.ui.theme.GateEmerald
import com.example.ui.theme.PeacockNavy
import com.example.ui.theme.SaffronContainer
import com.example.ui.theme.SaffronPrimary
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FocusTimerScreen(
  viewModel: MainViewModel,
  modifier: Modifier = Modifier
) {
  val timerSeconds by viewModel.timerSeconds.collectAsState()
  val isRunning by viewModel.isTimerRunning.collectAsState()
  val selectedCategory by viewModel.timerCategory.collectAsState()
  val selectedTopic by viewModel.timerTopic.collectAsState()
  val todaySessions by viewModel.todayStudySessions.collectAsState()
  val recentSessions by viewModel.recentStudySessions.collectAsState()

  var showManualLogDialog by remember { mutableStateOf(false) }

  val minutes = timerSeconds / 60
  val seconds = timerSeconds % 60
  val formattedTime = String.format(Locale.US, "%02d:%02d", minutes, seconds)

  val totalTodayMinutes = todaySessions.sumOf { it.durationMinutes }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Timer Card
    item {
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "Sankalp Focus Timer",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Deep work tapasya for GATE, College or Scripture study",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(16.dp))

          // Category Chips
          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 8.dp)
          ) {
            listOf("GATE Prep", "College Work", "Krishna Sadhana").forEach { cat ->
              FilterChip(
                selected = selectedCategory == cat,
                onClick = { viewModel.setTimerCategory(cat) },
                label = { Text(cat, fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = when (cat) {
                    "Krishna Sadhana" -> SaffronContainer
                    "College Work" -> MaterialTheme.colorScheme.secondaryContainer
                    else -> GateContainer
                  }
                )
              )
            }
          }

          // Topic / Subject field
          OutlinedTextField(
            value = selectedTopic,
            onValueChange = { viewModel.setTimerTopic(it) },
            label = { Text("Focus Topic / Subject") },
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
            singleLine = true
          )

          Spacer(modifier = Modifier.height(20.dp))

          // Timer Dial
          val timerProgress = (timerSeconds.toFloat() / (25 * 60f)).coerceIn(0f, 1f)
          val dialColor = when (selectedCategory) {
            "Krishna Sadhana" -> SaffronPrimary
            "College Work" -> PeacockNavy
            else -> GateEmerald
          }

          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(190.dp)
          ) {
            CircularProgressIndicator(
              progress = { if (isRunning) timerProgress else 1f },
              modifier = Modifier.size(190.dp),
              strokeWidth = 10.dp,
              color = dialColor,
              trackColor = dialColor.copy(alpha = 0.15f)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = formattedTime,
                fontSize = 44.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = selectedCategory,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = dialColor
              )
            }
          }

          Spacer(modifier = Modifier.height(20.dp))

          // Preset Buttons
          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 16.dp)
          ) {
            listOf(15 to "15m", 25 to "25m (Pomodoro)", 50 to "50m (Deep)").forEach { (mins, label) ->
              OutlinedButton(
                onClick = { viewModel.setTimerDurationMinutes(mins) },
                shape = RoundedCornerShape(8.dp),
                enabled = !isRunning
              ) {
                Text(label, fontSize = 11.sp)
              }
            }
          }

          // Controls
          Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedButton(
              onClick = { viewModel.resetTimer() },
              shape = RoundedCornerShape(12.dp)
            ) {
              Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Reset")
            }

            Button(
              onClick = {
                if (isRunning) viewModel.pauseTimer() else viewModel.startTimer()
              },
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = dialColor),
              modifier = Modifier.testTag("timer_start_pause_button")
            ) {
              Icon(
                imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(if (isRunning) "Pause" else "Start Focus")
            }
          }
        }
      }
    }

    // 2. Today's Summary & Manual Log
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Today's Total Focus Time",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "$totalTodayMinutes minutes (${String.format(Locale.US, "%.1f", totalTodayMinutes / 60f)} hrs)",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.ExtraBold
            )
          }

          Button(
            onClick = { showManualLogDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(8.dp)
          ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Log Offline", fontSize = 11.sp)
          }
        }
      }
    }

    // 3. Recent Sessions History
    item {
      Text(
        text = "Recent Study & Focus Logs",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold
      )
    }

    if (recentSessions.isEmpty()) {
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier.fillMaxWidth()
        ) {
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
              .fillMaxWidth()
              .padding(24.dp)
          ) {
            Text(
              text = "No focus sessions logged yet. Complete a timer session or tap 'Log Offline'.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    } else {
      items(recentSessions.take(10), key = { it.id }) { session ->
        StudySessionItemCard(session = session)
      }
    }
  }

  if (showManualLogDialog) {
    ManualLogStudyDialog(
      onDismiss = { showManualLogDialog = false },
      onConfirm = { cat, topic, mins ->
        viewModel.logManualStudySession(cat, topic, mins)
        showManualLogDialog = false
      }
    )
  }
}

@Composable
fun StudySessionItemCard(session: StudySession) {
  val dateFormatted = remember(session.timestamp) {
    SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(session.timestamp))
  }
  val badgeColor = when (session.category) {
    "Krishna Sadhana" -> SaffronPrimary
    "College Work" -> PeacockNavy
    else -> GateEmerald
  }

  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
          shape = CircleShape,
          color = badgeColor.copy(alpha = 0.15f),
          modifier = Modifier.size(36.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Timer, contentDescription = null, tint = badgeColor, modifier = Modifier.size(18.dp))
          }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = session.subjectOrTopic,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
          )
          Text(
            text = "${session.category} • $dateFormatted",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.padding(start = 8.dp)
      ) {
        Text(
          text = "+${session.durationMinutes} min",
          fontWeight = FontWeight.Bold,
          color = badgeColor,
          fontSize = 12.sp,
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
      }
    }
  }
}

@Composable
fun ManualLogStudyDialog(
  onDismiss: () -> Unit,
  onConfirm: (category: String, topic: String, minutes: Int) -> Unit
) {
  var category by remember { mutableStateOf("GATE Prep") }
  var topic by remember { mutableStateOf("") }
  var minutesStr by remember { mutableStateOf("45") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Log Study / Sadhana Time") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf("GATE Prep", "College Work", "Krishna Sadhana").forEach { cat ->
            FilterChip(
              selected = category == cat,
              onClick = { category = cat },
              label = { Text(cat, fontSize = 10.sp) }
            )
          }
        }
        OutlinedTextField(
          value = topic,
          onValueChange = { topic = it },
          label = { Text("Topic / Subject *") },
          placeholder = { Text("e.g. Operating Systems Paging") },
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = minutesStr,
          onValueChange = { minutesStr = it },
          label = { Text("Minutes Spent *") },
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val mins = minutesStr.toIntOrNull() ?: 30
          if (topic.isNotBlank()) {
            onConfirm(category, topic, mins)
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = GateEmerald)
      ) {
        Text("Save Log")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}
