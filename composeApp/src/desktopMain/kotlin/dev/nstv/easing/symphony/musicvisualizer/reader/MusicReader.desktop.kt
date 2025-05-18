package dev.nstv.easing.symphony.musicvisualizer.reader

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import javax.sound.sampled.AudioSystem
import kotlin.math.abs
import kotlin.math.sqrt


@Composable
actual fun provideMusicReader(normalized: Boolean, playOnLoad: Boolean): MusicReader =
    remember { DesktopMusicReader(normalized, playOnLoad) }

class DesktopMusicReader(
    normalized: Boolean,
    playOnLoad: Boolean,
) : MusicReader(normalized, playOnLoad) {
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
            if (tempBuffer.size >= FRAME_SIZE) {
                frames.add(tempBuffer.take(FRAME_SIZE).toFloatArray())
                tempBuffer.subList(0, FRAME_SIZE).clear()
            }
        }
        frameBuffer = frames

        clip = AudioSystem.getClip().apply {
            open(AudioSystem.getAudioInputStream(tempFile))
        }
        super.loadFile(fileUri)
    }

    override fun play() {
        if (clip?.isRunning == true) return
        clip?.start()
        job = CoroutineScope(Dispatchers.Default).launch {
            while (clip?.isRunning == true) {
                val currentTime = clip!!.microsecondPosition
                updateFrame(currentTime.toInt())
                delay(FRAME_DELAY_MILLIS)
            }
            clearFlows()
        }
        super.play()
    }

    private fun updateFrame(time: Int) {
        val sampleAtTime = (time / 1_000_000.0 * SAMPLE_RATE).toInt()
        val frameAtTime = sampleAtTime / FRAME_SIZE
        val rawFrame = frameBuffer.getOrNull(frameAtTime)

        if (rawFrame != null) {
            val frame = if (normalized) {
                val max = rawFrame.maxOfOrNull { v -> abs(v) }?.takeIf { it > 0f } ?: 1f
                rawFrame.map { sample -> sample / max }.toFloatArray()
            } else rawFrame


            val amplitude = sqrt(frame.map { it * it }.sum() / frame.size)
            val fft = frame.getFft()
            _waveformFlow.value = frame
            _amplitudeFlow.value = amplitude
            _fftFlow.value = fft.take(FFT_BINS).toFloatArray()
        } else {
            clearFlows()
        }
    }

    override fun pause() {
        if (clip?.isRunning == false) return
        clip?.stop()
        job?.cancel()
        super.pause()
    }

    override fun seekTo(position: Long) {
        clip?.microsecondPosition = position
    }

    override fun stop() {
        if (clip?.isRunning == false) return
        clip?.stop()
        clip?.microsecondPosition = 0
        job?.cancel()
        super.stop()
    }
}

