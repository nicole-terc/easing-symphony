package dev.nstv.easing.symphony.musicvisualizer.reader

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nstv.easing.symphony.musicvisualizer.reader.MusicReader.Companion.FFT_BINS
import dev.nstv.easing.symphony.util.DisposableEffectWithLifecycle
import kotlinx.coroutines.launch

@Composable
fun MusicPlayer(
    fileUri: String,
    playOnLoad: Boolean = true,
    normalized: Boolean = true,
    frameContent: @Composable (
        fftData: FloatArray,
        amplitudeData: Float,
        togglePlayback: () -> Unit
    ) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val musicReader = provideMusicReader(normalized)
    coroutineScope.launch { musicReader.loadFile(fileUri) }

    MusicPlayerContent(musicReader, playOnLoad, frameContent)
}

@Composable
private fun MusicPlayerContent(
    musicReader: MusicReader,
    playOnLoad: Boolean = true,
    frameContent: @Composable (
        fftData: FloatArray,
        amplitudeData: Float,
        togglePlayback: () -> Unit,
    ) -> Unit
) {
    val fftData by musicReader.fftFlow.collectAsStateWithLifecycle(FloatArray(FFT_BINS))
    val amplitudeData by musicReader.amplitudeFlow.collectAsStateWithLifecycle(0f)
    val fileLoaded by musicReader.isReady.collectAsStateWithLifecycle()
    val isPlaying by musicReader.isPlaying.collectAsStateWithLifecycle()

    LaunchedEffect(fileLoaded) {
        if (fileLoaded) {
            println("FILE LOADED")
            musicReader.play()
            if (!playOnLoad) {
                musicReader.pause()
            }
        }
    }

    DisposableEffectWithLifecycle(
        musicReader,
        onPause = { musicReader.pause() },
        onResume = { musicReader.play() }
    )

    frameContent(
        fftData,
        amplitudeData,
    ) {
        if (isPlaying) musicReader.pause() else musicReader.play()
    }
}
