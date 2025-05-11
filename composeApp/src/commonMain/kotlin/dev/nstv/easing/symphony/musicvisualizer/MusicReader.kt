package dev.nstv.easing.symphony.musicvisualizer

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow


interface MusicReader {
    val amplitudeFlow: Flow<Float>
    val fftFlow: Flow<FloatArray>

    suspend fun loadFile(filePath: String)
    fun play()
    fun pause()
    fun stop()
}

@Composable
expect fun provideMusicReader(): MusicReader