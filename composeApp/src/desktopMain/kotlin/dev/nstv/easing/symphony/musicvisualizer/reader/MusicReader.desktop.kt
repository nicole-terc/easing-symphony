package dev.nstv.easing.symphony.musicvisualizer.reader

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import javax.sound.sampled.AudioSystem
import kotlin.math.sqrt


@Composable
actual fun provideMusicReader(normalized: Boolean): MusicReader =
    remember { DesktopMusicReader(normalized) }

class DesktopMusicReader(
    normalized: Boolean,
) : MusicReader(normalized) {
    private val _amplitudeFlow = MutableStateFlow(0f)
    private val _fftFlow = MutableStateFlow(FloatArray(fftBins))
    override val amplitudeFlow: Flow<Float> = _amplitudeFlow
    override val fftFlow: Flow<FloatArray> = _fftFlow

    private var frameBuffer = listOf<FloatArray>()
    private var job: Job? = null
    private var clip: javax.sound.sampled.Clip? = null

    override suspend fun loadFile(fileUri: String) = withContext(Dispatchers.IO) {
        val resourceStream =
            this::class.java.classLoader?.getResourceAsStream(fileUri.substringAfter("!/"))
                ?: throw FileNotFoundException("Resource not found: $fileUri")

        val tempFile = File.createTempFile("music", ".wav")
        resourceStream.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        val audioInputStream = AudioSystem.getAudioInputStream(tempFile)
        val format = audioInputStream.format
        val bytesPerFrame = format.frameSize
        val buffer = ByteArray(4096 * bytesPerFrame)
        val samples = mutableListOf<Float>()

        var bytesRead: Int
        while (audioInputStream.read(buffer).also { bytesRead = it } != -1) {
            for (i in 0 until bytesRead step 2) {
                val sample =
                    ((buffer[i + 1].toInt() shl 8) or (buffer[i].toInt() and 0xFF)).toShort()
                samples.add(sample / 32768f)
            }
        }

        val tempBuffer = mutableListOf<Float>()
        val frames = mutableListOf<FloatArray>()
        for (sample in samples) {
            tempBuffer.add(sample)
            if (tempBuffer.size >= frameSize) {
                frames.add(tempBuffer.take(frameSize).toFloatArray())
                tempBuffer.subList(0, frameSize).clear()
            }
        }
        frameBuffer = frames

        clip = AudioSystem.getClip().apply {
            open(AudioSystem.getAudioInputStream(tempFile))
        }
        super.loadFile(fileUri)
    }

    override fun play() {
        clip?.start()
        job = CoroutineScope(Dispatchers.Default).launch {
            while (clip?.isRunning == true) {
                val currentSample = (clip!!.microsecondPosition / 1_000_000.0 * sampleRate).toInt()
                val currentFrame = currentSample / frameSize
                val frame = frameBuffer.getOrNull(currentFrame)
                if (frame != null) {
                    val amplitude = sqrt(frame.map { it * it }.sum() / frame.size)
                    val fft = frame.getFft()
                    _amplitudeFlow.value = amplitude
                    _fftFlow.value = fft.take(fftBins).toFloatArray()
                }
                delay(frameDelayMillis)
            }
        }
        super.play()
    }

    override fun pause() {
        clip?.stop()
        job?.cancel()
        super.pause()
    }

    override fun stop() {
        clip?.stop()
        clip?.microsecondPosition = 0
        job?.cancel()
        super.stop()
    }
}

