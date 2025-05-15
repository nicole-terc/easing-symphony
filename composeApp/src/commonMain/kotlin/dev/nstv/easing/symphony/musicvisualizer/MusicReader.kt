package dev.nstv.easing.symphony.musicvisualizer

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow


interface MusicReader {
    val amplitudeFlow: Flow<Float>
    val fftFlow: Flow<FloatArray>

    suspend fun loadFile(fileUri: String)
    fun play()
    fun pause()
    fun stop()

    companion object {
        const val frameSize: Int = 1024
        const val frameDelayMillis: Long = 16L
        const val sampleRate: Int = 44100
        const val fftBins: Int = 64
    }
}

@Composable
expect fun provideMusicReader(): MusicReader