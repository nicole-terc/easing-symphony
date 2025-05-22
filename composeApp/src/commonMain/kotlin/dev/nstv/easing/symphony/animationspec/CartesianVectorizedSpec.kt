package dev.nstv.easing.symphony.animationspec

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.VectorizedAnimationSpec
import androidx.compose.animation.core.VectorizedFiniteAnimationSpec
import androidx.compose.ui.geometry.Offset
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

class CartesianAnimationSpec(
    private val durationMillis: Int = DEFAULT_DURATION,
    private val inverted: Boolean = false,
    private val offsetFn: (progress: Float, distance: Float, angle: Float) -> Float
) : FiniteAnimationSpec<Offset> {
    override fun <V : AnimationVector> vectorize(
        converter: TwoWayConverter<Offset, V>
    ): VectorizedFiniteAnimationSpec<V> {
        @Suppress("UNCHECKED_CAST")
        return if (converter == Offset.VectorConverter) {
            CartesianVectorizedSpec(
                durationMillis = durationMillis,
                inverted = inverted,
                offsetFn = offsetFn,
            ) as VectorizedFiniteAnimationSpec<V>
        } else {
            error("OffsetFormulaAnimationSpec only supports Offset.VectorConverter.")
        }
    }
}

fun Int.toNanos() = this * 1_000_000L

class CartesianVectorizedSpec(
    private val durationMillis: Int,
    private val inverted: Boolean = false,
    private val offsetFn: (progress: Float, distance: Float, angle: Float) -> Float
) : VectorizedFiniteAnimationSpec<AnimationVector2D> {

    override val isInfinite: Boolean get() = false

    override fun getDurationNanos(
        initialValue: AnimationVector2D,
        targetValue: AnimationVector2D,
        initialVelocity: AnimationVector2D
    ) = durationMillis * 1_000_000L

    override fun getValueFromNanos(
        playTimeNanos: Long,
        initialValue: AnimationVector2D,
        targetValue: AnimationVector2D,
        initialVelocity: AnimationVector2D
    ): AnimationVector2D {
        val t = (playTimeNanos / 1_000_000f).coerceIn(0f, durationMillis.toFloat()) / durationMillis

        val startX = initialValue.v1
        val startY = initialValue.v2
        val endX = targetValue.v1
        val endY = targetValue.v2

        val dx = endX - startX
        val dy = endY - startY
        val distance = hypot(dx, dy).coerceAtLeast(0.0001f)
        val directionX = dx / distance
        val directionY = dy / distance
        val angle = atan2(dy, dx)

        val baseX = startX + dx * t
        val baseY = startY + dy * t

        val perpX = -directionY
        val perpY = directionX

        val offsetAmount = offsetFn(t, distance, angle)
        val appliedOffset = if (inverted) -offsetAmount else offsetAmount

        return AnimationVector2D(
            baseX + perpX * appliedOffset,
            baseY + perpY * appliedOffset
        )
    }

    override fun getVelocityFromNanos(
        playTimeNanos: Long,
        initialValue: AnimationVector2D,
        targetValue: AnimationVector2D,
        initialVelocity: AnimationVector2D
    ): AnimationVector2D {
        val delta = 1_000_000L
        val t1 = playTimeNanos
        val t2 = (playTimeNanos + delta).coerceAtMost(
            getDurationNanos(initialValue, targetValue, initialVelocity)
        )

        val v1 = getValueFromNanos(t1, initialValue, targetValue, initialVelocity)
        val v2 = getValueFromNanos(t2, initialValue, targetValue, initialVelocity)

        return AnimationVector2D(
            (v2.v1 - v1.v1) / (delta / 1_000_000f),
            (v2.v2 - v1.v2) / (delta / 1_000_000f)
        )
    }
}


fun sineWaveSpec(
    durationMillis: Int = DEFAULT_DURATION,
    waveCount: Int = 3,
    amplitude: Float = 20f,
    inverted: Boolean = false,
): AnimationSpec<Offset> = CartesianAnimationSpec(durationMillis, inverted) { progress, _, _ ->
    sin(progress * waveCount * 2f * PI).toFloat() * amplitude
}

fun sineWaveDecaySpec(
    durationMillis: Int = DEFAULT_DURATION,
    waveCount: Int = 5,
    amplitude: Float = 30f,
    inverted: Boolean = false,
): AnimationSpec<Offset> = CartesianAnimationSpec(durationMillis, inverted) { progress, _, _ ->
    val decay = (1f - progress)
    sin(progress * waveCount * 2f * PI).toFloat() * amplitude * decay
}

fun jitterySpec(
    durationMillis: Int = DEFAULT_DURATION,
    seed: Int = 0,
    amplitude: Float = 10f,
    inverted: Boolean = false,
): AnimationSpec<Offset> {
    val random = Random(seed)
    return CartesianAnimationSpec(durationMillis, inverted) { progress, _, _ ->
        val jitter = (random.nextFloat() - 0.5f) * 2f
        jitter * amplitude * (1 - progress)
    }
}

