package dev.nstv.easing.symphony.musicvisualizer.reader

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import platform.AVFAudio.AVAudioFile
import platform.AVFAudio.AVAudioPCMBuffer
import platform.AVFAudio.AVAudioPlayer
import platform.Foundation.NSError
import platform.Foundation.NSTimeInterval
import platform.Foundation.NSURL
import kotlin.math.abs
import kotlin.math.sqrt


@Composable
actual fun provideMusicReader(normalized: Boolean, playOnLoad: Boolean): MusicReader =
    remember { IOSMusicReader(normalized, playOnLoad) }

class IOSMusicReader(normalized: Boolean, playOnLoad: Boolean) :
    MusicReader(normalized, playOnLoad) {

    private lateinit var player: AVAudioPlayer
    private var frameBuffer = listOf<FloatArray>()
    private var job: Job? = null

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun loadFile(fileUri: String) {
        val url = NSURL(string = fileUri)
        memScoped {
            val audioFile = AVAudioFile(url, null)

            val format = audioFile.processingFormat
            val frameCount = audioFile.length.toUInt()
            val buffer = AVAudioPCMBuffer(format, frameCount)!!
            val readError = alloc<ObjCObjectVar<NSError?>>()
            if (!audioFile.readIntoBuffer(buffer, error = readError.ptr)) {
                throw IllegalStateException("Failed to read audio buffer: ${'$'}{readError.value?.localizedDescription}")
            }

            val channelData = buffer.floatChannelData!![0]!!
            val length = buffer.frameLength.toInt()
            val tempBuffer = mutableListOf<Float>()
            val frames = mutableListOf<FloatArray>()
            for (i in 0 until length) {
                tempBuffer.add(channelData[i])
                if (tempBuffer.size >= FRAME_SIZE) {
                    frames.add(tempBuffer.take(FRAME_SIZE).toFloatArray())
                    tempBuffer.subList(0, FRAME_SIZE).clear()
                }
            }
            frameBuffer = frames
        }

        player = AVAudioPlayer(contentsOfURL = url, null).apply {
            prepareToPlay()
        }
        super.loadFile(fileUri)
    }

    override fun play() {
        player.play()
        job = CoroutineScope(Dispatchers.Default).launch {
            while (player.playing) {
                updateFrame(player.currentTime)
                delay(FRAME_DELAY_MILLIS)
            }
            clearFlows()
        }
        super.play()
    }

    private fun updateFrame(time: NSTimeInterval) {
        val currentSample = (time * SAMPLE_RATE).toInt()
        val currentFrame = currentSample / FRAME_SIZE
        val rawFrame = frameBuffer.getOrNull(currentFrame)

        if (rawFrame != null) {
            val frame = if (normalized) {
                val max = rawFrame.maxOfOrNull { v -> abs(v) }?.takeIf { it > 0f } ?: 1f
                rawFrame.map { sample -> sample / max }.toFloatArray()
            } else rawFrame

            val amplitude = sqrt(frame.map { it * it }.sum() / frame.size)
            val fft = frame.getFft()
            _amplitudeFlow.value = amplitude
            _fftFlow.value = fft.take(FFT_BINS).toFloatArray()
        } else {
            clearFlows()
        }
    }

    override fun pause() {
        player.pause()
        job?.cancel()
        super.pause()
    }

    override fun seekTo(position: Long) {
        player.currentTime = position.toDouble() / 1000
    }

    override fun stop() {
        player.stop()
        player.currentTime = 0.0
        job?.cancel()
        super.stop()
    }
}
