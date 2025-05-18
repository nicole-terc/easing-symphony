package dev.nstv.easing.symphony.screen.music

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import dev.nstv.easing.symphony.design.Grid
import dev.nstv.easing.symphony.design.TileColor
import dev.nstv.easing.symphony.musicvisualizer.reader.MusicPlayer
import dev.nstv.easing.symphony.screen.musicFilePath
import easingsymphony.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun MusicPropertiesScreen(
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(Grid.One)) {
        MusicPlayer(
            fileUri = Res.getUri(musicFilePath),
        ) { fftData, amplitudeData, togglePlayback ->
            val accumulatedAmplitude = remember { mutableStateListOf<Float>() }
            val bandMagnitudes = computeBandMagnitudes(fftData)

            LaunchedEffect(amplitudeData) {
                accumulatedAmplitude.add(amplitudeData)
            }

            Text(
                text = "Amplitude",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.headlineMedium
            )

            Canvas(
                modifier = Modifier.fillMaxWidth().weight(1f),
            ) {
                val points = accumulatedAmplitude.mapIndexed { index, value ->
                    Offset(
                        x = index * (size.width / accumulatedAmplitude.size),
                        y = (size.height - (value * size.height))/2f
                    )
                }

                drawLine(
                    color = TileColor.LightGray,
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width, size.height / 2),
                    strokeWidth = 1f
                )

                drawPoints(
                    points = points,
                    pointMode = PointMode.Polygon,
                    cap = StrokeCap.Round,
                    color = TileColor.Blue,
                    strokeWidth = 2f,
                )
            }

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth().padding(vertical = Grid.One),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Frequency",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.headlineMedium
            )

            Canvas(
                modifier = Modifier.fillMaxWidth().padding(16.dp).weight(1f),
            ) {
                val width = (size.width / bandMagnitudes.size)
                for (i in bandMagnitudes.indices) {
                    val x = i * width
                    val height = size.height * bandMagnitudes[i]
                    drawLine(
                        color = TileColor.Pink,
                        start = Offset(x, size.height),
                        end = Offset(x, size.height - height),
                        strokeWidth = width
                    )
                }

            }
        }
    }
}

fun computeBandMagnitudes(fft: FloatArray): List<Float> {
    val bands = listOf(
        0..2, 3..7, 8..15, 16..31, 32..63 //, 64..127, 128..minOf(255, fft.lastIndex)
    )
    return bands.map { range ->
        fft.slice(range).average().toFloat()
    }
}