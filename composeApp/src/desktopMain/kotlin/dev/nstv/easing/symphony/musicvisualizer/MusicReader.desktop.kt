package dev.nstv.easing.symphony.musicvisualizer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.nstv.easing.symphony.audio.fft
import dev.nstv.easing.symphony.musicvisualizer.MusicReader.Companion.fftBins
import dev.nstv.easing.symphony.musicvisualizer.MusicReader.Companion.frameDelayMillis
import dev.nstv.easing.symphony.musicvisualizer.MusicReader.Companion.frameSize
import dev.nstv.easing.symphony.musicvisualizer.MusicReader.Companion.sampleRate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.sound.sampled.AudioSystem
import kotlin.math.sqrt


@Composable
actual fun provideMusicReader(): MusicReader = remember { DesktopMusicReader() }

class DesktopMusicReader : MusicReader {

    private lateinit var samples: FloatArray
    private val _amplitudeFlow = MutableStateFlow(0f)
    private val _fftFlow = MutableStateFlow(FloatArray(fftBins))
    private var job: Job? = null

    override val amplitudeFlow: Flow<Float> = _amplitudeFlow
    override val fftFlow: Flow<FloatArray> = _fftFlow

    private var audioClip = AudioSystem.getClip()

    override suspend fun loadFile(fileUri: String) {
        val audioInputStream = AudioSystem.getAudioInputStream(File(fileUri))
        val format = audioInputStream.format
        val bytesPerFrame = format.frameSize
        val buffer = ByteArray(4096 * bytesPerFrame)
        val pcmSamples = mutableListOf<Float>()

        var bytesRead: Int
        while (audioInputStream.read(buffer).also { bytesRead = it } != -1) {
            for (i in 0 until bytesRead step 2) {
                val sample =
                    ((buffer[i + 1].toInt() shl 8) or (buffer[i].toInt() and 0xFF)).toShort()
                pcmSamples.add(sample / 32768f)
            }
        }
        samples = pcmSamples.toFloatArray()
        audioClip = AudioSystem.getClip().apply {
            open(AudioSystem.getAudioInputStream(File(fileUri)))
        }
    }

    override fun play() {
        audioClip.start()
        job = CoroutineScope(Dispatchers.Default).launch {
            while (audioClip.isRunning) {
                val currentSample =
                    (audioClip.microsecondPosition / 1_000_000.0 * sampleRate).toInt()
                if (currentSample + frameSize <= samples.size) {
                    val frame = samples.copyOfRange(currentSample, currentSample + frameSize)
                    val amplitude = sqrt(frame.map { it * it }.sum() / frame.size)
                    val fft = frame.fft()
                    _amplitudeFlow.value = amplitude
                    _fftFlow.value = fft.take(fftBins).toFloatArray()
                }
                delay(frameDelayMillis)
            }
        }
    }

    override fun pause() {
        audioClip.stop()
        job?.cancel()
    }

    override fun stop() {
        audioClip.stop()
        audioClip.microsecondPosition = 0
        job?.cancel()
    }
}

// Assume fft(frame: FloatArray): FloatArray is defined elsewhere
