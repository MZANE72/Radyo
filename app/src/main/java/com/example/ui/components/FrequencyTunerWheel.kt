package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.RadioStation
import com.example.ui.theme.LcdAmber
import com.example.ui.theme.LcdCyan
import com.example.ui.theme.TunerNeedleColor
import com.example.ui.theme.TunerScaleMark
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun FrequencyTunerWheel(
    currentFrequency: Float,
    stations: List<RadioStation>,
    onFrequencyChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentFreqState = rememberUpdatedState(currentFrequency)
    var dragAccumulator by remember { mutableFloatStateOf(0f) }

    val minFreq = 87.5f
    val maxFreq = 108.0f
    val dpPerMhz = 120f // pixels per MHz

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(84.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF141922),
                        Color(0xFF0F141C),
                        Color(0xFF141922)
                    )
                )
            )
            .border(1.5.dp, Color(0xFF283444), RoundedCornerShape(12.dp))
            .testTag("frequency_tuner_wheel")
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { dragAccumulator = 0f },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        // Dragging right moves scale right, so tuned frequency decreases; dragging left increases
                        dragAccumulator += dragAmount.x
                        val deltaMhz = -dragAmount.x / dpPerMhz
                        val target = (currentFreqState.value + deltaMhz).coerceIn(minFreq, maxFreq)
                        val rounded = ((target * 10f).roundToInt() / 10f)
                        if (rounded != currentFreqState.value) {
                            onFrequencyChange(rounded)
                        }
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val centerX = canvasWidth / 2f
            val freq = currentFreqState.value

            val scaleCenterY = canvasHeight / 2f

            // Draw graduated frequency ticks
            // Range to draw on screen
            val visibleMhzSpan = (canvasWidth / dpPerMhz) / 2f + 0.5f
            val startMhz = ((freq - visibleMhzSpan) * 10).toInt() / 10f
            val endMhz = ((freq + visibleMhzSpan) * 10).toInt() / 10f

            val clampedStart = startMhz.coerceAtLeast(minFreq)
            val clampedEnd = endMhz.coerceAtMost(maxFreq)

            var step = ((clampedStart * 10).roundToInt())
            val maxStep = ((clampedEnd * 10).roundToInt())

            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#9AA4B2")
                textSize = 30f
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
                typeface = android.graphics.Typeface.MONOSPACE
            }

            while (step <= maxStep) {
                val currentTickMhz = step / 10f
                val dx = (currentTickMhz - freq) * dpPerMhz
                val tickX = centerX + dx

                if (tickX in -20f..(canvasWidth + 20f)) {
                    val isIntegerMhz = step % 10 == 0
                    val isHalfMhz = step % 5 == 0 && !isIntegerMhz

                    val (tickHeight, tickColor, strokeWidth) = when {
                        isIntegerMhz -> Triple(32.dp.toPx(), Color(0xFFD0D7DE), 2.5f)
                        isHalfMhz -> Triple(22.dp.toPx(), Color(0xFF8B949E), 1.8f)
                        else -> Triple(14.dp.toPx(), Color(0xFF484F58), 1.2f)
                    }

                    val startY = 10f
                    val endY = startY + tickHeight

                    drawLine(
                        color = tickColor,
                        start = Offset(tickX, startY),
                        end = Offset(tickX, endY),
                        strokeWidth = strokeWidth
                    )

                    // Draw integer MHz number labels at bottom
                    if (isIntegerMhz) {
                        drawIntoCanvas { canvas ->
                            canvas.nativeCanvas.drawText(
                                "${currentTickMhz.toInt()}",
                                tickX,
                                canvasHeight - 14f,
                                textPaint
                            )
                        }
                    }
                }
                step++
            }

            // Draw Station dots along the band
            for (st in stations) {
                val dx = (st.frequency - freq) * dpPerMhz
                val stX = centerX + dx
                if (stX in 0f..canvasWidth) {
                    val dotColor = if (st.isFavorite) LcdAmber else LcdCyan
                    drawCircle(
                        color = dotColor,
                        radius = 4.dp.toPx(),
                        center = Offset(stX, 48f)
                    )
                }
            }

            // Draw subtle vignette gradient on left and right edges for depth
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF0F141C),
                        Color.Transparent,
                        Color.Transparent,
                        Color(0xFF0F141C)
                    )
                )
            )

            // Center tuning needle (Glowing red/amber needle line)
            // Outer needle glow
            drawLine(
                color = TunerNeedleColor.copy(alpha = 0.35f),
                start = Offset(centerX, 0f),
                end = Offset(centerX, canvasHeight),
                strokeWidth = 6.dp.toPx()
            )
            // Core needle
            drawLine(
                color = TunerNeedleColor,
                start = Offset(centerX, 0f),
                end = Offset(centerX, canvasHeight),
                strokeWidth = 2.dp.toPx()
            )

            // Top and Bottom indicator triangles
            val triangleSize = 8.dp.toPx()
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(centerX - triangleSize / 2, 0f)
                lineTo(centerX + triangleSize / 2, 0f)
                lineTo(centerX, triangleSize)
                close()

                moveTo(centerX - triangleSize / 2, canvasHeight)
                lineTo(centerX + triangleSize / 2, canvasHeight)
                lineTo(centerX, canvasHeight - triangleSize)
                close()
            }
            drawPath(path, TunerNeedleColor)
        }
    }
}
