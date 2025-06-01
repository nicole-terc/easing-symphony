package dev.nstv.easing.symphony.screen.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import dev.nstv.easing.symphony.design.TileColor
import dev.nstv.easing.symphony.musicvisualizer.reader.MusicReader.Companion.FRAME_DELAY_MILLIS


@Composable
fun WavePhasedAmplitud(
    amplitude: Float,
    numberOfBalls: Int,
    modifier: Modifier = Modifier,
) {
    val savedAmplitude by remember { mutableStateOf(FloatArray(numberOfBalls)) }
    val animationDuration: Int = numberOfBalls * FRAME_DELAY_MILLIS.toInt()

    var counter by remember { mutableStateOf(0) }

    LaunchedEffect(amplitude) {
        counter++
        savedAmplitude[counter % numberOfBalls] = amplitude
    }

    Box(modifier = modifier) {
        WaveCanvas(
            yValues = savedAmplitude,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
fun WaveCanvas(
    yValues: FloatArray,
    modifier: Modifier = Modifier,
    color: Color = TileColor.Orange
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        if (yValues.size < 2) return@Canvas

        val spacing = size.width / (yValues.size - 1)
        val points = yValues.mapIndexed { i, yNorm ->
            Offset(i * spacing, size.height - yNorm * size.height)
        }

        val path = Path().apply {
            moveTo(points[0].x, points[0].y)
            for (i in 1 until points.size - 1) {
                val p1 = points[i]
                val p2 = points[i + 1]
                val control = Offset((p1.x + p2.x) / 2f, (p1.y + p2.y) / 2f)
                quadraticBezierTo(p1.x, p1.y, control.x, control.y)
            }
            // Add last segment
            val last = points.last()
            lineTo(last.x, last.y)
        }

        drawPath(path, color = color, style = Stroke(width = 3f))
    }
}