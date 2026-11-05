package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LcdCyan

@Composable
fun AddStationDialog(
    initialFrequency: Float,
    onAddStation: (name: String, freq: Float, genre: String, location: String) -> Unit,
    onDismiss: () -> Unit
) {
    var freqText by remember { mutableStateOf("%.1f".format(initialFrequency)) }
    var nameText by remember { mutableStateOf("") }
    var genreText by remember { mutableStateOf("Yerel Pop") }
    var locationText by remember { mutableStateOf("Yerel") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Yeni Yerel İstasyon Ekle",
                style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = freqText,
                    onValueChange = {
                        freqText = it
                        errorMessage = null
                    },
                    label = { Text("Frekans (MHz e.g. 95.5)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("custom_freq_input")
                )

                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text("İstasyon Adı (e.g. Radyo Ege)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("custom_name_input")
                )

                OutlinedTextField(
                    value = genreText,
                    onValueChange = { genreText = it },
                    label = { Text("Müzik Türü (e.g. Pop, Haber)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("custom_genre_input")
                )

                OutlinedTextField(
                    value = locationText,
                    onValueChange = { locationText = it },
                    label = { Text("Şehir / Bölge") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("custom_location_input")
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFFF5252))
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = freqText.replace(',', '.').toFloatOrNull()
                    if (parsed == null || parsed < 87.5f || parsed > 108.0f) {
                        errorMessage = "Geçerli bir FM frekansı girin (87.5 - 108.0 MHz)"
                    } else {
                        onAddStation(nameText, parsed, genreText, locationText)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = LcdCyan, contentColor = Color(0xFF0A151D)),
                modifier = Modifier.testTag("save_custom_station_button")
            ) {
                Text("Ekle & Kaydet", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Vazgeç")
            }
        },
        containerColor = Color(0xFF141A24)
    )
}
