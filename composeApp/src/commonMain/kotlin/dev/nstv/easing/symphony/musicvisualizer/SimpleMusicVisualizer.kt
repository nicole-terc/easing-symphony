package dev.nstv.easing.symphony.musicvisualizer

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import dev.nstv.easing.symphony.musicvisualizer.MusicReader.Companion.fftBins
import kotlinx.coroutines.flow.collectLatest
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

@Composable
fun SimpleMusicVisualizer(
    musicReader: MusicReader,
    modifier: Modifier = Modifier
) {
    var fftData by remember { mutableStateOf(FloatArray(fftBins)) }

    LaunchedEffect(Unit) {
        launch {
            musicReader.fftFlow.collectLatest { data ->
                fftData = data
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
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
