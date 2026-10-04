package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.ui.screens.CollegeScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FocusTimerScreen
import com.example.ui.screens.GateScreen
import com.example.ui.screens.SadhanaScreen
import com.example.ui.theme.PeacockNavy
import com.example.ui.theme.SaffronPrimary
import com.example.ui.theme.SankalpaTheme
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
  private val viewModel: MainViewModel by viewModels()

  @OptIn(ExperimentalMaterial3Api::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      SankalpaTheme {
        val currentTab by viewModel.currentTab.collectAsState()
        val userMessage by viewModel.userMessage.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }

        // BackHandler: if on sub-tab, return to Dashboard
        if (currentTab != AppTab.DASHBOARD) {
          BackHandler {
            viewModel.setTab(AppTab.DASHBOARD)
          }
        }

        // Show toast/snackbar notifications
        LaunchedEffect(userMessage) {
          userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
          }
        }

        Scaffold(
          modifier = Modifier.fillMaxSize(),
          topBar = {
            CenterAlignedTopAppBar(
              title = {
                Text(
                  text = when (currentTab) {
                    AppTab.DASHBOARD -> "सङ्कल्प • SANKALPA"
                    AppTab.SADHANA -> "Krishna Sadhana"
                    AppTab.COLLEGE -> "College Works & Attendance"
                    AppTab.GATE -> "GATE Preparation"
                    AppTab.FOCUS -> "Sankalp Focus Timer"
                  },
                  fontWeight = FontWeight.Bold,
                  fontSize = 18.sp,
                  color = MaterialTheme.colorScheme.onSurface
                )
              },
              colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
              )
            )
          },
          bottomBar = {
            NavigationBar(
              containerColor = MaterialTheme.colorScheme.surface,
              contentColor = MaterialTheme.colorScheme.onSurface
            ) {
              val navItems = listOf(
                AppTab.DASHBOARD to Pair(Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
                AppTab.SADHANA to Pair(Icons.Filled.SelfImprovement, Icons.Outlined.SelfImprovement),
                AppTab.COLLEGE to Pair(Icons.Filled.School, Icons.Outlined.School),
                AppTab.GATE to Pair(Icons.Filled.MenuBook, Icons.Outlined.MenuBook),
                AppTab.FOCUS to Pair(Icons.Filled.Timer, Icons.Outlined.Timer)
              )

              navItems.forEach { (tab, icons) ->
                val selected = currentTab == tab
                NavigationBarItem(
                  selected = selected,
                  onClick = { viewModel.setTab(tab) },
                  icon = {
                    Icon(
                      imageVector = if (selected) icons.first else icons.second,
                      contentDescription = tab.title
                    )
                  },
                  label = {
                    Text(
                      text = tab.title,
                      fontSize = 10.sp,
                      fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                  },
                  colors = NavigationBarItemDefaults.colors(
                    indicatorColor = when (tab) {
                      AppTab.SADHANA -> SaffronPrimary.copy(alpha = 0.2f)
                      AppTab.COLLEGE -> PeacockNavy.copy(alpha = 0.2f)
                      else -> MaterialTheme.colorScheme.secondaryContainer
                    }
                  ),
                  modifier = Modifier.testTag("nav_${tab.name.lowercase()}")
                )
              }
            }
          },
          snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
          Box(modifier = Modifier.padding(innerPadding)) {
            when (currentTab) {
              AppTab.DASHBOARD -> DashboardScreen(
                viewModel = viewModel,
                onNavigateToTab = { viewModel.setTab(it) }
              )
              AppTab.SADHANA -> SadhanaScreen(viewModel = viewModel)
              AppTab.COLLEGE -> CollegeScreen(viewModel = viewModel)
              AppTab.GATE -> GateScreen(viewModel = viewModel)
              AppTab.FOCUS -> FocusTimerScreen(viewModel = viewModel)
            }
          }
        }
      }
    }
  }
}
