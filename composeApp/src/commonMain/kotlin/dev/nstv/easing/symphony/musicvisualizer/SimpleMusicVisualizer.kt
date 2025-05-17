package dev.nstv.easing.symphony.musicvisualizer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nstv.easing.symphony.musicvisualizer.reader.MusicReader
import dev.nstv.easing.symphony.musicvisualizer.reader.MusicReader.Companion.fftBins
import dev.nstv.easing.symphony.musicvisualizer.reader.provideMusicReader
import dev.nstv.easing.symphony.util.DisposableEffectWithLifecycle
import kotlinx.coroutines.launch

@Composable
fun MusicPlayer(
    fileUri: String,
    playOnLoad: Boolean = true,
    normalized: Boolean = false,
    content: @Composable (
        fftData: FloatArray,
        togglePlayback: () -> Unit
    ) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val musicReader = provideMusicReader(normalized)
    coroutineScope.launch { musicReader.loadFile(fileUri) }

    SimpleMusicVisualizerContent(musicReader, playOnLoad, content)
}

@Composable
private fun SimpleMusicVisualizerContent(
    musicReader: MusicReader,
    playOnLoad: Boolean = true,
    content: @Composable (
        fftData: FloatArray,
        togglePlayback: () -> Unit,
    ) -> Unit
) {
    val fftData by musicReader.fftFlow.collectAsStateWithLifecycle(FloatArray(fftBins))
    val fileLoaded by musicReader.isReady.collectAsStateWithLifecycle()
    val isPlaying by musicReader.isPlaying.collectAsStateWithLifecycle()

    LaunchedEffect(fileLoaded) {
        if (fileLoaded) {
            println("FILE LOADED")
            musicReader.play()
            if (!playOnLoad){
                musicReader.pause()
            }
        }
    }

    DisposableEffectWithLifecycle(
        musicReader,
        onPause = { musicReader.pause() },
        onResume = { musicReader.play() }
    )

    content(
        fftData
    ) {
        if (isPlaying) musicReader.pause() else musicReader.play()
    }
}
