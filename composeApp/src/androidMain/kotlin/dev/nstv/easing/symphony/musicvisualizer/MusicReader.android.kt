package dev.nstv.easing.symphony.musicvisualizer

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaPlayer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
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
import kotlin.math.sqrt


@Composable
actual fun provideMusicReader(): MusicReader {
    val context = LocalContext.current
    return remember { AndroidMusicReader(context) }
}


class AndroidMusicReader(
    private val context: Context
) : MusicReader {

    private val _amplitudeFlow = MutableStateFlow(0f)
    private val _fftFlow = MutableStateFlow(FloatArray(fftBins))
    override val amplitudeFlow: Flow<Float> = _amplitudeFlow
    override val fftFlow: Flow<FloatArray> = _fftFlow

    private var player: MediaPlayer? = null
    private var frameBuffer = listOf<FloatArray>()
    private var job: Job? = null

    override suspend fun loadFile(fileUri: String) {
        val assetPath = fileUri.removePrefix("file:///android_asset/")
        val afd = context.assets.openFd(assetPath)

        val extractor = MediaExtractor()
        extractor.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)

        var trackIndex = -1
        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (mime?.startsWith("audio/") == true) {
                trackIndex = i
                break
            }
        }
        require(trackIndex != -1) { "No audio track found" }

        extractor.selectTrack(trackIndex)
        val format = extractor.getTrackFormat(trackIndex)
        val mime = format.getString(MediaFormat.KEY_MIME)!!
        val codec = MediaCodec.createDecoderByType(mime)
        codec.configure(format, null, null, 0)
        codec.start()

        val bufferInfo = MediaCodec.BufferInfo()
        val tempBuffer = mutableListOf<Float>()
        frameBuffer = mutableListOf()

        while (true) {
            val inIndex = codec.dequeueInputBuffer(10000)
            if (inIndex >= 0) {
                val inputBuffer = codec.getInputBuffer(inIndex)!!
                val sampleSize = extractor.readSampleData(inputBuffer, 0)
                if (sampleSize < 0) {
                    codec.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                    break
                } else {
                    codec.queueInputBuffer(inIndex, 0, sampleSize, extractor.sampleTime, 0)
                    extractor.advance()
                }
            }

            val outIndex = codec.dequeueOutputBuffer(bufferInfo, 10000)
            if (outIndex >= 0) {
                val outputBuffer = codec.getOutputBuffer(outIndex)!!
                val chunk = ByteArray(bufferInfo.size)
                outputBuffer.get(chunk)
                outputBuffer.clear()
                codec.releaseOutputBuffer(outIndex, false)

                for (i in chunk.indices step 2) {
                    val sample =
                        ((chunk[i + 1].toInt() shl 8) or (chunk[i].toInt() and 0xFF)).toShort()
                    tempBuffer.add(sample / 32768f)
                    if (tempBuffer.size >= frameSize) {
                        (frameBuffer as MutableList).add(tempBuffer.take(frameSize).toFloatArray())
                        tempBuffer.subList(0, frameSize).clear()
                    }
                }
            }
        }

        codec.stop()
        codec.release()
        extractor.release()
        afd.close()

        val afdForPlayer = context.assets.openFd(assetPath)
        player = MediaPlayer().apply {
            setDataSource(
                afdForPlayer.fileDescriptor,
                afdForPlayer.startOffset,
                afdForPlayer.length
            )
            prepare()
            afdForPlayer.close()
        }
        play()
    }

    override fun play() {
        player?.start()
        job = CoroutineScope(Dispatchers.Default).launch {
            while (player?.isPlaying == true) {
                val currentTime = player!!.currentPosition
                val currentFrame = ((currentTime / 1000.0) * sampleRate / frameSize).toInt()
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
        player?.pause()
        job?.cancel()
    }

    override fun stop() {
        player?.stop()
        player?.seekTo(0)
        job?.cancel()
    }
}

// Assume fft(frame: FloatArray): FloatArray is defined elsewhere

