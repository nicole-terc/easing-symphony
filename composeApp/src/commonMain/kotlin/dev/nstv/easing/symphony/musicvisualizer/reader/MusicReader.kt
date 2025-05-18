package dev.nstv.easing.symphony.musicvisualizer.reader

import androidx.annotation.CallSuper
import androidx.compose.runtime.Composable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


abstract class MusicReader(
    protected val normalized: Boolean,
    private val playOnLoad: Boolean = true,
) {
    protected val _waveformFlow = MutableStateFlow(FloatArray(MusicReader.FRAME_SIZE))
    val waveformFlow: StateFlow<FloatArray> = _waveformFlow.asStateFlow()
    protected val _amplitudeFlow = MutableStateFlow(0f)
    val amplitudeFlow: StateFlow<Float> = _amplitudeFlow.asStateFlow()
    protected val _fftFlow = MutableStateFlow(FloatArray(FFT_BINS))
    val fftFlow: StateFlow<FloatArray> = _fftFlow.asStateFlow()
    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean>
        get() = _isPlaying.asStateFlow()

    open suspend fun loadFile(fileUri: String) {
        fileLoaded()
    }

    protected suspend fun fileLoaded() {
        // TODO: fix lost music context when play is not called after load
//        play()
//        pause()
        if(playOnLoad){
            delay(PLAY_DELAY)
            play()
        }
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

    abstract fun seekTo(position: Long)


    @CallSuper
    open fun stop() {
        _isPlaying.value = false
        clearFlows()
    }


    protected fun clearFlows() {
        _waveformFlow.value = FloatArray(FRAME_SIZE)
        _amplitudeFlow.value = 0f
        _fftFlow.value = FloatArray(FFT_BINS)
    }

    companion object {
        const val FRAME_SIZE: Int = 1024

        // 16L ~ 60fps | 32L ~ 30fps | 64L ~ 15fps
        const val FRAME_DELAY_MILLIS: Long = 64L
        const val SAMPLE_RATE: Int = 44100
        const val FFT_BINS: Int = 64
        const val PLAY_DELAY: Long = 10L
    }
}

@Composable
expect fun provideMusicReader(normalized: Boolean, playOnLoad: Boolean): MusicReader