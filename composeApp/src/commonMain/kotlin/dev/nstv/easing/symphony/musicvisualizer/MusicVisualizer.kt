package dev.nstv.easing.symphony.musicvisualizer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow

@Composable
fun MusicVisualizer(
    modifier: Modifier = Modifier,
    amplitudeFlow: Flow<Float>,
    fftFlow: Flow<FloatArray>,
    waveformFlow: Flow<FloatArray>
) {

    Spacer(modifier = Modifier.height(24.dp))

    Text("Waveform Visualizer")
    WaveformVisualizer(waveformFlow)

    Spacer(modifier = Modifier.height(16.dp))

    Text("FFT Visualizer")
    FftBarsVisualizer(fftFlow)
}

@Composable
fun WaveformVisualizer(waveformFlow: Flow<FloatArray>) {
    val waveform by waveformFlow.collectAsState(initial = FloatArray(0))

    Canvas(modifier = Modifier.fillMaxWidth().height(100.dp)) {
        if (waveform.isEmpty()) return@Canvas

        val widthStep = size.width / waveform.size
        val centerY = size.height / 2

        val path = Path().apply {
            moveTo(0f, centerY)
            waveform.forEachIndexed { i, value ->
                val x = i * widthStep
                val y = centerY - (value * centerY)
                lineTo(x, y)
            }
        }

        drawPath(path, color = Color.Green, style = Stroke(width = 2.dp.toPx()))
    }
}

@Composable
fun FftBarsVisualizer(fftFlow: Flow<FloatArray>) {
    val fft by fftFlow.collectAsState(initial = FloatArray(0))

    Canvas(modifier = Modifier.fillMaxWidth().height(100.dp)) {
        if (fft.isEmpty()) return@Canvas

        val barWidth = size.width / fft.size
        fft.forEachIndexed { i, magnitude ->
            val barHeight = magnitude.coerceIn(0f, size.height)
            drawRect(
                color = Color.Magenta,
                topLeft = Offset(i * barWidth, size.height - barHeight),
                size = Size(barWidth * 0.8f, barHeight)
            )
        }
    }
}