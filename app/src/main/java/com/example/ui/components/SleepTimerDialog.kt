package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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

@Composable
fun SleepTimerDialog(
    currentMinutesLeft: Int?,
    onSetTimer: (Int?) -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf(15, 30, 45, 60, 90)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Uyku Zamanlayıcısı",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (currentMinutesLeft != null) {
                        "Aktif zamanlayıcı: $currentMinutesLeft dakika sonra radyo otomatik kapanacak."
                    } else {
                        "Radyonun otomatik olarak kapanmasını istediğiniz süreyi seçin:"
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFCBD5E1))
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    options.forEach { mins ->
                        val isSel = currentMinutesLeft == mins
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) Color(0xFF004654) else Color(0xFF1E2632))
                                .border(1.dp, if (isSel) LcdCyan else Color(0xFF323E50), RoundedCornerShape(8.dp))
                                .clickable {
                                    onSetTimer(mins)
                                    onDismiss()
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$mins dk",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSel) LcdCyan else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (currentMinutesLeft != null) {
                OutlinedButton(
                    onClick = {
                        onSetTimer(null)
                        onDismiss()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = LcdAmber),
                    modifier = Modifier.testTag("cancel_timer_button")
                ) {
                    Text("Zamanlayıcıyı İptal Et")
                }
            } else {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = LcdCyan, contentColor = Color(0xFF0A151D)),
                    modifier = Modifier.testTag("close_timer_dialog_button")
                ) {
                    Text("Kapat", fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = Color(0xFF141A24)
    )
}
