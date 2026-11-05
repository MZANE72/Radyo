package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RadioState
import com.example.ui.theme.LcdAmber
import com.example.ui.theme.LcdCyan
import com.example.ui.theme.LcdCyanDim
import com.example.ui.theme.LcdGreen
import com.example.ui.theme.LcdRed
import com.example.ui.theme.LcdScreenBackground
import com.example.ui.theme.LcdTextPrimary
import com.example.ui.theme.ShareTechMonoFontFamily

@Composable
fun LcdDisplayPanel(
    radioState: RadioState,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rec_blink")
    val recAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rec_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F1722),
                        LcdScreenBackground,
                        Color(0xFF070E14)
                    )
                )
            )
            .border(2.dp, Brush.verticalGradient(listOf(Color(0xFF384454), Color(0xFF1E2632))), RoundedCornerShape(16.dp))
            .padding(14.dp)
            .testTag("lcd_display_panel")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top Status Bar (Signal bars, Stereo, Squelch, Antenna Mode, Recording)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Signal strength meter
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "SİNYAL",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = LcdCyanDim,
                            fontSize = 10.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    for (i in 1..5) {
                        val active = radioState.isPoweredOn && i <= radioState.signalBars
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height((5 + i * 3).dp)
                                .clip(RoundedCornerShape(1.dp))
                                .background(if (active) LcdGreen else Color(0xFF1B2E28))
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (radioState.isPoweredOn) "${radioState.signalDbm} dBm" else "---",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = LcdCyanDim,
                            fontSize = 9.sp
                        )
                    )
                }

                // Middle Badges (Stereo, Internal Antenna Boost, Squelch)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Internal Antenna badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (radioState.internalAntennaBoost) Color(0xFF00382B) else Color(0xFF1A222C))
                            .border(1.dp, if (radioState.internalAntennaBoost) LcdGreen.copy(alpha = 0.5f) else Color.Transparent, RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (radioState.internalAntennaBoost) "DAHİLİ ANTEN DSP" else "STANDART",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = if (radioState.internalAntennaBoost) LcdGreen else Color(0xFF6B7A8C),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    // Stereo Indicator
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (radioState.isStereo && radioState.activeStation != null) Color(0xFF002B3D) else Color(0xFF1A222C))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "ST",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = if (radioState.isStereo && radioState.activeStation != null) LcdCyan else Color(0xFF4A5568),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    // Recording indicator
                    AnimatedVisibility(visible = radioState.isRecording) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF3E0A0A))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FiberManualRecord,
                                contentDescription = "Kayıt Yapılıyor",
                                tint = LcdRed.copy(alpha = recAlpha),
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            val m = radioState.recordingDurationSeconds / 60
                            val s = radioState.recordingDurationSeconds % 60
                            Text(
                                text = "%02d:%02d".format(m, s),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = LcdRed,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    // Sleep Timer indicator
                    if (radioState.sleepTimerMinutesLeft != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF332600))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "UYKU: ${radioState.sleepTimerMinutesLeft}D",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = LcdAmber,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }

            // Frequency Display & Station Name
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Large Frequency Readout
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = if (radioState.isPoweredOn) "%.1f".format(radioState.currentFrequency) else "OFF",
                        fontFamily = ShareTechMonoFontFamily,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (radioState.isPoweredOn) LcdCyan else Color(0xFF2C3948),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "MHz",
                        fontFamily = ShareTechMonoFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (radioState.isPoweredOn) LcdCyanDim else Color(0xFF2C3948),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Station Callout / RDS PS Name
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    val st = radioState.activeStation
                    if (radioState.isPoweredOn && st != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (st.isFavorite) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Favori",
                                    tint = LcdAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = st.name.uppercase(),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = LcdAmber,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = "${st.genre} • ${st.location}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = LcdCyanDim,
                                fontSize = 11.sp
                            )
                        )
                    } else if (radioState.isPoweredOn) {
                        Text(
                            text = if (radioState.squelchEnabled) "FREKANS ARAMA" else "STATİK PARAZİT",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color(0xFF6B7A8C),
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            text = "Kanal Kilitlenmedi",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color(0xFF455262),
                                fontSize = 10.sp
                            )
                        )
                    } else {
                        Text(
                            text = "GÜÇ KAPALI",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color(0xFF2C3948)
                            )
                        )
                    }
                }
            }

            // RDS RadioText (Program title / song ticker)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF060B10))
                    .border(1.dp, Color(0xFF141F2B), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                val programText = when {
                    !radioState.isPoweredOn -> "Radyo kapalı. Açmak için güç butonuna dokunun."
                    radioState.isScanning -> ">> FM BANDI TARANIYOR... Frekans tespiti yapılıyor <<"
                    radioState.activeStation != null -> "RDS [${radioState.activeStation.name}]: ${radioState.activeStation.currentProgram} • ${radioState.activeStation.description}"
                    radioState.squelchEnabled -> "RDS: Sinyal aranıyor... Frekans düğmesini çevirin veya [TARA] butonuna basın."
                    else -> "RDS: Parazit dinleme modu aktif. İstasyon kilidi bekleniyor."
                }
                Text(
                    text = programText,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = if (radioState.isPoweredOn && radioState.activeStation != null) LcdTextPrimary else Color(0xFF566576),
                        fontSize = 11.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Real-time Audio Spectrum Visualizer (16-bars)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(26.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF04080D))
                    .padding(horizontal = 6.dp, vertical = 3.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                val levels = radioState.audioSpectrumLevels
                for (b in 0 until 16) {
                    val rawLevel = if (radioState.isPoweredOn && radioState.isPlaying) {
                        levels.getOrElse(b) { 0.1f }
                    } else {
                        0.05f
                    }
                    val heightFraction = rawLevel.coerceIn(0.08f, 1.0f)

                    val barColor = when {
                        heightFraction > 0.85f -> LcdRed
                        heightFraction > 0.65f -> LcdAmber
                        else -> LcdCyan
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 1.dp)
                            .fillMaxHeight(heightFraction)
                            .clip(RoundedCornerShape(topStart = 1.dp, topEnd = 1.dp))
                            .background(barColor)
                    )
                }
            }
        }
    }
}
