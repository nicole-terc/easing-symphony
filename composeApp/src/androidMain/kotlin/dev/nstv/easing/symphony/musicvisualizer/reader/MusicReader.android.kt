package dev.nstv.easing.symphony.musicvisualizer.reader

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaPlayer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.sqrt


@Composable
actual fun provideMusicReader(normalized: Boolean, playOnLoad: Boolean): MusicReader {
    val context = LocalContext.current
    return remember { AndroidMusicReader(context, normalized, playOnLoad) }
}


class AndroidMusicReader(
    private val context: Context,
    normalized: Boolean,
    playOnLoad: Boolean,
) : MusicReader(normalized, playOnLoad) {

    private var player: MediaPlayer? = null
    private var frameBuffer = listOf<FloatArray>()
    private var job: Job? = null

    override suspend fun loadFile(fileUri: String) = withContext(Dispatchers.IO) {
        // Thank you for the removePrefix, LexiLabs! (https://github.com/LexiLabs-App/basic-sound/blob/main/basic-sound/src/androidMain/kotlin/app/lexilabs/basic/sound/AudioByte.kt)
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
                    if (tempBuffer.size >= FRAME_SIZE) {
                        (frameBuffer as MutableList).add(tempBuffer.take(FRAME_SIZE).toFloatArray())
                        tempBuffer.subList(0, FRAME_SIZE).clear()
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
        super.loadFile(fileUri)
    }

    override fun play() {
        player?.start()
        job = CoroutineScope(Dispatchers.Default).launch {
            player?.let { player ->
                while (player.isPlaying) {
                    updateFrame(player.currentPosition)
                    delay(FRAME_DELAY_MILLIS)
                }
                clearFlows()
            }
        }
        super.play()
    }

    private fun updateFrame(time: Int) {
        val frameAtTime = ((time / 1000.0) * SAMPLE_RATE / FRAME_SIZE).toInt()
        val rawFrame = frameBuffer.getOrNull(frameAtTime)

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
        player?.pause()
        job?.cancel()
        super.pause()
    }

    override fun seekTo(position: Long) {
        player?.seekTo(position.toInt())
    }

    override fun stop() {
        player?.stop()
        player?.seekTo(0)
        job?.cancel()
        super.stop()
    }
}

