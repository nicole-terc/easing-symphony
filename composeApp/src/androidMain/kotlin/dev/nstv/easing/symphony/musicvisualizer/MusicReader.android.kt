package dev.nstv.easing.symphony.musicvisualizer

import android.content.Context
import android.media.MediaPlayer
import android.media.audiofx.Visualizer
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.math.sqrt

@Composable
actual fun provideMusicReader(): MusicReader = AndroidMusicReader(LocalContext.current)

class AndroidMusicReader(
    private val context: Context
) : MusicReader {
    private var mediaPlayer: MediaPlayer? = null
    private var visualizer: Visualizer? = null

    private val _amplitudeFlow = MutableStateFlow(0f)
    override val amplitudeFlow: Flow<Float> = _amplitudeFlow

    private val _fftFlow = MutableStateFlow(FloatArray(0))
    override val fftFlow: Flow<FloatArray> = _fftFlow

    override suspend fun loadFile(filePath: String) {
        mediaPlayer = MediaPlayer().apply {
            setDataSource(filePath)
            prepare()
        }

        visualizer = Visualizer(mediaPlayer!!.audioSessionId).apply {
            captureSize = Visualizer.getCaptureSizeRange()[1]
            setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                override fun onWaveFormDataCapture(
                    v: Visualizer?,
                    waveform: ByteArray?,
                    samplingRate: Int
                ) {
                    waveform?.let {
                        val max = it.maxOrNull()?.toInt()?.coerceAtLeast(0) ?: 0
                        _amplitudeFlow.value = max / 128f
                    }
                }

                override fun onFftDataCapture(
                    v: Visualizer?,
                    fft: ByteArray?,
                    samplingRate: Int
                ) {
                    fft?.let {
                        val magnitudes = FloatArray(it.size / 2) { i ->
                            val re = it[i * 2].toFloat()
                            val im = it[i * 2 + 1].toFloat()
                            sqrt(re * re + im * im)
                        }
                        _fftFlow.value = magnitudes
                    }
                }
            }, Visualizer.getMaxCaptureRate() / 2, true, true)
        }
    }

    override fun play() {
        mediaPlayer?.start()
        visualizer?.enabled = true
    }

    override fun pause() {
        mediaPlayer?.pause()
    }

    override fun stop() {
        visualizer?.enabled = false
        visualizer?.release()
        mediaPlayer?.stop()
        mediaPlayer?.release()
    }
}