package dev.nstv.easing.symphony.musicvisualizer

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.VectorizedAnimationSpec
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.launch
import kotlin.math.abs

@Suppress("UNCHECKED_CAST")
class LogDecaySpec(
    private val stiffness: Float = 50f,
    private val epsilon: Float = 0.001f
) : AnimationSpec<Float> {

    override fun <V : AnimationVector> vectorize(
        converter: TwoWayConverter<Float, V>
    ): VectorizedAnimationSpec<V> {
        return object : VectorizedAnimationSpec<V> {

            override val isInfinite: Boolean = false

            private fun cast(vector: AnimationVector): AnimationVector1D =
                vector as? AnimationVector1D
                    ?: error("LogDecaySpec only supports AnimationVector1D")

            override fun getValueFromNanos(
                playTimeNanos: Long,
                initialValue: V,
                targetValue: V,
                initialVelocity: V
            ): V {
                val timeSec = playTimeNanos / 1_000_000_000f
                val progress = 1f - (1f / (1f + stiffness * timeSec))

                val start = cast(initialValue).value
                val end = cast(targetValue).value
                val lerped = lerp(start, end, progress)

                return AnimationVector1D(lerped) as V
            }

            override fun getVelocityFromNanos(
                playTimeNanos: Long,
                initialValue: V,
                targetValue: V,
                initialVelocity: V
            ): V {
                val deltaT = 1_000_000L // 1 microsecond
                val v1 =
                    getValueFromNanos(playTimeNanos, initialValue, targetValue, initialVelocity)
                val v2 = getValueFromNanos(
                    playTimeNanos + deltaT,
                    initialValue,
                    targetValue,
                    initialVelocity
                )

                val vel = (cast(v2).value - cast(v1).value) / (deltaT / 1_000_000_000f)
                return AnimationVector1D(vel) as V
            }

            override fun getDurationNanos(
                initialValue: V,
                targetValue: V,
                initialVelocity: V
            ): Long {
                val start = cast(initialValue).value
                val end = cast(targetValue).value
                val distance = abs(end - start)
                val timeToConverge = 1f / (stiffness * (epsilon + 1e-6f))
                return (timeToConverge * 1_000_000_000L).toLong()
            }
        }
    }
}

@Composable
fun LogDecayBarsVisualizer(
    fft: FloatArray,
    modifier: Modifier = Modifier,
    barColor: Color = defaultColor
) {
    val scope = rememberCoroutineScope()
    val barCount = fft.size
    val animatables = remember {
        List(barCount) { Animatable(0f) }
    }

    val animationSpec = remember { LogDecaySpec(stiffness = 80f) }

    LaunchedEffect(fft) {
        fft.forEachIndexed { i, target ->
            scope.launch {
                animatables[i].animateTo(target, animationSpec = animationSpec)
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val barWidth = size.width / (barCount * 1.5f)
        val space = barWidth / 2
        animatables.forEachIndexed { i, animatable ->
            val height = animatable.value * size.height
            val x = i * (barWidth + space)
            drawRect(
                color = barColor,
                topLeft = Offset(x, size.height - height),
                size = Size(barWidth, height)
            )
        }
    }
}

