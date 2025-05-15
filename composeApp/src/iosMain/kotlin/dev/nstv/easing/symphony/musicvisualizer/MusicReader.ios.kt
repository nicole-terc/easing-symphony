package dev.nstv.easing.symphony.musicvisualizer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.nstv.easing.symphony.audio.fft
import dev.nstv.easing.symphony.musicvisualizer.MusicReader.Companion.fftBins
import dev.nstv.easing.symphony.musicvisualizer.MusicReader.Companion.frameDelayMillis
import dev.nstv.easing.symphony.musicvisualizer.MusicReader.Companion.frameSize
import dev.nstv.easing.symphony.musicvisualizer.MusicReader.Companion.sampleRate
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
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
actual fun provideMusicReader(): MusicReader = remember { IOSMusicReader() }

class IOSMusicReader : MusicReader {
    private val _amplitudeFlow = MutableStateFlow(0f)
    private val _fftFlow = MutableStateFlow(FloatArray(fftBins))
    override val amplitudeFlow: Flow<Float> = _amplitudeFlow
    override val fftFlow: Flow<FloatArray> = _fftFlow

    private lateinit var player: AVAudioPlayer
    private var frameBuffer = listOf<FloatArray>()
    private var job: Job? = null

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun loadFile(filePath: String) {
        val url = NSURL(string = filePath)
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
                if (tempBuffer.size >= frameSize) {
                    frames.add(tempBuffer.take(frameSize).toFloatArray())
                    tempBuffer.subList(0, frameSize).clear()
                }
            }
            frameBuffer = frames
        }

        player = AVAudioPlayer(contentsOfURL = url, null).apply {
            prepareToPlay()
        }
        play()
    }

    override fun play() {
        player.play()
        job = CoroutineScope(Dispatchers.Default).launch {
            while (player.playing) {
                val currentSample = (player.currentTime * sampleRate).toInt()
                val currentFrame = currentSample / frameSize
                val frame = frameBuffer.getOrNull(currentFrame)
                if (frame != null) {
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
        player.pause()
        job?.cancel()
    }

    override fun stop() {
        player.stop()
        player.currentTime = 0.0
        job?.cancel()
    }
}
