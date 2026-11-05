package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RadioState
import com.example.model.RadioStation
import com.example.ui.RadioViewModel
import com.example.ui.components.EqualizerDialog
import com.example.ui.components.FrequencyTunerWheel
import com.example.ui.components.LcdDisplayPanel
import com.example.ui.components.PlaybackControls
import com.example.ui.components.QuickPresetsRow
import com.example.ui.components.SleepTimerDialog
import com.example.ui.theme.LcdAmber
import com.example.ui.theme.LcdCyan

@Composable
fun TunerScreen(
    radioState: RadioState,
    stations: List<RadioStation>,
    presets: List<Float>,
    viewModel: RadioViewModel,
    modifier: Modifier = Modifier
) {
    var showEqualizer by remember { mutableStateOf(false) }
    var showSleepTimer by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("tuner_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Backlit LCD Digital Display
        LcdDisplayPanel(
            radioState = radioState
        )

        // Horizontal Frequency Tuner Wheel
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FM FREKANS SKALASI",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF8B949E),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    text = "87.5 - 108.0 MHz",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = LcdCyan,
                        fontSize = 11.sp
                    )
                )
            }
            FrequencyTunerWheel(
                currentFrequency = radioState.currentFrequency,
                stations = stations,
                onFrequencyChange = { viewModel.setFrequency(it) }
            )
        }

        // Quick Preset Channels (P1 - P6)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "HAFIZA KANALLARI (P1 - P6)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF8B949E),
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "Kaydetmek için basılı tutun",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF6E7681),
                        fontSize = 10.sp
                    )
                )
            }
            QuickPresetsRow(
                presets = presets,
                currentFrequency = radioState.currentFrequency,
                onSelectPreset = { viewModel.setFrequency(it) },
                onSavePreset = { viewModel.savePreset(it) }
            )
        }

        // Main Playback and Tuning Controls
        PlaybackControls(
            radioState = radioState,
            onTogglePower = { viewModel.togglePower() },
            onTogglePlay = { viewModel.togglePlayPause() },
            onStepFrequency = { viewModel.stepFrequency(it) },
            onSeekStation = { viewModel.seekStation(it) },
            onAutoScan = { viewModel.autoScanFullBand() },
            onToggleRecord = { viewModel.toggleRecord() },
            onToggleAntennaBoost = { viewModel.toggleInternalAntennaBoost() },
            onToggleSquelch = { viewModel.toggleSquelch() },
            onVolumeChange = { viewModel.setVolume(it) },
            onToggleMute = { viewModel.toggleMute() },
            onOpenEqualizer = { showEqualizer = true },
            onOpenSleepTimer = { showSleepTimer = true }
        )

        // Active Station Information Banner
        val currentStation = radioState.activeStation
        if (currentStation != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161F2C)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${currentStation.name} (${currentStation.frequency} MHz)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "${currentStation.genre} • ${currentStation.location} • ${currentStation.currentProgram}",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF9AA4B2))
                        )
                    }
                    IconButton(
                        onClick = { viewModel.toggleFavorite(currentStation.id) },
                        modifier = Modifier.testTag("favorite_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (currentStation.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favoriye Ekle",
                            tint = if (currentStation.isFavorite) LcdAmber else Color(0xFF8B949E),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }

    if (showEqualizer) {
        EqualizerDialog(
            currentPreset = radioState.activeEqualizer,
            onSelectPreset = { viewModel.setEqualizer(it) },
            onDismiss = { showEqualizer = false }
        )
    }

    if (showSleepTimer) {
        SleepTimerDialog(
            currentMinutesLeft = radioState.sleepTimerMinutesLeft,
            onSetTimer = { viewModel.setSleepTimer(it) },
            onDismiss = { showSleepTimer = false }
        )
    }
}
