package dev.nstv.easing.symphony.screen.music

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nstv.easing.symphony.design.Grid
import dev.nstv.easing.symphony.design.TileColor
import dev.nstv.easing.symphony.musicvisualizer.reader.MusicReader.Companion.FFT_BINS
import dev.nstv.easing.symphony.musicvisualizer.reader.MusicReaderWrapper
import dev.nstv.easing.symphony.musicvisualizer.reader.musicPlayerControl
import dev.nstv.easing.symphony.screen.musicFilePath
import easingsymphony.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

private const val amplitudeScale = 10f

// Current implementation is not-optimized
const val showAccumulatedWaveform = false

@OptIn(ExperimentalResourceApi::class, ExperimentalFoundationApi::class, ExperimentalTime::class)
@Composable
fun MusicPropertiesScreen(
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = modifier.padding(Grid.One)) {
        MusicReaderWrapper(
            fileUri = Res.getUri(musicFilePath),
        ) { musicReader ->

            val isPlaying by musicReader.isPlaying.collectAsStateWithLifecycle()
            val frequencies by musicReader.frequencies.collectAsStateWithLifecycle(
                FloatArray(
                    FFT_BINS
                )
            )
            val amplitude by musicReader.amplitude.collectAsStateWithLifecycle(0f)
            val waveform by musicReader.waveform.collectAsStateWithLifecycle(FloatArray(0))
            // Simulated sin wave
//            val waveformData =
//                generateSineWaveExponential(0.01f, 44100, 440f..1000f, 0.8f..1f).toMutableList()
//            for (i in waveformData.size / 2 until waveformData.size / 2 + waveformData.size / 4) {
//                waveformData.removeAt(i)
//            }

            val accumulatedWaveform = remember { mutableStateListOf<Float>() }
            val accumulatedAmplitude = remember { mutableStateListOf<Float>() }
            val bandMagnitudes = frequencies.computeBandMagnitudes()

            if (showAccumulatedWaveform) {
                LaunchedEffect(waveform) {
                    waveform.forEach {
                        accumulatedWaveform.add(it)
                    }
                }
            }

            LaunchedEffect(amplitude) {
                accumulatedAmplitude.add(amplitude)
                println(
                    "accumulatedAmplitude at ${
                        Clock.System.now().toEpochMilliseconds()
                    } size ${accumulatedAmplitude.size}: $accumulatedAmplitude"
                )

            }

            fun restartPlayback() {
                accumulatedAmplitude.clear()
                accumulatedWaveform.clear()
            }

            Column(
                modifier = Modifier.fillMaxWidth().musicPlayerControl(
                    musicReader = musicReader,
                    onPlaybackRestarted = { restartPlayback() }
                )
            ) {

                Text(
                    text = "Waveform",
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.headlineMedium
                )

                Canvas(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                ) {
                    val widthStep = size.width / waveform.size
                    val centerY = size.height / 2f

                    val points = waveform.mapIndexed { index, value ->
                        Offset(
                            x = index * widthStep,
                            y = centerY - value * centerY
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
                        color = TileColor.Green,
                        strokeWidth = 2f,
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth().padding(vertical = Grid.One),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (showAccumulatedWaveform) {
                    Text(
                        text = "Waveform Accumulated",
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.headlineMedium
                    )

                    Canvas(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                    ) {
                        val centerY = size.height / 2f

                        val points = accumulatedWaveform.mapIndexed { index, value ->
                            Offset(
                                x = index * (size.width / accumulatedWaveform.size),
                                y = centerY - value * centerY
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
                            color = TileColor.Green,
                            strokeWidth = 2f,
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth().padding(vertical = Grid.One),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "Amplitude",
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.headlineMedium
                )
                Row(Modifier.fillMaxWidth().weight(1f)) {
                    Box(
                        Modifier.fillMaxHeight().weight(1f)
                    ) {
                        Canvas(Modifier.fillMaxSize()) {
                            val baseRadius = minOf(size.width, size.height) / 2
                            drawCircle(
                                color = TileColor.Blue,
                                radius = baseRadius * amplitude,
                                style = Fill
                            )
                        }
                    }
                    Canvas(
                        modifier = Modifier.fillMaxHeight().weight(2f),
                    ) {
                        val points = accumulatedAmplitude.mapIndexed { index, value ->
                            Offset(
                                x = index * (size.width / accumulatedAmplitude.size),
                                y = (size.height - (value * size.height))
                            )
                        }

                        drawPoints(
                            points = points,
                            pointMode = PointMode.Polygon,
                            cap = StrokeCap.Round,
                            color = TileColor.Blue,
                            strokeWidth = 2f,
                        )
                    }
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
                    modifier = Modifier.fillMaxWidth().weight(1f),
                ) {
                    val width = (size.width / bandMagnitudes.size)
                    for (i in bandMagnitudes.indices) {
                        val x = i * width + width / 2
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
}

fun FloatArray.computeBandMagnitudes(): List<Float> {
    val bands = listOf(
        0..2, 3..7, 8..15, 16..31, 32..63 //, 64..127, 128..minOf(255, fft.lastIndex)
    )
    return bands.map { range ->
        this.slice(range).average().toFloat()
    }
}