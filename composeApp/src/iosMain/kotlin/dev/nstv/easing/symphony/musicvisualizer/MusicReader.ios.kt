package dev.nstv.easing.symphony.musicvisualizer

import androidx.compose.runtime.Composable
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import platform.AVFAudio.AVAudioFile
import platform.Foundation.NSURL

@Composable
actual fun provideMusicReader(): MusicReader {
    TODO("Not yet implemented")
}

class iOSMusicReader(): MusicReader {
    private val engine = AVAudioEngine()
    private val player = AVAudioPlayerNode()
    private val bufferSize = 1024

    private val _amplitudeFlow = MutableStateFlow(0f)
    override val amplitudeFlow: Flow<Float> = _amplitudeFlow

    private val _fftFlow = MutableStateFlow(FloatArray(bufferSize / 2))
    override val fftFlow: Flow<FloatArray> = _fftFlow

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun load(filePath: String) {
        val url = NSURL.fileURLWithPath(filePath)
        val audioFile = AVAudioFile(url, "r", null)
        engine.attachNode(player)
        engine.connect(player, toNode = engine.mainMixerNode, format = audioFile.processingFormat)
        engine.prepare()

        val tapFormat = engine.mainMixerNode.outputFormat(forBus = 0u)
        engine.mainMixerNode.installTapOnBus(0u, bufferSize.toULong(), tapFormat) { buffer, _ ->
            val channelData = buffer.floatChannelData?.get(0) ?: return@installTapOnBus
            val samples = FloatArray(buffer.frameLength.toInt()) { channelData[it] }

            val amplitude = samples.maxOfOrNull { abs(it) } ?: 0f
            _amplitudeFlow.value = amplitude

            val log2n = vDSP_Length(log2(bufferSize.toDouble()).roundToInt())
            val fftSetup = vDSP_create_fftsetup(log2n, Int32(kFFTRadix2))

            val realp = FloatArray(bufferSize / 2)
            val imagp = FloatArray(bufferSize / 2)
            val splitComplex = DSPSplitComplex(realp.refTo(0), imagp.refTo(0))

            samples.withUnsafeBufferPointer { pointer ->
                pointer.baseAddress?.let {
                    it.withMemoryRebound(to: DSPComplex.self, capacity: bufferSize) { complexPointer ->
                    vDSP_ctoz(complexPointer, 2, &splitComplex, 1, vDSP_Length(bufferSize / 2))
                }
                }
            }

            vDSP_fft_zrip(fftSetup, &splitComplex, 1, log2n, FFTDirection(FFT_FORWARD))

            val magnitudes = FloatArray(bufferSize / 2)
            vDSP_zvmags(&splitComplex, 1, &magnitudes, 1, vDSP_Length(bufferSize / 2))

            vDSP_destroy_fftsetup(fftSetup)
            _fftFlow.value = magnitudes
        }

        player.scheduleFile(audioFile, atTime = null, completionHandler = null)
    }

    override fun start() {
        try {
            engine.startAndReturnError(null)
            player.play()
        } catch (e: Exception) {
            println("iOS Visualizer error: ${e.message}")
        }
    }

    override fun pause() {
        player.pause()
    }

    override fun stop() {
        player.stop()
        engine.stop()
    }
}