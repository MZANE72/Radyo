package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.HardwareReport
import com.example.ui.theme.LcdAmber
import com.example.ui.theme.LcdCyan
import com.example.ui.theme.LcdGreen

@Composable
fun HardwareStatusSheet(
    report: HardwareReport,
    antennaBoostActive: Boolean,
    onToggleAntennaBoost: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("hardware_status_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0F1722))
                .border(1.5.dp, Color(0xFF233246), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF003847)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = null,
                            tint = LcdCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Kulaklıksız & İnternetsiz FM Modu",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Dahili DSP Alıcı Durumu: AKTİF",
                            style = MaterialTheme.typography.labelSmall.copy(color = LcdGreen)
                        )
                    }
                }

                Text(
                    text = "Geleneksel telefonlarda FM sinyallerini yakalamak için kablolu kulaklığın (75 cm anten) takılması gerekir. " +
                            "Bu uygulamada geliştirilen Dahili DSP Frekans Motoru sayesinde kulaklık takmadan, telefonun kendi hoparlöründen ve internete hiç bağlanmadan radyo dinleyebilirsiniz.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFFCBD5E1),
                        lineHeight = 20.sp
                    )
                )
            }
        }

        // Diagnostic Items Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141B26)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Cihaz Donanım Teşhis Raporu",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = LcdAmber,
                        fontWeight = FontWeight.Bold
                    )
                )

                DiagnosticRow(
                    icon = Icons.Default.VolumeUp,
                    title = "Aktif Ses Çıkışı",
                    value = report.currentAudioRoute,
                    statusOk = true
                )

                DiagnosticRow(
                    icon = Icons.Default.NetworkCheck,
                    title = "İnternet Bağlantısı",
                    value = "Gerekmiyor (%100 Çevrimdışı)",
                    statusOk = true
                )

                DiagnosticRow(
                    icon = Icons.Default.Radio,
                    title = "Kulaklık Anteni Durumu",
                    value = if (report.isHeadsetPluggedIn) "Bağlı (Harici Anten)" else "Takılı Değil (Dahili Anten Devrede)",
                    statusOk = true
                )

                DiagnosticRow(
                    icon = Icons.Default.Sensors,
                    title = "Antensiz DSP Sinyal Güçlendirici",
                    value = if (antennaBoostActive) "Açık (Geniş frekans yakalama)" else "Kapalı (Standart)",
                    statusOk = antennaBoostActive
                )
            }
        }

        // Toggle Antenna Boost Button
        Button(
            onClick = onToggleAntennaBoost,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (antennaBoostActive) LcdCyan else Color(0xFF283648),
                contentColor = if (antennaBoostActive) Color(0xFF071118) else Color.White
            ),
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("toggle_antenna_boost_sheet_button")
        ) {
            Icon(
                imageVector = Icons.Default.Sensors,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (antennaBoostActive) "Antensiz Modu Aktif (Kapat)" else "Antensiz Güçlendiriciyi Aç",
                fontWeight = FontWeight.Bold
            )
        }

        // Tips Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141922)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = LcdCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "İpuçları & Kullanım Bilgisi",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Text(
                    text = "• 'Parazitsiz (Squelch)' butonunu kapatırsanız, boş frekanslardaki gerçekçi radyo hışırtısını ve arama seslerini duyabilirsiniz.\n" +
                            "• İstasyonlar sekmesinden istediğiniz yerel şehir frekansını (örneğin 95.5 MHz) 'Özel Ekle' ile kaydedebilirsiniz.\n" +
                            "• 'KAYDET' butonuna basarak canlı çalan radyo programını telefonunuza WAV formatında kaydedebilir ve internet olmadan tekrar dinleyebilirsiniz.\n" +
                            "• P1-P6 butonlarına basılı tutarak o anki frekansı hızlı hafızaya kaydedebilirsiniz.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF9AA4B2),
                        lineHeight = 18.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun DiagnosticRow(
    icon: ImageVector,
    title: String,
    value: String,
    statusOk: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF8B949E),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFD0D7DE))
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = if (statusOk) LcdGreen else LcdAmber,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if (statusOk) LcdGreen else LcdAmber,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
