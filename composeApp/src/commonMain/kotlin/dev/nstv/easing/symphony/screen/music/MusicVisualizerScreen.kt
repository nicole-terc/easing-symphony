package dev.nstv.easing.symphony.screen.music

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nstv.easing.symphony.musicvisualizer.reader.provideMusicReader
import dev.nstv.easing.symphony.musicvisualizer.MusicVisualizer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun MusicVisualizerScreen(modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val musicReader = provideMusicReader(false)
    val amplitudeFlow = remember { musicReader.amplitudeFlow }
    val fftFlow = remember { musicReader.fftFlow }
    val isReady by musicReader.isReady.collectAsStateWithLifecycle(false)

    LaunchedEffect(isReady) {
        if (isReady) {
            musicReader.play()
        }
    }

    val waveformFlow = remember {
        // Use amplitudeFlow to fake a waveform if not available
        musicReader.amplitudeFlow.map { amp ->
            FloatArray(64) { i -> sin(i / 64f * 2 * PI).toFloat() * amp }
        }.stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = FloatArray(64)
        )
    }

    Column(modifier = modifier) {
        MusicVisualizer(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            amplitudeFlow = amplitudeFlow,
            fftFlow = fftFlow,
            waveformFlow = waveformFlow
        )
    }
}