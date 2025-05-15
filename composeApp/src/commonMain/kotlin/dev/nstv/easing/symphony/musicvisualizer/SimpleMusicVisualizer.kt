package dev.nstv.easing.symphony.musicvisualizer

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nstv.easing.symphony.extensions.nextItemLoop
import dev.nstv.easing.symphony.musicvisualizer.MusicReader.Companion.fftBins
import dev.nstv.easing.symphony.util.DisposableEffectWithLifecycle
import kotlinx.coroutines.launch

@Composable
fun SimpleMusicVisualizer(
    fileUri: String,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val musicReader = provideMusicReader()
    coroutineScope.launch { musicReader.loadFile(fileUri) }

    SimpleMusicVisualizer(musicReader, modifier)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SimpleMusicVisualizer(
    musicReader: MusicReader,
    modifier: Modifier = Modifier
) {
    val fftData by musicReader.fftFlow.collectAsStateWithLifecycle(FloatArray(fftBins))
    val fileLoaded by musicReader.isReady.collectAsStateWithLifecycle()
    val isPlaying by musicReader.isPlaying.collectAsStateWithLifecycle()
    var visualizerType by remember { mutableStateOf(VisualizerType.Simple) }

    LaunchedEffect(fileLoaded) {
        if (fileLoaded) {
            musicReader.play()
        }
    }

    DisposableEffectWithLifecycle(
        musicReader,
        onPause = { musicReader.pause() },
        onResume = { musicReader.play() }
    )

    Box(
        modifier = modifier.fillMaxSize().combinedClickable(
            onDoubleClick = {
                visualizerType = VisualizerType.entries.nextItemLoop(visualizerType)
            },
        ) {
            if (isPlaying) musicReader.pause() else musicReader.play()
        }
    ) {
        EffectVisualizer(
            fft = fftData,
            modifier = Modifier.fillMaxSize(),
            visualizerType = visualizerType,
        )
    }
}
