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
import dev.nstv.easing.symphony.musicvisualizer.VisualizerType.*
import dev.nstv.easing.symphony.musicvisualizer.reader.MusicReader
import dev.nstv.easing.symphony.musicvisualizer.reader.MusicReader.Companion.FRAME_DELAY_MILLIS
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*

enum class VisualizerType {
    Simple,
    Scrolling,
    ScrollingAnimatable,
    CenteredBars,
    CenteredBarsAnimatable,
    Curved,
    CurvedAnimatable,
    CurvedMirrored,
    CurvedMirroredAnimatable,
    Circular,
    CircularBurst,
    WavingCircle,
    LogDecay;
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

val defaultColor = TileColor.Pink
val defaultAmplitud = 10f

@Composable
fun EffectVisualizer(
    fft: FloatArray,
    modifier: Modifier = Modifier,
    visualizerType: VisualizerType = Simple,
    gradientColors: List<Color> = Neon.getColors()
) {

    when (visualizerType) {
        Simple -> SimpleVisualizer(fft, modifier, gradientColors)
        Scrolling -> EfficientScrollingVisualizer(fft, modifier, gradientColors)
        ScrollingAnimatable -> EfficientScrollingVisualizerAnimatable(fft, modifier, gradientColors)
        CenteredBars -> CenteredVisualizer(fft, modifier, gradientColors)
        CenteredBarsAnimatable -> CenteredVisualizerAnimatable(fft, modifier, gradientColors)
        Curved -> CurvedVisualizer(fft, modifier, gradientColors)
        CurvedAnimatable -> CurvedVisualizerAnimatable(fft, modifier, gradientColors)
        CurvedMirrored -> MirrorCurvedVisualizer(fft, modifier, gradientColors)
        CurvedMirroredAnimatable -> MirrorCurvedVisualizerAnimatable(fft, modifier, gradientColors)
        Circular -> CircularVisualizer(fft, modifier, gradientColors)
        CircularBurst -> FlippingCircularBurstVisualizer(fft, modifier, gradientColors)
        WavingCircle -> WavingCircleVisualizer(fft, modifier, gradientColors)
        LogDecay -> LogDecayBarsVisualizer(fft)
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
            val height = (value * size.height).coerceIn(0f, size.height)
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
fun EfficientScrollingVisualizerAnimatable(
    fft: FloatArray,
    modifier: Modifier = Modifier,
    gradientColors: List<Color>? = null,
    color: Color = Color.Cyan,
) {
    val barCount = 100
    val animBars = remember { List(barCount) { Animatable(0f) } }
    val fftSnapshot = rememberUpdatedState(fft.copyOf())
    val scope = rememberCoroutineScope()
    val animationTime = remember { (FRAME_DELAY_MILLIS * 0.75).toInt() }

    LaunchedEffect(Unit) {
        while (true) {
            val newValue = (fftSnapshot.value.lastOrNull() ?: 0f).coerceIn(0f, 1f)

            // Snapshot current bar values before shifting
            val currentValues = animBars.map { it.value }

            // Now shift using those stable values
            for (i in 0 until barCount - 1) {
                animBars[i].snapTo(currentValues[i + 1])
            }

            // Animate the last bar to new FFT value
            scope.launch {
                animBars[barCount - 1].animateTo(
                    newValue,
                    animationSpec = tween(
                        durationMillis = animationTime,
                        easing = LinearOutSlowInEasing
                    )
                )
            }

            delay(FRAME_DELAY_MILLIS)
        }
    }

    Canvas(modifier.fillMaxSize()) {
        val barWidth = size.width / barCount
        animBars.forEachIndexed { i, anim ->
            val height = anim.value * size.height
            drawRect(
                brush = gradientColors?.let { Brush.verticalGradient(it) }
                    ?: Brush.verticalGradient(listOf(color)),
                topLeft = Offset(i * barWidth, size.height - height),
                size = Size(barWidth, height)
            )
        }
    }
}


@Composable
fun CenteredVisualizer(
    fft: FloatArray,
    modifier: Modifier = Modifier,
    gradientColors: List<Color>? = null,
    color: Color = defaultColor,
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
fun CenteredVisualizerAnimatable(
    fft: FloatArray,
    modifier: Modifier = Modifier,
    gradientColors: List<Color>? = null,
    color: Color = Color.Cyan
) {
    val barCount = 100
    val animBars = remember { List(barCount) { Animatable(0f) } }
    val fftSnapshot = rememberUpdatedState(fft.copyOf())
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        while (true) {
            val newValue = (fftSnapshot.value.lastOrNull() ?: 0f).coerceIn(0f, 1f)

            // Take snapshot before shifting
            val currentValues = animBars.map { it.value }

            for (i in 0 until barCount - 1) {
                animBars[i].snapTo(currentValues[i + 1])
            }

            scope.launch {
                animBars[barCount - 1].animateTo(
                    newValue,
                    animationSpec = tween(80, easing = LinearOutSlowInEasing)
                )
            }

            delay(80)
        }
    }

    Canvas(modifier.fillMaxSize()) {
        val barWidth = size.width / barCount
        val centerY = size.height / 2f

        animBars.forEachIndexed { i, anim ->
            val barHeight = anim.value * size.height / 2f
            drawRect(
                brush = gradientColors?.let { Brush.verticalGradient(it) }
                    ?: Brush.verticalGradient(listOf(color)),
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
    color: Color = defaultColor,
    amplitudeScale: Float = defaultAmplitud // as % of canvas height
) {
    val pointCount = fft.size
    val target = remember { FloatArray(pointCount) }
    val current = remember { FloatArray(pointCount) }
    val fftSnapshot = rememberUpdatedState(fft.copyOf())

    LaunchedEffect(Unit) {
        while (true) {
            val snapshot = fftSnapshot.value
            val max = snapshot.maxOrNull()?.takeIf { it > 0f } ?: 1f

            val normalized = FloatArray(pointCount) { i ->
                (snapshot.getOrNull(i)?.coerceIn(0f, 1f) ?: 0f) / max
            }

            for (i in 0 until pointCount - 1) {
                target[i] = target[i + 1]
            }
            target[pointCount - 1] = normalized.lastOrNull() ?: 0f

            delay(16)
        }
    }

    Canvas(modifier.fillMaxSize()) {
        val widthPerPoint = size.width / (pointCount - 1)
        val centerY = size.height / 2f
        val verticalScale = (size.height / 2f) * amplitudeScale

        for (i in 0 until pointCount) {
            current[i] = lerp(current[i], target[i], 0.2f)
        }

        val path = Path().apply {
            moveTo(0f, centerY - current[0] * verticalScale)
            for (i in 1 until pointCount) {
                val x1 = (i - 1) * widthPerPoint
                val x2 = i * widthPerPoint
                val y1 = centerY - current[i - 1] * verticalScale
                val y2 = centerY - current[i] * verticalScale
                val midX = (x1 + x2) / 2
                val midY = (y1 + y2) / 2
                quadraticTo(x1, y1, midX, midY)
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
fun CurvedVisualizerAnimatable(
    fft: FloatArray,
    modifier: Modifier = Modifier,
    gradientColors: List<Color>? = null,
    color: Color = defaultColor,
    amplitudeScale: Float = defaultAmplitud
) {
    val pointCount = 128
    val animPoints = remember { List(pointCount) { Animatable(0f) } }
    val fftSnapshot = rememberUpdatedState(fft.copyOf())
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        while (true) {
            val snapshot = fftSnapshot.value
            val max = snapshot.maxOrNull()?.takeIf { it > 0f } ?: 1f
            val newValue = (snapshot.lastOrNull() ?: 0f).coerceIn(0f, 1f) / max

            // Pre-capture all current values
            val current = animPoints.map { it.value }

            // Shift all values one left
            for (i in 0 until pointCount - 1) {
                animPoints[i].snapTo(current[i + 1])
            }

            // Animate the last point to the newest value
            scope.launch {
                animPoints[pointCount - 1].animateTo(
                    newValue,
                    animationSpec = tween(80, easing = LinearOutSlowInEasing)
                )
            }

            delay(80)
        }
    }

    Canvas(modifier.fillMaxSize()) {
        val widthPerPoint = size.width / (pointCount - 1)
        val centerY = size.height / 2f
        val scale = size.height / 2f * amplitudeScale

        val path = Path().apply {
            moveTo(0f, centerY - animPoints[0].value * scale)
            for (i in 1 until pointCount) {
                val x1 = (i - 1) * widthPerPoint
                val x2 = i * widthPerPoint
                val y1 = centerY - animPoints[i - 1].value * scale
                val y2 = centerY - animPoints[i].value * scale
                val midX = (x1 + x2) / 2
                val midY = (y1 + y2) / 2
                quadraticTo(x1, y1, midX, midY)
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
fun MirrorCurvedVisualizer(
    fft: FloatArray,
    modifier: Modifier = Modifier,
    gradientColors: List<Color>? = null,
    color: Color = defaultColor,
    amplitudeScale: Float = defaultAmplitud
) {
    val pointCount = fft.size
    val target = remember { FloatArray(pointCount) }
    val current = remember { FloatArray(pointCount) }
    val fftSnapshot = rememberUpdatedState(fft.copyOf())

    LaunchedEffect(Unit) {
        while (true) {
            val snapshot = fftSnapshot.value
            val max = snapshot.maxOrNull()?.takeIf { it > 0f } ?: 1f

            val normalized = FloatArray(pointCount) { i ->
                (snapshot.getOrNull(i)?.coerceIn(0f, 1f) ?: 0f) / max
            }

            for (i in 0 until pointCount - 1) {
                target[i] = target[i + 1]
            }
            target[pointCount - 1] = normalized.lastOrNull() ?: 0f

            delay(16)
        }
    }

    Canvas(modifier.fillMaxSize()) {
        val widthPerPoint = size.width / (pointCount - 1)
        val centerY = size.height / 2f
        val verticalScale = (size.height / 2f) * amplitudeScale

        for (i in 0 until pointCount) {
            current[i] = lerp(current[i], target[i], 0.2f)
        }

        // Top half path
        val topPath = Path().apply {
            moveTo(0f, centerY - current[0] * verticalScale)
            for (i in 1 until pointCount) {
                val x1 = (i - 1) * widthPerPoint
                val x2 = i * widthPerPoint
                val y1 = centerY - current[i - 1] * verticalScale
                val y2 = centerY - current[i] * verticalScale
                val midX = (x1 + x2) / 2
                val midY = (y1 + y2) / 2
                quadraticTo(x1, y1, midX, midY)
            }
        }

        // Bottom mirrored waveform
        val bottomPath = Path().apply {
            moveTo(0f, centerY + current[0] * verticalScale)
            for (i in 1 until pointCount) {
                val x1 = (i - 1) * widthPerPoint
                val x2 = i * widthPerPoint
                val y1 = centerY + current[i - 1] * verticalScale
                val y2 = centerY + current[i] * verticalScale
                val midX = (x1 + x2) / 2
                val midY = (y1 + y2) / 2
                quadraticTo(x1, y1, midX, midY)
            }
        }

        val brush = gradientColors?.let { Brush.horizontalGradient(it) } ?: SolidColor(color)

        drawPath(topPath, brush = brush, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
        drawPath(
            bottomPath,
            brush = brush,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
fun MirrorCurvedVisualizerAnimatable(
    fft: FloatArray,
    modifier: Modifier = Modifier,
    gradientColors: List<Color>? = null,
    color: Color = defaultColor,
    amplitudeScale: Float = defaultAmplitud,
) {
    val pointCount = 128
    val animPoints = remember { List(pointCount) { Animatable(0f) } }
    val fftSnapshot = rememberUpdatedState(fft.copyOf())
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        while (true) {
            val snapshot = fftSnapshot.value
            val max = snapshot.maxOrNull()?.takeIf { it > 0f } ?: 1f
            val newValue = (snapshot.lastOrNull() ?: 0f).coerceIn(0f, 1f) / max

            val current = animPoints.map { it.value }

            // Shift left
            for (i in 0 until pointCount - 1) {
                animPoints[i].snapTo(current[i + 1])
            }

            // Animate new value at the end
            scope.launch {
                animPoints[pointCount - 1].animateTo(
                    newValue,
                    animationSpec = tween(80, easing = LinearOutSlowInEasing)
                )
            }

            delay(80)
        }
    }

    Canvas(modifier.fillMaxSize()) {
        val widthPerPoint = size.width / (pointCount - 1)
        val centerY = size.height / 2f
        val scale = size.height / 2f * amplitudeScale
        val values = animPoints.map { it.value }

        val topPath = Path().apply {
            moveTo(0f, centerY - values[0] * scale)
            for (i in 1 until pointCount) {
                val x1 = (i - 1) * widthPerPoint
                val x2 = i * widthPerPoint
                val y1 = centerY - values[i - 1] * scale
                val y2 = centerY - values[i] * scale
                val midX = (x1 + x2) / 2
                val midY = (y1 + y2) / 2
                quadraticTo(x1, y1, midX, midY)
            }
        }

        val bottomPath = Path().apply {
            moveTo(0f, centerY + values[0] * scale)
            for (i in 1 until pointCount) {
                val x1 = (i - 1) * widthPerPoint
                val x2 = i * widthPerPoint
                val y1 = centerY + values[i - 1] * scale
                val y2 = centerY + values[i] * scale
                val midX = (x1 + x2) / 2
                val midY = (y1 + y2) / 2
                quadraticTo(x1, y1, midX, midY)
            }
        }

        val brush = gradientColors?.let { Brush.horizontalGradient(it) } ?: SolidColor(color)

        drawPath(topPath, brush = brush, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
        drawPath(
            bottomPath,
            brush = brush,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}


@Composable
fun CircularVisualizer(
    fft: FloatArray,
    modifier: Modifier = Modifier,
    gradientColors: List<Color>? = null,
    color: Color = defaultColor,
    amplitudeScale: Float = 50f // increase to make curve pop
) {
    val pointCount = 128
    val animPoints = remember { List(pointCount) { Animatable(0f) } }
    val fftSnapshot = rememberUpdatedState(fft.copyOf())
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        while (true) {
            val snapshot = fftSnapshot.value
            val max = snapshot.maxOrNull()?.takeIf { it > 0f } ?: 1f
            val newValue = (snapshot.lastOrNull() ?: 0f).coerceIn(0f, 1f) / max

            val current = animPoints.map { it.value }

            // Scroll left
            for (i in 0 until pointCount - 1) {
                animPoints[i].snapTo(current[i + 1])
            }

            // Animate latest point
            scope.launch {
                animPoints[pointCount - 1].animateTo(
                    newValue,
                    animationSpec = tween(80, easing = LinearOutSlowInEasing)
                )
            }

            delay(80)
        }
    }

    Canvas(modifier.fillMaxSize()) {
        val radius = min(size.width, size.height) / 4f
        val center = Offset(size.width / 2f, size.height / 2f)
        val scale = radius * amplitudeScale

        val path = Path()

        for (i in 0..pointCount) {
            val idx = i % pointCount
            val angle = (idx.toFloat() / pointCount) * 2f * PI.toFloat()
            val magnitude = animPoints[idx].value
            val distance = radius + magnitude * scale
            val x = center.x + cos(angle) * distance
            val y = center.y + sin(angle) * distance

            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)

            // Optional trailing dots
            drawCircle(
                color = color.copy(alpha = idx / pointCount.toFloat()),
                radius = 3.dp.toPx(),
                center = Offset(x, y)
            )
        }

        drawPath(
            path,
            brush = gradientColors?.let { Brush.sweepGradient(it) }
                ?: SolidColor(color.copy(alpha = 0.3f)),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
fun CircularBurstVisualizer(
    fft: FloatArray,
    modifier: Modifier = Modifier,
    gradientColors: List<Color>? = null,
    color: Color = defaultColor,
    amplitudeScale: Float = 3f
) {
    val pointCount = 64
    val animPoints = remember { List(pointCount) { Animatable(0f) } }
    val fftSnapshot = rememberUpdatedState(fft.copyOf())

    LaunchedEffect(Unit) {
        while (true) {
            val snapshot = fftSnapshot.value
            val max = snapshot.maxOrNull()?.takeIf { it > 0f } ?: 1f

            repeat(pointCount) { i ->
                val fftValue = (snapshot.getOrNull(i) ?: 0f).coerceIn(0f, 1f) / max
                animPoints[i].animateTo(
                    fftValue,
                    animationSpec = tween(80, easing = LinearOutSlowInEasing)
                )
            }

            delay(80)
        }
    }

    Canvas(modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val baseRadius = min(size.width, size.height) / 4f
        val scale = baseRadius * amplitudeScale

        repeat(pointCount) { i ->
            val angle = (i.toFloat() / pointCount) * 2f * PI.toFloat()
            val value = animPoints[i].value
            val endOffset = baseRadius + value * scale

            val x = center.x + cos(angle) * endOffset
            val y = center.y + sin(angle) * endOffset

            val brush = gradientColors?.let {
                Brush.radialGradient(it, center = center, radius = endOffset)
            } ?: SolidColor(color)

            drawLine(
                brush = brush,
                start = center,
                end = Offset(x, y),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun FlippingCircularBurstVisualizer(
    fft: FloatArray,
    modifier: Modifier = Modifier,
    gradientColors: List<Color>? = null,
    color: Color = defaultColor,
    amplitudeScale: Float = 0.5f,
    flipIntervalFrames: Int = 4 // Flip every 4 frames (~320ms at 80ms/frame)
) {
    val pointCount = 40//64
    val animPoints = remember { List(pointCount) { Animatable(0f) } }
    val fftSnapshot = rememberUpdatedState(fft.copyOf())
    var flip by remember { mutableStateOf(false) }
    var frameCounter by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            val snapshot = fftSnapshot.value
            val max = snapshot.maxOrNull()?.coerceIn(0f, 1f) ?: 0f
            val min = snapshot.minOrNull()?.coerceIn(0f, 1f) ?: 0f

            val targets = List(pointCount) { i ->
                val useMax = if (flip) i % 2 != 0 else i % 2 == 0
                if (useMax) max else min
            }

            coroutineScope {
                targets.forEachIndexed { i, value ->
                    launch {
                        animPoints[i].animateTo(
                            value,
                            animationSpec = tween(80, easing = LinearOutSlowInEasing)
                        )
                    }
                }
            }

            frameCounter++
            if (frameCounter >= flipIntervalFrames) {
                flip = !flip
                frameCounter = 0
            }

            delay(80)
        }
    }

    Canvas(modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val baseRadius = min(size.width, size.height) / 4f
        val scale = baseRadius * amplitudeScale

        animPoints.forEachIndexed { i, anim ->
            val angle = (i.toFloat() / pointCount) * 2f * PI.toFloat()
            val value = anim.value
            val endOffset = baseRadius + value * scale

            val x = center.x + cos(angle) * endOffset
            val y = center.y + sin(angle) * endOffset

            val brush = gradientColors?.let {
                Brush.radialGradient(it, center = center, radius = endOffset)
            } ?: SolidColor(color)

            drawLine(
                brush = brush,
                start = center,
                end = Offset(x, y),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun WavingCircleVisualizer(
    fft: FloatArray,
    modifier: Modifier = Modifier,
    gradientColors: List<Color>? = null,
    color: Color = defaultColor,
    amplitudeScale: Float = 0.5f,
    flipIntervalFrames: Int = 4
) {
    val pointCount = 128
    val animPoints = remember { List(pointCount) { Animatable(0f) } }
    val fftSnapshot = rememberUpdatedState(fft.copyOf())
//    var flip by remember { mutableStateOf(false) }
    var frameCounter by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            val snapshot = fftSnapshot.value
            val max = snapshot.maxOrNull()?.coerceIn(0f, 1f) ?: 0f
            val min = snapshot.minOrNull()?.coerceIn(0f, 1f) ?: 0f

            val targets = List(pointCount) { i ->
                val useMax = if (true) i % 2 != 0 else i % 2 == 0
                if (useMax) max else min
            }

            coroutineScope {
                targets.forEachIndexed { i, value ->
                    launch {
                        animPoints[i].animateTo(
                            value,
                            animationSpec = tween(80, easing = LinearOutSlowInEasing)
                        )
                    }
                }
            }

            frameCounter++
            if (frameCounter >= flipIntervalFrames) {
//                flip = !flip
                frameCounter = 0
            }

            delay(80)
        }
    }

    Canvas(modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val baseRadius = min(size.width, size.height) / 3f
        val scale = baseRadius * amplitudeScale

        val path = Path()
        animPoints.forEachIndexed { i, anim ->
            val angle = (i.toFloat() / pointCount) * 2f * PI.toFloat()
            val value = anim.value
            val r = baseRadius + value * scale
            val x = center.x + cos(angle) * r
            val y = center.y + sin(angle) * r

            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        path.close()

        drawPath(
            path,
            brush = gradientColors?.let { Brush.sweepGradient(it, center) }
                ?: SolidColor(color),
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}













