package dev.nstv.easing.symphony.musicvisualizer.reader

import androidx.annotation.CallSuper
import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


abstract class MusicReader(
    private val normalized: Boolean,
) {
    abstract val amplitudeFlow: Flow<Float>
    abstract val fftFlow: Flow<FloatArray>
    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean>
        get() = _isReady.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean>
        get() = _isPlaying.asStateFlow()

    open suspend fun loadFile(fileUri: String) {
        fileLoaded()
    }

    protected fun fileLoaded() {
        _isReady.value = true
    }

    protected fun FloatArray.getFft(): FloatArray =
        if (normalized) {
            this.fftNormalizedLog10InPlace()
        } else {
            this.fft()
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
        const val FRAME_SIZE: Int = 1024
        const val FRAME_DELAY_MILLIS: Long = 16L
        const val SAMPLE_RATE: Int = 44100
        const val FFT_BINS: Int = 64
    }
}

@Composable
expect fun provideMusicReader(normalized: Boolean): MusicReader