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
    private lateinit var player: AVAudioPlayer
    private lateinit var samples: FloatArray
    private val _amplitudeFlow = MutableStateFlow(0f)
    private val _fftFlow = MutableStateFlow(FloatArray(fftBins))

    override val amplitudeFlow: Flow<Float> = _amplitudeFlow
    override val fftFlow: Flow<FloatArray> = _fftFlow

    private var job: Job? = null

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun loadFile(fileUri: String) {
        val url = NSURL.fileURLWithPath(fileUri)
        memScoped {
            val error = alloc<ObjCObjectVar<NSError?>>()
            val audioFile = AVAudioFile(forReading = url, error = error.ptr)
            if (error.value != null) {
                throw IllegalStateException("Failed to open audio files: ${'$'}{error.value?.localizedDescription}")
            }

            val format = audioFile.processingFormat
            val frameCount = audioFile.length.toUInt()

            val buffer = AVAudioPCMBuffer(format, frameCount)!!
            val readError = alloc<ObjCObjectVar<NSError?>>()
            if (!audioFile.readIntoBuffer(buffer, error = readError.ptr)) {
                throw IllegalStateException("Failed to read audio buffer: ${'$'}{readError.value?.localizedDescription}")
            }

            val channelData = buffer.floatChannelData!![0]!!
            val length = buffer.frameLength.toInt()
            samples = FloatArray(length)
            for (i in 0 until length) {
                samples[i] = channelData[i]
            }
        }

        player = AVAudioPlayer(contentsOfURL = url, null).apply {
            prepareToPlay()
        }
    }

    override fun play() {
        player.play()
        job = CoroutineScope(Dispatchers.Default).launch {
            while (player.playing) {
                val currentTime = player.currentTime
                val currentSample = (currentTime * sampleRate).toInt()
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
        player.pause()
        job?.cancel()
    }

    override fun stop() {
        player.stop()
        player.currentTime = 0.0
        job?.cancel()
    }
}
