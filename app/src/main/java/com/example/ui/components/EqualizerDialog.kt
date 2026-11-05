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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import com.example.model.EqualizerPreset
import com.example.ui.theme.LcdAmber
import com.example.ui.theme.LcdCyan

@Composable
fun EqualizerDialog(
    currentPreset: EqualizerPreset,
    onSelectPreset: (EqualizerPreset) -> Unit,
    onDismiss: () -> Unit
) {
    var selected by remember { mutableStateOf(currentPreset) }
    val bandLabels = listOf("60Hz", "230Hz", "910Hz", "3.6kHz", "14kHz")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Ses Ekolayzerı & FX",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Önceden Tanımlı Ses Profilleri",
                    style = MaterialTheme.typography.labelMedium.copy(color = Color(0xFF9AA4B2))
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(EqualizerPreset.PRESETS) { preset ->
                        val isSel = selected.name == preset.name
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) Color(0xFF004654) else Color(0xFF1E2632))
                                .border(1.dp, if (isSel) LcdCyan else Color(0xFF323E50), RoundedCornerShape(8.dp))
                                .clickable {
                                    selected = preset
                                    onSelectPreset(preset)
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = preset.name,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSel) LcdCyan else Color(0xFFD0D7DE),
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Band sliders display
                Text(
                    text = "5-Bant Frekans Düzeyi",
                    style = MaterialTheme.typography.labelMedium.copy(color = Color(0xFF9AA4B2))
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    selected.bands.forEachIndexed { i, value ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = bandLabels.getOrElse(i) { "B${i + 1}" },
                                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFCBD5E1)),
                                modifier = Modifier.width(52.dp)
                            )
                            Slider(
                                value = value.toFloat(),
                                onValueChange = {},
                                valueRange = -10f..10f,
                                enabled = false,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    disabledThumbColor = LcdCyan,
                                    disabledActiveTrackColor = LcdCyan,
                                    disabledInactiveTrackColor = Color(0xFF283648)
                                )
                            )
                            Text(
                                text = "${if (value > 0) "+" else ""}$value dB",
                                style = MaterialTheme.typography.labelSmall.copy(color = LcdAmber),
                                modifier = Modifier.width(44.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = LcdCyan, contentColor = Color(0xFF0A151D)),
                modifier = Modifier.testTag("equalizer_confirm_button")
            ) {
                Text("Tamam", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = Color(0xFF141A24)
    )
}
