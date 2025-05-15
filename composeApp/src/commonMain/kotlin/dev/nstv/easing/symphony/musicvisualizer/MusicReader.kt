package dev.nstv.easing.symphony.musicvisualizer

import androidx.annotation.CallSuper
import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


abstract class MusicReader {
    abstract val amplitudeFlow: Flow<Float>
    abstract val fftFlow: Flow<FloatArray>
    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean>
        get() = _isReady.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean>
        get() = _isPlaying.asStateFlow()

    open suspend fun loadFile(fileUri: String){
        fileLoaded()
    }

    protected fun fileLoaded() {
        _isReady.value = true
    }

    @CallSuper
    open fun play() {
        _isPlaying.value = true
    }

    @CallSuper
    open fun pause() {
        _isPlaying.value = false
    }

    @CallSuper
    open fun stop() {
        _isPlaying.value = false
    }

    companion object {
        const val frameSize: Int = 1024
        const val frameDelayMillis: Long = 16L
        const val sampleRate: Int = 44100
        const val fftBins: Int = 64
    }
}

@Composable
expect fun provideMusicReader(): MusicReader