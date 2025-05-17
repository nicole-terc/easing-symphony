package dev.nstv.easing.symphony.musicvisualizer

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class BurstVisualizerType{
    ReactiveCircle
}

@Composable
fun BursMusicVisualizer(
    fft: FloatArray,
    modifier: Modifier = Modifier,
    visualizerType: BurstVisualizerType = BurstVisualizerType.ReactiveCircle,
    color: Color = defaultColor,
){
    when(visualizerType){
        BurstVisualizerType.ReactiveCircle -> {
            ReactiveCircleVisualizer(
                fftValues = fft,
                modifier = modifier,
                color = color
            )
        }
    }
}

@Composable
fun ReactiveCircleVisualizer(
    fftValues: FloatArray,
    modifier: Modifier = Modifier,
    color: Color = defaultColor,
    baseRadius: Dp = 40.dp,
    maxBoost: Dp = 80.dp,
    animationSpec: AnimationSpec<Float> = spring(dampingRatio = 0.4f, stiffness = 350f),
    lowFreqCount: Int = 8
) {
    val radiusAnim = remember { Animatable(0f) }
    val density = LocalDensity.current

    // Extract low frequency energy
    val bassPower = remember(fftValues) {
        fftValues
            .take(lowFreqCount.coerceAtMost(fftValues.size))
            .average()
            .toFloat()
            .coerceIn(0f, 1f)
    }

    // Animate the radius
    LaunchedEffect(bassPower, baseRadius, maxBoost) {
        val targetRadius = with(density) {
            baseRadius.toPx() + bassPower * maxBoost.toPx()
        }
        radiusAnim.animateTo(targetRadius, animationSpec = animationSpec)
    }

    // Draw the circle
    Canvas(modifier = modifier) {
        drawCircle(
            color = color,
            radius = radiusAnim.value,
            center = center,
            style = Stroke(width = with(density) { 6.dp.toPx() })
        )
    }
}

