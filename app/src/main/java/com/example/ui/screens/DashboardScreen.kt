package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.entity.GitaShloka
import com.example.ui.theme.GateContainer
import com.example.ui.theme.GateEmerald
import com.example.ui.theme.PeacockNavy
import com.example.ui.theme.SaffronContainer
import com.example.ui.theme.SaffronPrimary
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.MainViewModel

@Composable
fun DashboardScreen(
  viewModel: MainViewModel,
  onNavigateToTab: (AppTab) -> Unit,
  modifier: Modifier = Modifier
) {
  val todaySadhana by viewModel.todaySadhana.collectAsState()
  val pendingTasks by viewModel.pendingTasks.collectAsState()
  val attendanceList by viewModel.attendanceList.collectAsState()
  val gateTopics by viewModel.gateTopics.collectAsState()
  val todaySessions by viewModel.todayStudySessions.collectAsState()
  val todayShloka = viewModel.todayShloka

  val japaRounds = todaySadhana?.japaRounds ?: 0
  val targetRounds = todaySadhana?.targetRounds ?: 16
  val sadhanaScore = todaySadhana?.calculateScore() ?: 0

  val totalTodayMinutes = todaySessions.sumOf { it.durationMinutes }
  val masteredGateCount = gateTopics.count { it.status == "Mastered" || it.status.startsWith("Revised") }
  val totalGateCount = gateTopics.size.coerceAtLeast(1)
  val gateProgress = (masteredGateCount.toFloat() / totalGateCount.toFloat())

  val lowAttendanceCount = attendanceList.count { it.currentPercentage < it.targetPercentage }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Hero Visual Banner
    item {
      HeroBannerCard()
    }

    // 2. Today's Date & Sanskrit Motivation
    item {
      DailyHeaderCard(
        dateString = viewModel.todayDateString,
        overallScore = sadhanaScore
      )
    }

    // 3. Shloka of the Day
    item {
      ShlokaOfTheDayCard(shloka = todayShloka)
    }

    // 4. Tri-Pillar Progress Overview Cards
    item {
      Text(
        text = "Today's Tri-Pillar Overview",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
      )
    }

    // Pillar 1: Krishna Consciousness Sadhana
    item {
      PillarOverviewCard(
        title = "Krishna Consciousness Sadhana",
        subtitle = "$japaRounds / $targetRounds Rounds Chanted • Score $sadhanaScore%",
        icon = Icons.Default.SelfImprovement,
        badgeColor = SaffronPrimary,
        containerColor = SaffronContainer,
        progress = (japaRounds.toFloat() / targetRounds.toFloat()).coerceIn(0f, 1f),
        statusText = if (japaRounds >= targetRounds) "16 Rounds Complete! Hari Bol!" else "${targetRounds - japaRounds} rounds remaining today",
        onCardClick = { onNavigateToTab(AppTab.SADHANA) }
      )
    }

    // Pillar 2: College Works & Attendance
    item {
      val attendanceAdvice = if (lowAttendanceCount > 0) "$lowAttendanceCount subject(s) below 75%!" else "All attendance on track (>=75%)"
      PillarOverviewCard(
        title = "College Works & Attendance",
        subtitle = "${pendingTasks.size} Pending Task(s) • $attendanceAdvice",
        icon = Icons.Default.School,
        badgeColor = PeacockNavy,
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        progress = if (pendingTasks.isEmpty()) 1f else 0.5f,
        statusText = if (pendingTasks.isNotEmpty()) "Next due: ${pendingTasks.first().title}" else "All assignments & works up to date!",
        onCardClick = { onNavigateToTab(AppTab.COLLEGE) }
      )
    }

    // Pillar 3: GATE Preparation
    item {
      PillarOverviewCard(
        title = "GATE Exam Preparation",
        subtitle = "$masteredGateCount of $totalGateCount Topics Covered • $totalTodayMinutes min Studied Today",
        icon = Icons.Default.MenuBook,
        badgeColor = GateEmerald,
        containerColor = GateContainer,
        progress = gateProgress,
        statusText = "${(gateProgress * 100).toInt()}% syllabus mastered/revised",
        onCardClick = { onNavigateToTab(AppTab.GATE) }
      )
    }

    // 5. Quick Actions Row
    item {
      QuickActionsRow(
        onChantClick = { onNavigateToTab(AppTab.SADHANA) },
        onStudyTimerClick = { onNavigateToTab(AppTab.FOCUS) },
        onTasksClick = { onNavigateToTab(AppTab.COLLEGE) }
      )
    }

    // 6. Today's Urgent Deadlines Preview
    if (pendingTasks.isNotEmpty()) {
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
                text = "Urgent Academic Deadlines",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "View All (${pendingTasks.size})",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.clickable { onNavigateToTab(AppTab.COLLEGE) }
              )
            }
            Spacer(modifier = Modifier.height(8.dp))
            pendingTasks.take(2).forEach { task ->
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 4.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.DateRange,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Text(
                    text = "${task.courseName} • ${task.priority} Priority",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun HeroBannerCard() {
  Card(
    shape = RoundedCornerShape(20.dp),
    modifier = Modifier
      .fillMaxWidth()
      .height(150.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      Image(
        painter = painterResource(id = R.drawable.hero_banner),
        contentDescription = "Sadhana and Study harmony",
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
      )
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(
              colors = listOf(Color.Transparent, Color(0xCC0F172A))
            )
          )
      )
      Column(
        modifier = Modifier
          .align(Alignment.BottomStart)
          .padding(16.dp)
      ) {
        Text(
          text = "सङ्कल्प • SANKALPA",
          color = Color(0xFFFBBF24),
          fontWeight = FontWeight.ExtraBold,
          fontSize = 18.sp,
          letterSpacing = 1.sp
        )
        Text(
          text = "Spiritual Discipline • Academic Duty • GATE Tapasya",
          color = Color.White,
          style = MaterialTheme.typography.bodySmall,
          fontWeight = FontWeight.Medium
        )
      }
    }
  }
}

@Composable
fun DailyHeaderCard(
  dateString: String,
  overallScore: Int
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column {
        Text(
          text = "Today's Alignment",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.primary,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = dateString,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.ExtraBold
        )
      }

      Surface(
        shape = CircleShape,
        color = SaffronPrimary,
        modifier = Modifier.size(54.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "$overallScore%",
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
            Text(
              text = "Score",
              color = Color.White.copy(alpha = 0.8f),
              fontSize = 9.sp
            )
          }
        }
      }
    }
  }
}

@Composable
fun ShlokaOfTheDayCard(shloka: GitaShloka) {
  var isExpanded by remember { mutableStateOf(false) }

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
    ),
    modifier = Modifier
      .fillMaxWidth()
      .clickable { isExpanded = !isExpanded }
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = SaffronPrimary,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Bhagavad Gita ${shloka.chapter}.${shloka.verse}",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )
        }
        Text(
          text = if (isExpanded) "Hide" else "Read",
          style = MaterialTheme.typography.labelSmall,
          color = SaffronPrimary,
          fontWeight = FontWeight.Bold
        )
      }

      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = shloka.sanskrit,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 22.sp,
        color = MaterialTheme.colorScheme.onPrimaryContainer
      )

      AnimatedVisibility(visible = isExpanded) {
        Column(modifier = Modifier.padding(top = 10.dp)) {
          Text(
            text = "\"${shloka.englishTranslation}\"",
            style = MaterialTheme.typography.bodySmall,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )
          Spacer(modifier = Modifier.height(8.dp))
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = "Practical Takeaway: ${shloka.practicalTakeaway}",
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.padding(8.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
fun PillarOverviewCard(
  title: String,
  subtitle: String,
  icon: ImageVector,
  badgeColor: Color,
  containerColor: Color,
  progress: Float,
  statusText: String,
  onCardClick: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = containerColor),
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onCardClick() }
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            shape = CircleShape,
            color = badgeColor,
            modifier = Modifier.size(36.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
              )
            }
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = title,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = subtitle,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowForward,
          contentDescription = "Go",
          tint = badgeColor,
          modifier = Modifier.size(18.dp)
        )
      }

      Spacer(modifier = Modifier.height(12.dp))
      LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier
          .fillMaxWidth()
          .height(6.dp)
          .clip(RoundedCornerShape(3.dp)),
        color = badgeColor,
        trackColor = badgeColor.copy(alpha = 0.2f)
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = statusText,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

@Composable
fun QuickActionsRow(
  onChantClick: () -> Unit,
  onStudyTimerClick: () -> Unit,
  onTasksClick: () -> Unit
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Button(
      onClick = onChantClick,
      shape = RoundedCornerShape(12.dp),
      colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
      modifier = Modifier
        .weight(1f)
        .testTag("quick_chant_button")
    ) {
      Icon(Icons.Default.SelfImprovement, contentDescription = null, modifier = Modifier.size(18.dp))
      Spacer(modifier = Modifier.width(4.dp))
      Text("Chant", fontSize = 12.sp)
    }

    Button(
      onClick = onStudyTimerClick,
      shape = RoundedCornerShape(12.dp),
      colors = ButtonDefaults.buttonColors(containerColor = GateEmerald),
      modifier = Modifier
        .weight(1f)
        .testTag("quick_timer_button")
    ) {
      Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(18.dp))
      Spacer(modifier = Modifier.width(4.dp))
      Text("Study", fontSize = 12.sp)
    }

    OutlinedButton(
      onClick = onTasksClick,
      shape = RoundedCornerShape(12.dp),
      modifier = Modifier
        .weight(1f)
        .testTag("quick_tasks_button")
    ) {
      Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(18.dp))
      Spacer(modifier = Modifier.width(4.dp))
      Text("College", fontSize = 12.sp)
    }
  }
}
