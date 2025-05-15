package dev.nstv.easing.symphony.musicvisualizer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nstv.easing.symphony.musicvisualizer.MusicReader.Companion.fftBins
import dev.nstv.easing.symphony.util.DisposableEffectWithLifecycle
import kotlinx.coroutines.launch

@Composable
fun SimpleMusicVisualizer(
    fileUri: String,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val musicReader = provideMusicReader()
    coroutineScope.launch { musicReader.loadFile(fileUri) }

    SimpleMusicVisualizer(musicReader, modifier)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SimpleMusicVisualizer(
    musicReader: MusicReader,
    modifier: Modifier = Modifier
) {
    val fftData by musicReader.fftFlow.collectAsStateWithLifecycle(FloatArray(fftBins))
    val fileLoaded by musicReader.isReady.collectAsStateWithLifecycle()
    val isPlaying by musicReader.isPlaying.collectAsStateWithLifecycle()

    LaunchedEffect(fileLoaded) {
        if (fileLoaded) {
            musicReader.play()
        }
    }

    DisposableEffectWithLifecycle(
        musicReader,
        onPause = { musicReader.pause() },
        onResume = { musicReader.play() }
    )

    Canvas(
        modifier = modifier.fillMaxSize().combinedClickable(
            onDoubleClick = { if (isPlaying) musicReader.pause() else musicReader.play() },
        ) {}
    ) {
        val barWidth = size.width / fftData.size
        fftData.forEachIndexed { i, value ->
            val height = (value * size.height * 5).coerceIn(0f, size.height)
            drawRect(
                color = Color.Cyan,
                topLeft = Offset(i * barWidth, size.height - height),
                size = Size(barWidth * 0.8f, height)
            )
        }
    }
}
