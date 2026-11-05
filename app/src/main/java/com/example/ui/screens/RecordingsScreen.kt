package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.model.RadioRecording
import com.example.ui.RadioViewModel
import com.example.ui.theme.LcdAmber
import com.example.ui.theme.LcdCyan
import com.example.ui.theme.LcdGreen
import com.example.ui.theme.LcdRed
import com.example.ui.theme.ShareTechMonoFontFamily
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecordingsScreen(
    recordings: List<RadioRecording>,
    playingRecordingId: String?,
    recordingProgressSec: Int,
    viewModel: RadioViewModel,
    modifier: Modifier = Modifier
) {
    var recordingToDelete by remember { mutableStateOf<RadioRecording?>(null) }
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("recordings_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Radyo Kayıtlarım",
                    style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "İnternetsiz dinlenebilir çevrimdışı WAV ses dosyaları",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF9AA4B2))
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF1E2836))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${recordings.size} Kayıt",
                    style = MaterialTheme.typography.labelSmall.copy(color = LcdCyan, fontWeight = FontWeight.Bold)
                )
            }
        }

        if (recordings.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E2632)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = Color(0xFF8B949E),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Text(
                        text = "Henüz radyo kaydı alınmadı",
                        style = MaterialTheme.typography.titleMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Herhangi bir radyo frekansını dinlerken 'KAYDET' butonuna basarak yayını cihazınıza kaydedebilir ve internet olmadan dilediğiniz zaman tekrar dinleyebilirsiniz.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF8B949E)),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("recordings_lazy_list"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(recordings, key = { it.id }) { rec ->
                    val isPlayingThis = playingRecordingId == rec.id

                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("recording_card_${rec.id}"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isPlayingThis) Color(0xFF122230) else Color(0xFF161C26)
                        ),
                        border = if (isPlayingThis) androidx.compose.foundation.BorderStroke(1.5.dp, LcdCyan) else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF25303E)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Play / Pause button
                                IconButton(
                                    onClick = {
                                        if (isPlayingThis) {
                                            viewModel.stopRecordingPlayback()
                                        } else {
                                            viewModel.playRecording(rec)
                                        }
                                    },
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(if (isPlayingThis) LcdCyan else Color(0xFF222B38))
                                        .testTag("play_recording_btn_${rec.id}")
                                ) {
                                    Icon(
                                        imageVector = if (isPlayingThis) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "Oynat",
                                        tint = if (isPlayingThis) Color(0xFF071118) else Color.White
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                // Details
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = rec.title,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = if (isPlayingThis) LcdCyan else Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = "${rec.stationName} (${rec.frequency} MHz) • ${dateFormat.format(Date(rec.timestamp))}",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF8B949E))
                                    )
                                    val sizeMb = rec.fileSizeBytes / (1024.0 * 1024.0)
                                    Text(
                                        text = "Süre: ${rec.durationSeconds} sn • %.2f MB".format(sizeMb),
                                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF6E7681))
                                    )
                                }

                                // Delete Button
                                IconButton(
                                    onClick = { recordingToDelete = rec },
                                    modifier = Modifier.size(36.dp).testTag("delete_rec_btn_${rec.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Kaydı Sil",
                                        tint = Color(0xFFFF5252),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Progress bar if playing
                            if (isPlayingThis) {
                                Spacer(modifier = Modifier.height(8.dp))
                                val progress = (recordingProgressSec.toFloat() / rec.durationSeconds.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = LcdCyan,
                                    trackColor = Color(0xFF283648)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "%02d:%02d".format(recordingProgressSec / 60, recordingProgressSec % 60),
                                        style = MaterialTheme.typography.labelSmall.copy(color = LcdCyan)
                                    )
                                    Text(
                                        text = "%02d:%02d".format(rec.durationSeconds / 60, rec.durationSeconds % 60),
                                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF8B949E))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (recordingToDelete != null) {
        val target = recordingToDelete!!
        AlertDialog(
            onDismissRequest = { recordingToDelete = null },
            title = { Text("Kaydı Sil", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "'${target.title}' kaydını silmek istediğinizden emin misiniz?",
                    color = Color(0xFFCBD5E1)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteRecording(target.id)
                        recordingToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252), contentColor = Color.White)
                ) {
                    Text("Sil", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { recordingToDelete = null }) {
                    Text("İptal")
                }
            },
            containerColor = Color(0xFF141A24)
        )
    }
}
