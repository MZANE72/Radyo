package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RadioState
import com.example.ui.theme.LcdAmber
import com.example.ui.theme.LcdCyan
import com.example.ui.theme.LcdGreen
import com.example.ui.theme.LcdRed

@Composable
fun PlaybackControls(
    radioState: RadioState,
    onTogglePower: () -> Unit,
    onTogglePlay: () -> Unit,
    onStepFrequency: (Float) -> Unit,
    onSeekStation: (Boolean) -> Unit,
    onAutoScan: () -> Unit,
    onToggleRecord: () -> Unit,
    onToggleAntennaBoost: () -> Unit,
    onToggleSquelch: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onToggleMute: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("playback_controls"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Main Tuning Deck (Seek, -0.1, Big Play, +0.1, Seek)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Seek Back
            IconButton(
                onClick = { onSeekStation(false) },
                enabled = radioState.isPoweredOn,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E2632))
                    .testTag("seek_backward_button")
            ) {
                Icon(
                    imageVector = Icons.Default.FastRewind,
                    contentDescription = "Önceki İstasyonu Ara",
                    tint = if (radioState.isPoweredOn) LcdCyan else Color(0xFF485360)
                )
            }

            // Step -0.1 MHz
            FilledIconButton(
                onClick = { onStepFrequency(-0.1f) },
                enabled = radioState.isPoweredOn,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color(0xFF222B38),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .size(48.dp)
                    .testTag("step_minus_button")
            ) {
                Text(
                    text = "-0.1",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                )
            }

            // Central Play/Pause Knob
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                if (radioState.isPlaying) Color(0xFF00E5FF) else Color(0xFF388BFD),
                                if (radioState.isPlaying) Color(0xFF007E8C) else Color(0xFF1E3A5F),
                                Color(0xFF101924)
                            )
                        )
                    )
                    .border(
                        2.5.dp,
                        if (radioState.isPlaying) LcdCyan else Color(0xFF4C6178),
                        CircleShape
                    )
                    .clickable(enabled = radioState.isPoweredOn) { onTogglePlay() }
                    .testTag("play_pause_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (radioState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (radioState.isPlaying) "Radyoyu Duraklat" else "Radyoyu Başlat",
                    tint = if (radioState.isPoweredOn) Color.White else Color(0xFF6B7280),
                    modifier = Modifier.size(38.dp)
                )
            }

            // Step +0.1 MHz
            FilledIconButton(
                onClick = { onStepFrequency(0.1f) },
                enabled = radioState.isPoweredOn,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color(0xFF222B38),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .size(48.dp)
                    .testTag("step_plus_button")
            ) {
                Text(
                    text = "+0.1",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                )
            }

            // Seek Forward
            IconButton(
                onClick = { onSeekStation(true) },
                enabled = radioState.isPoweredOn,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E2632))
                    .testTag("seek_forward_button")
            ) {
                Icon(
                    imageVector = Icons.Default.FastForward,
                    contentDescription = "Sonraki İstasyonu Ara",
                    tint = if (radioState.isPoweredOn) LcdCyan else Color(0xFF485360)
                )
            }
        }

        // Secondary Action Function Pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Power Toggle
            FunctionPill(
                icon = Icons.Default.PowerSettingsNew,
                label = if (radioState.isPoweredOn) "AÇIK" else "KAPALI",
                active = radioState.isPoweredOn,
                activeColor = LcdGreen,
                onClick = onTogglePower,
                modifier = Modifier.weight(1f),
                tag = "power_toggle_button"
            )

            // Auto Scan Band
            FunctionPill(
                icon = Icons.Default.Search,
                label = if (radioState.isScanning) "TARIYOR" else "TARA",
                active = radioState.isScanning,
                activeColor = LcdAmber,
                onClick = onAutoScan,
                modifier = Modifier.weight(1f),
                tag = "auto_scan_button"
            )

            // Record FM Audio
            FunctionPill(
                icon = if (radioState.isRecording) Icons.Default.Stop else Icons.Default.FiberManualRecord,
                label = if (radioState.isRecording) "KAYITTA" else "KAYDET",
                active = radioState.isRecording,
                activeColor = LcdRed,
                onClick = onToggleRecord,
                modifier = Modifier.weight(1f),
                tag = "record_toggle_button"
            )

            // Antensiz Mod (Internal Antenna Boost)
            FunctionPill(
                icon = Icons.Default.Sensors,
                label = "ANTENSİZ",
                active = radioState.internalAntennaBoost,
                activeColor = LcdCyan,
                onClick = onToggleAntennaBoost,
                modifier = Modifier.weight(1f),
                tag = "antenna_boost_button"
            )
        }

        // Tertiary Row: Squelch, Equalizer, Sleep Timer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Squelch Noise Filter
            FunctionPill(
                icon = Icons.Default.GraphicEq,
                label = if (radioState.squelchEnabled) "PARAZİTSİZ" else "STATİK SES",
                active = radioState.squelchEnabled,
                activeColor = LcdCyan,
                onClick = onToggleSquelch,
                modifier = Modifier.weight(1f),
                tag = "squelch_toggle_button"
            )

            // Equalizer
            FunctionPill(
                icon = Icons.Default.Equalizer,
                label = radioState.activeEqualizer.name.take(8),
                active = true,
                activeColor = Color(0xFF64B5F6),
                onClick = onOpenEqualizer,
                modifier = Modifier.weight(1f),
                tag = "equalizer_button"
            )

            // Sleep Timer
            FunctionPill(
                icon = Icons.Default.HourglassTop,
                label = if (radioState.sleepTimerMinutesLeft != null) "${radioState.sleepTimerMinutesLeft}D" else "ZAMANLAYICI",
                active = radioState.sleepTimerMinutesLeft != null,
                activeColor = LcdAmber,
                onClick = onOpenSleepTimer,
                modifier = Modifier.weight(1f),
                tag = "sleep_timer_button"
            )
        }

        // Volume Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF151C26))
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onToggleMute,
                modifier = Modifier.size(36.dp).testTag("mute_button")
            ) {
                Icon(
                    imageVector = if (radioState.isMuted || radioState.volume == 0f) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                    contentDescription = "Sesi Kapat / Aç",
                    tint = if (radioState.isMuted) LcdRed else LcdCyan,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Slider(
                value = if (radioState.isMuted) 0f else radioState.volume,
                onValueChange = onVolumeChange,
                valueRange = 0f..1f,
                modifier = Modifier.weight(1f).testTag("volume_slider"),
                colors = SliderDefaults.colors(
                    thumbColor = LcdCyan,
                    activeTrackColor = LcdCyan,
                    inactiveTrackColor = Color(0xFF283648)
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${(if (radioState.isMuted) 0f else radioState.volume * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFF9AA4B2),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                ),
                modifier = Modifier.width(36.dp)
            )
        }
    }
}

@Composable
private fun FunctionPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    active: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tag: String
) {
    Box(
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (active) activeColor.copy(alpha = 0.16f) else Color(0xFF1B222C))
            .border(
                1.dp,
                if (active) activeColor.copy(alpha = 0.6f) else Color(0xFF2C3746),
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 4.dp)
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (active) activeColor else Color(0xFF8B949E),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (active) activeColor else Color(0xFFC0CAD5),
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                ),
                maxLines = 1
            )
        }
    }
}
