package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Radio
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.RadioViewModel
import com.example.ui.ScreenTab
import com.example.ui.components.HardwareStatusSheet
import com.example.ui.screens.RecordingsScreen
import com.example.ui.screens.StationsScreen
import com.example.ui.screens.TunerScreen
import com.example.ui.theme.LcdAmber
import com.example.ui.theme.LcdCyan
import com.example.ui.theme.LcdGreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.RadioBlack
import com.example.ui.theme.ShareTechMonoFontFamily

class MainActivity : ComponentActivity() {

    private val radioViewModel: RadioViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val radioState by radioViewModel.radioState.collectAsState()
                val currentScreen by radioViewModel.currentScreen.collectAsState()
                val stations by radioViewModel.filteredStations.collectAsState()
                val allStations by radioViewModel.stations.collectAsState()
                val presets by radioViewModel.presets.collectAsState()
                val recordings by radioViewModel.recordings.collectAsState()
                val searchQuery by radioViewModel.searchQuery.collectAsState()
                val selectedCategory by radioViewModel.selectedCategory.collectAsState()
                val playingRecId by radioViewModel.playingRecordingId.collectAsState()
                val recProgress by radioViewModel.recordingProgressSec.collectAsState()
                val hwReport by radioViewModel.hardwareReport.collectAsState()

                BackHandler(enabled = currentScreen != ScreenTab.TUNER) {
                    radioViewModel.setScreen(ScreenTab.TUNER)
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize().background(RadioBlack),
                    topBar = {
                        TopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Radio,
                                        contentDescription = null,
                                        tint = LcdCyan,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "FM RADYO",
                                        fontFamily = ShareTechMonoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        color = Color.White,
                                        fontSize = 20.sp
                                    )
                                }
                            },
                            actions = {
                                // Antenna Mode Pill
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (radioState.internalAntennaBoost) Color(0xFF00382B) else Color(0xFF1E2836))
                                        .border(1.dp, if (radioState.internalAntennaBoost) LcdGreen else Color(0xFF2E3D4F), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(if (radioState.internalAntennaBoost) LcdGreen else LcdAmber)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = if (radioState.internalAntennaBoost) "ANTENSİZ ÇEVRİMDIŞI" else "STANDART FM",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (radioState.internalAntennaBoost) LcdGreen else Color(0xFFCAD5E2),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color(0xFF0D1219),
                                titleContentColor = Color.White
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = Color(0xFF0B1017),
                            contentColor = Color.White
                        ) {
                            NavigationBarItem(
                                selected = currentScreen == ScreenTab.TUNER,
                                onClick = { radioViewModel.setScreen(ScreenTab.TUNER) },
                                icon = {
                                    Icon(
                                        imageVector = if (currentScreen == ScreenTab.TUNER) Icons.Filled.Tune else Icons.Outlined.Tune,
                                        contentDescription = "Radyo Alıcısı"
                                    )
                                },
                                label = { Text("Alıcı") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF071118),
                                    selectedTextColor = LcdCyan,
                                    indicatorColor = LcdCyan,
                                    unselectedIconColor = Color(0xFF8B949E),
                                    unselectedTextColor = Color(0xFF8B949E)
                                ),
                                modifier = Modifier.testTag("nav_tuner_tab")
                            )

                            NavigationBarItem(
                                selected = currentScreen == ScreenTab.STATIONS,
                                onClick = { radioViewModel.setScreen(ScreenTab.STATIONS) },
                                icon = {
                                    Icon(
                                        imageVector = if (currentScreen == ScreenTab.STATIONS) Icons.Filled.Radio else Icons.Outlined.Radio,
                                        contentDescription = "İstasyonlar"
                                    )
                                },
                                label = { Text("İstasyonlar") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF071118),
                                    selectedTextColor = LcdCyan,
                                    indicatorColor = LcdCyan,
                                    unselectedIconColor = Color(0xFF8B949E),
                                    unselectedTextColor = Color(0xFF8B949E)
                                ),
                                modifier = Modifier.testTag("nav_stations_tab")
                            )

                            NavigationBarItem(
                                selected = currentScreen == ScreenTab.RECORDINGS,
                                onClick = { radioViewModel.setScreen(ScreenTab.RECORDINGS) },
                                icon = {
                                    Icon(
                                        imageVector = if (currentScreen == ScreenTab.RECORDINGS) Icons.Filled.Mic else Icons.Outlined.Mic,
                                        contentDescription = "Kayıtlar"
                                    )
                                },
                                label = { Text("Kayıtlar") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF071118),
                                    selectedTextColor = LcdCyan,
                                    indicatorColor = LcdCyan,
                                    unselectedIconColor = Color(0xFF8B949E),
                                    unselectedTextColor = Color(0xFF8B949E)
                                ),
                                modifier = Modifier.testTag("nav_recordings_tab")
                            )

                            NavigationBarItem(
                                selected = currentScreen == ScreenTab.HARDWARE,
                                onClick = {
                                    radioViewModel.refreshHardwareReport()
                                    radioViewModel.setScreen(ScreenTab.HARDWARE)
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (currentScreen == ScreenTab.HARDWARE) Icons.Filled.Info else Icons.Outlined.Info,
                                        contentDescription = "Donanım & Anten Bilgisi"
                                    )
                                },
                                label = { Text("Donanım") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF071118),
                                    selectedTextColor = LcdCyan,
                                    indicatorColor = LcdCyan,
                                    unselectedIconColor = Color(0xFF8B949E),
                                    unselectedTextColor = Color(0xFF8B949E)
                                ),
                                modifier = Modifier.testTag("nav_hardware_tab")
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(RadioBlack)
                    ) {
                        when (currentScreen) {
                            ScreenTab.TUNER -> {
                                TunerScreen(
                                    radioState = radioState,
                                    stations = allStations,
                                    presets = presets,
                                    viewModel = radioViewModel
                                )
                            }
                            ScreenTab.STATIONS -> {
                                StationsScreen(
                                    stations = stations,
                                    radioState = radioState,
                                    searchQuery = searchQuery,
                                    selectedCategory = selectedCategory,
                                    viewModel = radioViewModel
                                )
                            }
                            ScreenTab.RECORDINGS -> {
                                RecordingsScreen(
                                    recordings = recordings,
                                    playingRecordingId = playingRecId,
                                    recordingProgressSec = recProgress,
                                    viewModel = radioViewModel
                                )
                            }
                            ScreenTab.HARDWARE -> {
                                HardwareStatusSheet(
                                    report = hwReport,
                                    antennaBoostActive = radioState.internalAntennaBoost,
                                    onToggleAntennaBoost = { radioViewModel.toggleInternalAntennaBoost() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
