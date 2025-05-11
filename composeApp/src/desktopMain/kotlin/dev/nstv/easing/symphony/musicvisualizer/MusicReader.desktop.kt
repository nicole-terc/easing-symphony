package dev.nstv.easing.symphony.musicvisualizer

import androidx.compose.runtime.Composable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import org.apache.commons.math3.complex.Complex
import org.apache.commons.math3.transform.DftNormalization
import org.apache.commons.math3.transform.FastFourierTransformer
import org.apache.commons.math3.transform.TransformType
import java.io.File
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.SourceDataLine
import kotlin.concurrent.thread
import kotlin.math.abs

@Composable
actual fun provideMusicReader(): MusicReader = DesktopMusicReader()


class DesktopMusicReader : MusicReader {
    private val _amplitudeFlow = MutableStateFlow(0f)
    override val amplitudeFlow: Flow<Float> = _amplitudeFlow

    private val _fftFlow = MutableStateFlow(FloatArray(0))
    override val fftFlow: Flow<FloatArray> = _fftFlow

    private var audioLine: SourceDataLine? = null
    private var playingThread: Thread? = null

    override suspend fun loadFile(filePath: String) {
        val file = File(filePath)
        val audioInputStream = withContext(Dispatchers.IO) {
            AudioSystem.getAudioInputStream(file)
        }
        val format = audioInputStream.format
        val decodedFormat = AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            format.sampleRate,
            16,
            format.channels,
            format.channels * 2,
            format.sampleRate,
            false
        )

        val din = AudioSystem.getAudioInputStream(decodedFormat, audioInputStream)
        val info = DataLine.Info(SourceDataLine::class.java, decodedFormat)
        val line = AudioSystem.getLine(info) as SourceDataLine
        line.open(decodedFormat)
        audioLine = line

        playingThread = thread {
            val buffer = ByteArray(4096)
            val fft = FastFourierTransformer(DftNormalization.STANDARD)
            line.start()
            while (true) {
                val bytesRead = din.read(buffer, 0, buffer.size)
                if (bytesRead == -1) break
                line.write(buffer, 0, bytesRead)

                val samples = ShortArray(bytesRead / 2) { i ->
                    ((buffer[i * 2 + 1].toInt() shl 8) or (buffer[i * 2].toInt() and 0xff)).toShort()
                }

                val amplitude = samples.maxOfOrNull { abs(it.toInt()) }?.div(32768f) ?: 0f
                _amplitudeFlow.value = amplitude

                val fftInput = samples.map { it.toDouble() }.toDoubleArray()
                val result: Array<Complex> = fft.transform(fftInput, TransformType.FORWARD)
                val magnitudes =
                    result.take(fftInput.size / 2).map { it.abs().toFloat() }.toFloatArray()
                _fftFlow.value = magnitudes
            }
            line.drain()
            line.stop()
            line.close()
        }
    }

    override fun play() {}
    override fun pause() {
        // No built-in pause; stop is used
        stop()
    }

    override fun stop() {
        playingThread?.interrupt()
        audioLine?.stop()
        audioLine?.close()
    }
}