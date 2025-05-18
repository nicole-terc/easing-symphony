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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import platform.AVFAudio.AVAudioFile
import platform.AVFAudio.AVAudioPCMBuffer
import platform.AVFAudio.AVAudioPlayer
import platform.Foundation.NSError
import platform.Foundation.NSURL
import kotlin.math.sqrt


@Composable
actual fun provideMusicReader(normalized: Boolean): MusicReader = remember { IOSMusicReader(normalized) }

class IOSMusicReader(normalized: Boolean) : MusicReader(normalized) {
    private val _amplitudeFlow = MutableStateFlow(0f)
    private val _fftFlow = MutableStateFlow(FloatArray(FFT_BINS))
    override val amplitudeFlow: Flow<Float> = _amplitudeFlow
    override val fftFlow: Flow<FloatArray> = _fftFlow

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
                val currentSample = (player.currentTime * SAMPLE_RATE).toInt()
                val currentFrame = currentSample / FRAME_SIZE
                val frame = frameBuffer.getOrNull(currentFrame)
                if (frame != null) {
                    val amplitude = sqrt(frame.map { it * it }.sum() / frame.size)
                    val fft = frame.getFft()
                    _amplitudeFlow.value = amplitude
                    _fftFlow.value = fft.take(FFT_BINS).toFloatArray()
                }
                delay(FRAME_DELAY_MILLIS)
            }
        }
        super.play()
    }

    override fun pause() {
        player.pause()
        job?.cancel()
        super.pause()
    }

    override fun stop() {
        player.stop()
        player.currentTime = 0.0
        job?.cancel()
        super.stop()
    }
}
