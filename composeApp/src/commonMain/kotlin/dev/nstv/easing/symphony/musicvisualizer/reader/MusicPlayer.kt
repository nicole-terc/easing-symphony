package dev.nstv.easing.symphony.musicvisualizer.reader

import androidx.compose.runtime.Composable
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
    val musicReader = provideMusicReader(normalized, playOnLoad)
    coroutineScope.launch { musicReader.loadFile(fileUri) }

    MusicPlayerContent(musicReader, frameContent)
}

@Composable
private fun MusicPlayerContent(
    musicReader: MusicReader,
    frameContent: @Composable (
        fftData: FloatArray,
        amplitudeData: Float,
        togglePlayback: () -> Unit,
    ) -> Unit
) {
    val fftData by musicReader.frequencies.collectAsStateWithLifecycle(FloatArray(FFT_BINS))
    val amplitudeData by musicReader.amplitude.collectAsStateWithLifecycle(0f)
    val isPlaying by musicReader.isPlaying.collectAsStateWithLifecycle()

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
