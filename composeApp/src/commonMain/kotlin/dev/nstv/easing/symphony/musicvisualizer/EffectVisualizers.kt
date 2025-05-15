package dev.nstv.easing.symphony.musicvisualizer

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import dev.nstv.easing.symphony.design.TileColor
import dev.nstv.easing.symphony.musicvisualizer.GradientPresets.Neon
import kotlinx.coroutines.delay
import kotlin.math.*

enum class VisualizerType {
    Simple,
    Scrolling,
    CenteredBars,
    Curved,
    Circular;
}

enum class GradientPresets {
    Fire,
    Ocean,
    Neon,
    Sunset,
    Ice;

    fun getColors(): List<Color> = when (this) {
        Fire -> listOf(Color.Red, Color.Yellow, Color.White)
        Ocean -> listOf(Color.Blue, Color.Cyan, Color.White)
        Neon -> listOf(Color.Magenta, Color.Cyan, Color.Green)
        Sunset -> listOf(Color.Red, Color.Magenta, Color.Yellow)
        Ice -> listOf(Color.Cyan, Color.White, Color.Blue)
    }
}

private val defaultColor = TileColor.Pink

@Composable
fun EffectVisualizer(
    fft: FloatArray,
    modifier: Modifier = Modifier,
    visualizerType: VisualizerType = VisualizerType.Simple,
    gradientColors: List<Color> = Neon.getColors()
) {
    when (visualizerType) {
        VisualizerType.Simple -> SimpleVisualizer(fft, modifier, gradientColors)
        VisualizerType.Scrolling -> EfficientScrollingVisualizer(fft, modifier, gradientColors)
        VisualizerType.CenteredBars -> CenteredBarsVisualizer(fft, modifier, gradientColors)
        VisualizerType.Curved -> CurvedVisualizer(fft, modifier, gradientColors)
        VisualizerType.Circular -> CircularVisualizer(fft, modifier, gradientColors)
    }
}

@Composable
fun SimpleVisualizer(
    fft: FloatArray,
    modifier: Modifier = Modifier,
    gradientColors: List<Color>? = null,
    color: Color = defaultColor
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val barWidth = size.width / fft.size
        fft.forEachIndexed { i, value ->
            val height = (value * size.height * 5).coerceIn(0f, size.height)
            drawRect(
                brush = gradientColors?.let { Brush.verticalGradient(it) } ?: SolidColor(color),
                topLeft = Offset(i * barWidth, size.height - height),
                size = Size(barWidth * 0.8f, height)
            )
        }
    }
}


@Composable
fun EfficientScrollingVisualizer(
    fft: FloatArray,
    modifier: Modifier = Modifier,
    gradientColors: List<Color>? = null,
    color: Color = defaultColor
) {
    val barCount = 100
    val bars = remember { FloatArray(barCount) }
    val fftSnapshot = rememberUpdatedState(fft.copyOf())

    LaunchedEffect(Unit) {
        while (true) {
            val newBar = (fftSnapshot.value.lastOrNull() ?: 0f).coerceIn(0f, 1f)
            for (i in 0 until barCount - 1) bars[i] = bars[i + 1]
            bars[barCount - 1] = newBar
            delay(16)
        }
    }

    Canvas(modifier.fillMaxSize()) {
        val barWidth = size.width / barCount
        bars.forEachIndexed { index, value ->
            val barHeight = value * size.height
            drawRect(
                brush = gradientColors?.let { Brush.verticalGradient(it) }
                    ?: SolidColor(color),
                topLeft = Offset(index * barWidth, size.height - barHeight),
                size = Size(barWidth, barHeight)
            )
        }
    }
}

@Composable
fun CenteredBarsVisualizer(
    fft: FloatArray,
    modifier: Modifier = Modifier,
    gradientColors: List<Color>? = null,
    color: Color = defaultColor
) {
    val barCount = 100
    val target = remember { FloatArray(barCount) }
    val current = remember { FloatArray(barCount) }
    val fftSnapshot = rememberUpdatedState(fft.copyOf())

    LaunchedEffect(Unit) {
        while (true) {
            val newBar = (fftSnapshot.value.lastOrNull() ?: 0f).coerceIn(0f, 1f)
            for (i in 0 until barCount - 1) target[i] = target[i + 1]
            target[barCount - 1] = newBar
            delay(16)
        }
    }

    Canvas(modifier.fillMaxSize()) {
        val barWidth = size.width / barCount
        val centerY = size.height / 2f
        for (i in 0 until barCount) {
            current[i] = lerp(current[i], target[i], 0.2f)
            val barHeight = current[i] * size.height / 2f
            drawRect(
                brush = gradientColors?.let { Brush.verticalGradient(it) }
                    ?: SolidColor(color),
                topLeft = Offset(i * barWidth, centerY - barHeight),
                size = Size(barWidth, barHeight * 2)
            )
        }
    }
}

@Composable
fun CurvedVisualizer(
    fft: FloatArray,
    modifier: Modifier = Modifier,
    gradientColors: List<Color>? = null,
    color: Color = defaultColor
) {
    val pointCount = 128
    val target = remember { FloatArray(pointCount) }
    val current = remember { FloatArray(pointCount) }
    val fftSnapshot = rememberUpdatedState(fft.copyOf())

    LaunchedEffect(Unit) {
        while (true) {
            val normalized = FloatArray(pointCount) { i ->
                fftSnapshot.value.getOrNull(i)?.coerceIn(0f, 1f) ?: 0f
            }
            for (i in 0 until pointCount - 1) target[i] = target[i + 1]
            target[pointCount - 1] = normalized.lastOrNull() ?: 0f
            delay(16)
        }
    }

    Canvas(modifier.fillMaxSize()) {
        val widthPerPoint = size.width / (pointCount - 1)
        val centerY = size.height / 2f
        for (i in 0 until pointCount) {
            current[i] = lerp(current[i], target[i], 0.2f)
        }

        val path = Path().apply {
            moveTo(0f, centerY)
            for (i in 1 until pointCount) {
                val x1 = (i - 1) * widthPerPoint
                val x2 = i * widthPerPoint
                val y1 = centerY - (current[i - 1] - 0.5f) * size.height
                val y2 = centerY - (current[i] - 0.5f) * size.height
                val midX = (x1 + x2) / 2
                val midY = (y1 + y2) / 2
                quadraticBezierTo(x1, y1, midX, midY)
            }
        }

        drawPath(
            path,
            brush = gradientColors?.let { Brush.horizontalGradient(it) }
                ?: SolidColor(color),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
fun CircularVisualizer(
    fft: FloatArray,
    modifier: Modifier = Modifier,
    gradientColors: List<Color>? = null,
    color: Color = defaultColor
) {
    val pointCount = 128
    val target = remember { FloatArray(pointCount) }
    val current = remember { FloatArray(pointCount) }
    val fftSnapshot = rememberUpdatedState(fft.copyOf())

    LaunchedEffect(Unit) {
        while (true) {
            val normalized = FloatArray(pointCount) { i ->
                fftSnapshot.value.getOrNull(i)?.coerceIn(0f, 1f) ?: 0f
            }
            for (i in 0 until pointCount - 1) target[i] = target[i + 1]
            target[pointCount - 1] = normalized.lastOrNull() ?: 0f
            delay(16)
        }
    }

    Canvas(modifier.fillMaxSize()) {
        val radius = min(size.width, size.height) / 3f
        val center = Offset(size.width / 2f, size.height / 2f)
        for (i in 0 until pointCount) {
            current[i] = lerp(current[i], target[i], 0.2f)
        }

        val path = Path()
        for (i in 0..pointCount) {
            val index = i % pointCount
            val angle = (index.toFloat() / pointCount) * 2f * PI.toFloat()
            val magnitude = current[index]
            val offset = radius + (magnitude * radius * 0.8f)
            val x = center.x + cos(angle) * offset
            val y = center.y + sin(angle) * offset

            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)

            val alpha = index / pointCount.toFloat()
            drawCircle(
                color = color.copy(alpha = alpha),
                radius = 3.dp.toPx(),
                center = Offset(x, y)
            )
        }

        drawPath(
            path,
            brush = gradientColors?.let { Brush.sweepGradient(it) }
                ?: SolidColor(color.copy(alpha = 0.3f)),
            style = Stroke(width = 1.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}





