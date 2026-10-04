package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.SadhanaEntry
import com.example.ui.theme.SaffronContainer
import com.example.ui.theme.SaffronLight
import com.example.ui.theme.SaffronPrimary
import com.example.ui.viewmodel.MainViewModel

@Composable
fun SadhanaScreen(
  viewModel: MainViewModel,
  modifier: Modifier = Modifier
) {
  val sadhanaEntry by viewModel.todaySadhana.collectAsState()
  val currentBead by viewModel.currentBead.collectAsState()

  val entry = sadhanaEntry ?: SadhanaEntry(date = viewModel.todayDateString)
  val sadhanaScore = entry.calculateScore()

  var reflectionText by remember(entry.reflectionNote) { mutableStateOf(entry.reflectionNote) }
  var isEditingReflection by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Title Banner
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SaffronContainer),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column {
            Text(
              text = "Krishna Consciousness Sadhana",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
              text = "Daily Chanting, Hearing & Scripture Study",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
          }
          Surface(
            shape = CircleShape,
            color = SaffronPrimary,
            modifier = Modifier.size(48.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Text(
                text = "$sadhanaScore%",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp
              )
            }
          }
        }
      }
    }

    // 2. Interactive Japa Mala Chanting Counter
    item {
      JapaMalaCard(
        currentBead = currentBead,
        japaRounds = entry.japaRounds,
        targetRounds = entry.targetRounds,
        onBeadTap = { viewModel.onMalaBeadTapped() },
        onResetBead = { viewModel.resetBeadCounter() },
        onIncrementRound = { viewModel.incrementJapaRoundQuick() },
        onDecrementRound = { viewModel.decrementJapaRoundQuick() }
      )
    }

    // 3. Sacred Maha-Mantra Reference
    item {
      MahaMantraDisplayCard()
    }

    // 4. Scripture Reading & Hearing Section
    item {
      Text(
        text = "Sravanam & Kirtanam (Study & Hearing)",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold
      )
    }

    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          // Bhagavad Gita Reading
          ReadingCounterRow(
            label = "Bhagavad Gita Reading",
            minutes = entry.gitaReadingMinutes,
            onMinutesChange = { newMins ->
              viewModel.updateSadhanaEntry(entry.copy(gitaReadingMinutes = newMins.coerceAtLeast(0)))
            }
          )

          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

          // Srimad Bhagavatam Reading
          ReadingCounterRow(
            label = "Srimad Bhagavatam Reading",
            minutes = entry.bhagavatamReadingMinutes,
            onMinutesChange = { newMins ->
              viewModel.updateSadhanaEntry(entry.copy(bhagavatamReadingMinutes = newMins.coerceAtLeast(0)))
            }
          )

          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

          // Lecture / Kirtan Hearing
          ReadingCounterRow(
            label = "Prabhupada / Guru Lecture Hearing",
            minutes = entry.lectureMinutes,
            onMinutesChange = { newMins ->
              viewModel.updateSadhanaEntry(entry.copy(lectureMinutes = newMins.coerceAtLeast(0)))
            }
          )
        }
      }
    }

    // 5. Daily Discipline & Habits Checklist
    item {
      Text(
        text = "Daily Regulative Principles",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold
      )
    }

    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          // Mangala / Early Wake Up
          HabitToggleRow(
            icon = Icons.Default.WbSunny,
            title = "Woke up before 6:00 AM (Brahma Muhurta)",
            subtitle = "Time: ${entry.wakingTime}",
            isChecked = entry.wokeBefore6Am,
            onCheckedChange = { checked ->
              viewModel.updateSadhanaEntry(entry.copy(wokeBefore6Am = checked))
            }
          )

          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

          // Aarti & Puja Done
          HabitToggleRow(
            icon = Icons.Default.SelfImprovement,
            title = "Morning / Evening Aarti & Deity Worship",
            subtitle = "Offered lamp & prayers to Sri Sri Radha Krishna",
            isChecked = entry.aartiDone,
            onCheckedChange = { checked ->
              viewModel.updateSadhanaEntry(entry.copy(aartiDone = checked))
            }
          )

          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

          // Prasadam Only
          HabitToggleRow(
            icon = Icons.Default.Favorite,
            title = "Honored Sanctified Krishna Prasadam Only",
            subtitle = "Pure sattvic food offered to Lord Krishna",
            isChecked = entry.prasadamOnly,
            onCheckedChange = { checked ->
              viewModel.updateSadhanaEntry(entry.copy(prasadamOnly = checked))
            }
          )

          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

          // Vaishnava Association
          HabitToggleRow(
            icon = Icons.Default.MenuBook,
            title = "Vaishnava Association / Satsang",
            subtitle = "Connected with devotees or attended temple program",
            isChecked = entry.associationDone,
            onCheckedChange = { checked ->
              viewModel.updateSadhanaEntry(entry.copy(associationDone = checked))
            }
          )
        }
      }
    }

    // 6. Daily Spiritual Reflection / Gratitude
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = "Daily Reflection & Gratitude",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold
            )
            IconButton(
              onClick = { isEditingReflection = !isEditingReflection },
              modifier = Modifier.size(32.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit reflection",
                tint = SaffronPrimary,
                modifier = Modifier.size(18.dp)
              )
            }
          }

          if (isEditingReflection) {
            OutlinedTextField(
              value = reflectionText,
              onValueChange = { reflectionText = it },
              placeholder = { Text("Write your realizations, gratitude to Krishna, or chanting focus...") },
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
              minLines = 3
            )
            Button(
              onClick = {
                viewModel.updateSadhanaEntry(entry.copy(reflectionNote = reflectionText))
                isEditingReflection = false
              },
              colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.align(Alignment.End)
            ) {
              Text("Save Reflection")
            }
          } else {
            Text(
              text = if (entry.reflectionNote.isNotBlank()) entry.reflectionNote else "No notes logged for today. Tap edit to write realizations.",
              style = MaterialTheme.typography.bodyMedium,
              fontStyle = if (entry.reflectionNote.isBlank()) FontStyle.Italic else FontStyle.Normal,
              color = if (entry.reflectionNote.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.padding(vertical = 6.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
fun JapaMalaCard(
  currentBead: Int,
  japaRounds: Int,
  targetRounds: Int,
  onBeadTap: () -> Unit,
  onResetBead: () -> Unit,
  onIncrementRound: () -> Unit,
  onDecrementRound: () -> Unit
) {
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
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Japa Mala Counter",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "108 Beads = 1 Round",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        // Rounds pill
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = SaffronContainer,
          modifier = Modifier.padding(4.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            IconButton(
              onClick = onDecrementRound,
              modifier = Modifier.size(24.dp)
            ) {
              Icon(Icons.Default.Remove, contentDescription = "Decrement", modifier = Modifier.size(16.dp))
            }
            Text(
              text = "$japaRounds / $targetRounds",
              fontWeight = FontWeight.Bold,
              color = SaffronPrimary,
              modifier = Modifier.padding(horizontal = 6.dp)
            )
            IconButton(
              onClick = onIncrementRound,
              modifier = Modifier.size(24.dp)
            ) {
              Icon(Icons.Default.Add, contentDescription = "Increment", modifier = Modifier.size(16.dp))
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Tap to Chant Big Bead Button
      val beadProgress = currentBead.toFloat() / 108f
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(180.dp)
      ) {
        CircularProgressIndicator(
          progress = { beadProgress },
          modifier = Modifier.size(180.dp),
          strokeWidth = 10.dp,
          color = SaffronPrimary,
          trackColor = SaffronContainer
        )

        Surface(
          shape = CircleShape,
          color = SaffronPrimary,
          shadowElevation = 6.dp,
          modifier = Modifier
            .size(140.dp)
            .clickable { onBeadTap() }
            .testTag("chant_bead_button")
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
          ) {
            Text(
              text = "$currentBead",
              fontSize = 36.sp,
              fontWeight = FontWeight.ExtraBold,
              color = Color.White
            )
            Text(
              text = "of 108 beads",
              fontSize = 12.sp,
              color = Color.White.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "TAP TO CHANT",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp,
              color = Color(0xFFFEF3C7)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        OutlinedButton(
          onClick = onResetBead,
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.height(36.dp)
        ) {
          Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Reset Bead", fontSize = 12.sp)
        }

        Button(
          onClick = onIncrementRound,
          shape = RoundedCornerShape(8.dp),
          colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
          modifier = Modifier.height(36.dp)
        ) {
          Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("+1 Round Done", fontSize = 12.sp)
        }
      }
    }
  }
}

@Composable
fun MahaMantraDisplayCard() {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = "हरे कृष्ण हरे कृष्ण कृष्ण कृष्ण हरे हरे",
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = SaffronPrimary,
        textAlign = TextAlign.Center
      )
      Text(
        text = "हरे राम हरे राम राम राम हरे हरे",
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = SaffronPrimary,
        textAlign = TextAlign.Center
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = "Hare Krishna Hare Krishna Krishna Krishna Hare Hare\nHare Rama Hare Rama Rama Rama Hare Hare",
        fontSize = 12.sp,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

@Composable
fun ReadingCounterRow(
  label: String,
  minutes: Int,
  onMinutesChange: (Int) -> Unit
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodyMedium,
      fontWeight = FontWeight.Medium,
      modifier = Modifier.weight(1f)
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
      IconButton(
        onClick = { onMinutesChange(minutes - 5) },
        modifier = Modifier.size(32.dp)
      ) {
        Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
      }

      Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.padding(horizontal = 4.dp)
      ) {
        Text(
          text = "$minutes min",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
      }

      IconButton(
        onClick = { onMinutesChange(minutes + 5) },
        modifier = Modifier.size(32.dp)
      ) {
        Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
      }
    }
  }
}

@Composable
fun HabitToggleRow(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  subtitle: String,
  isChecked: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onCheckedChange(!isChecked) },
    verticalAlignment = Alignment.CenterVertically
  ) {
    Surface(
      shape = CircleShape,
      color = if (isChecked) SaffronContainer else MaterialTheme.colorScheme.surfaceVariant,
      modifier = Modifier.size(36.dp)
    ) {
      Box(contentAlignment = Alignment.Center) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = if (isChecked) SaffronPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(20.dp)
        )
      }
    }

    Spacer(modifier = Modifier.width(12.dp))

    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }

    Checkbox(
      checked = isChecked,
      onCheckedChange = onCheckedChange,
      colors = CheckboxDefaults.colors(checkedColor = SaffronPrimary)
    )
  }
}
