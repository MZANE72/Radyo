package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LcdAmber
import com.example.ui.theme.LcdCyan
import kotlin.math.abs

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QuickPresetsRow(
    presets: List<Float>,
    currentFrequency: Float,
    onSelectPreset: (Float) -> Unit,
    onSavePreset: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("quick_presets_row"),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        presets.take(6).forEachIndexed { index, freq ->
            val isCurrent = abs(freq - currentFrequency) < 0.05f
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isCurrent) Color(0xFF003847) else Color(0xFF1B222C)
                    )
                    .border(
                        1.5.dp,
                        if (isCurrent) LcdCyan else Color(0xFF2E3846),
                        RoundedCornerShape(8.dp)
                    )
                    .combinedClickable(
                        onClick = { onSelectPreset(freq) },
                        onLongClick = { onSavePreset(index) }
                    )
                    .padding(vertical = 6.dp, horizontal = 2.dp)
                    .testTag("preset_button_${index + 1}"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "P${index + 1}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = if (isCurrent) LcdAmber else Color(0xFF8B949E),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                    Text(
                        text = "%.1f".format(freq),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isCurrent) LcdCyan else Color(0xFFE6EDF3),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }
    }
}
