package dev.nstv.easing.symphony.animationspec

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring.DampingRatioHighBouncy
import androidx.compose.animation.core.Spring.DampingRatioMediumBouncy
import androidx.compose.animation.core.Spring.StiffnessHigh
import androidx.compose.animation.core.Spring.StiffnessMedium
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.VectorizedAnimationSpec
import androidx.compose.animation.core.VectorizedFiniteAnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.util.lerp
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.hypot
import kotlin.math.ln
import kotlin.math.sin

enum class CustomOffsetAnimationSpec {
    Simple_Sine,
    CartesianSine,
    CartesianSineDecay,
    CartesianJitter,
    SpiralSine,
    SpiralSineCircle,
    SpiralLog,
    SpiralArchimedean,
    SpiralFibonacci,
    SpiralWiggle,
    Drift,
    Tween,
    Spring,
    Snap;

    fun toAnimationSpec(durationMillis: Int = DEFAULT_DURATION): AnimationSpec<Offset> =
        when (this) {
            Simple_Sine -> SimpleSineAnimationSpec(durationMillis = durationMillis)
            Drift -> DriftOffsetSpec(durationMillis = durationMillis)
            Tween -> tween(easing = LinearEasing, durationMillis = durationMillis)
            Spring -> spring(dampingRatio = DampingRatioMediumBouncy, stiffness = StiffnessMedium)
            Snap -> snap()
            CartesianSine -> sineWaveSpec(durationMillis = durationMillis)
            CartesianSineDecay -> sineWaveDecaySpec(durationMillis = durationMillis)
            CartesianJitter -> jitterySpec(durationMillis = durationMillis)
            SpiralSine -> sineSpiralSpec(durationMillis = durationMillis)
            SpiralSineCircle -> sineWaveCircleSpec(durationMillis = durationMillis)
            SpiralLog -> SpiralLogAnimationSpec(durationMillis = durationMillis)
            SpiralArchimedean -> archimedeanSpiralSpec(durationMillis = durationMillis)
            SpiralFibonacci -> fibonacciSpiralSpec(durationMillis = durationMillis)
            SpiralWiggle -> wiggleSpiralSpec(durationMillis = durationMillis)
        }

    fun toInvertedAnimationSpec(): AnimationSpec<Offset> = when (this) {
        Simple_Sine -> SimpleSineAnimationSpec()
        Drift -> DriftOffsetSpec()
        Tween -> tween(easing = LinearEasing)
        Spring -> spring(dampingRatio = DampingRatioHighBouncy, stiffness = StiffnessHigh)
        Snap -> snap()
        CartesianSine -> sineWaveSpec(inverted = true)
        CartesianSineDecay -> sineWaveDecaySpec(inverted = true)
        CartesianJitter -> jitterySpec(inverted = true)
        SpiralSine -> sineSpiralSpec(inverted = true)
        SpiralSineCircle -> sineWaveCircleSpec()
        SpiralLog -> SpiralLogAnimationSpec(inverted = true)
        SpiralArchimedean -> archimedeanSpiralSpec(inverted = true)
        SpiralFibonacci -> fibonacciSpiralSpec(inverted = true)
        SpiralWiggle -> wiggleSpiralSpec(inverted = true)
    }

    companion object {
        fun getAnimationSpecMap(): Map<String, AnimationSpec<Offset>> =
            entries.associate { it.name to it.toAnimationSpec() }

        fun getInvertedAnimationSpecMap(): Map<String, AnimationSpec<Offset>> =
            entries.associate { it.name to it.toInvertedAnimationSpec() }
    }
}

// --- Simple SineSpec (no projection) ---

@Suppress("UNCHECKED_CAST")
class SimpleSineAnimationSpec(val durationMillis: Int = DEFAULT_DURATION) : AnimationSpec<Offset> {
    override fun <V : AnimationVector> vectorize(converter: TwoWayConverter<Offset, V>): VectorizedAnimationSpec<V> {
        return MyVectorSpec(durationMillis) as VectorizedAnimationSpec<V>
    }
}

class MyVectorSpec(val durationMillis: Int) : VectorizedAnimationSpec<AnimationVector2D> {
    private val waveCount: Int = 3
    private val amplitude: Float = 40f
    private val durationNanos = durationMillis.toNanos()

    override val isInfinite: Boolean = false
    override fun getDurationNanos(
        initialValue: AnimationVector2D,
        targetValue: AnimationVector2D,
        initialVelocity: AnimationVector2D
    ): Long = durationNanos

    override fun getValueFromNanos(
        playTimeNanos: Long,
        initialValue: AnimationVector2D,
        targetValue: AnimationVector2D,
        initialVelocity: AnimationVector2D
    ): AnimationVector2D {
        val progress = playTimeNanos / durationNanos.toFloat()

        val startX = initialValue.v1
        val startY = initialValue.v2
        val endX = targetValue.v1
        val endY = targetValue.v2

        val dx = endX - startX
        val dy = endY - startY

        val distance = hypot(dx, dy).coerceAtLeast(0.0001f)
        val directionX = dx / distance
        val directionY = -dy / distance

        val baseX = startX + dx * progress
        val baseY = startY + dy * progress

        val offsetAmount = sin(progress * waveCount * 2f * PI).toFloat() * amplitude

        return AnimationVector2D(
            baseX + directionY * offsetAmount,
            baseY + directionX * offsetAmount
        )
    }

    override fun getVelocityFromNanos(
        playTimeNanos: Long,
        initialValue: AnimationVector2D,
        targetValue: AnimationVector2D,
        initialVelocity: AnimationVector2D
    ) = AnimationVector2D(0f, 0f)

}

// --- SpiralSpec ---
class SpiralLogAnimationSpec(
    private val turns: Int = 4,
    private val durationMillis: Int = 1000,
    private val inverted: Boolean = false
) : FiniteAnimationSpec<Offset> {

    override fun <V : AnimationVector> vectorize(
        converter: TwoWayConverter<Offset, V>
    ): VectorizedFiniteAnimationSpec<V> {
        @Suppress("UNCHECKED_CAST")
        return SpiralLogVectorizedSpec2D(
            turns,
            durationMillis,
            inverted
        ) as VectorizedFiniteAnimationSpec<V>
    }
}


class SpiralLogVectorizedSpec2D(
    private val spiralTurns: Int,
    private val durationMillis: Int,
    private val inverted: Boolean
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
        val progress =
            (playTimeNanos / 1_000_000f).coerceIn(0f, durationMillis.toFloat()) / durationMillis

        val startX = initialValue.v1
        val startY = initialValue.v2
        val endX = targetValue.v1
        val endY = targetValue.v2

        // Vector from end (center) to start
        val dx = startX - endX
        val dy = startY - endY

        val r1 = hypot(dx, dy)
        val theta1 = atan2(dy, dx)
        val deltaTheta = (spiralTurns * 2f * PI).toFloat()
        val theta2 = if (inverted) theta1 - deltaTheta else theta1 + deltaTheta

        val r2 = 0.01f
        val lnRRatio = ln(r1 / r2)
        val b = lnRRatio / (theta1 - theta2)
        val a = r1 / exp(b * theta1)

        val t = theta1 + (theta2 - theta1) * progress
        val r = a * exp(b * t)

        val x = r * cos(t) + endX
        val y = r * sin(t) + endY

        return AnimationVector2D(x, y)
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
            getDurationNanos(
                initialValue,
                targetValue,
                initialVelocity
            )
        )

        val v1 = getValueFromNanos(t1, initialValue, targetValue, initialVelocity)
        val v2 = getValueFromNanos(t2, initialValue, targetValue, initialVelocity)

        return AnimationVector2D(
            (v2.v1 - v1.v1) / (delta / 1_000_000f),
            (v2.v2 - v1.v2) / (delta / 1_000_000f)
        )
    }
}

class DriftOffsetSpec(private val durationMillis: Int = 1000) : AnimationSpec<Offset> {
    override fun <V : AnimationVector> vectorize(converter: TwoWayConverter<Offset, V>): VectorizedAnimationSpec<V> =
        object : VectorizedAnimationSpec<V> {
            override val isInfinite: Boolean = false
            override fun getDurationNanos(
                initialValue: V,
                targetValue: V,
                initialVelocity: V
            ): Long = durationMillis * 1_000_000L

            override fun getValueFromNanos(
                playTimeNanos: Long,
                initialValue: V,
                targetValue: V,
                initialVelocity: V
            ): V {
                val t = playTimeNanos / 1_000_000f / durationMillis
                val driftT = t * 0.5f + sin(t * PI).toFloat() * 0.5f
                val start = converter.convertFromVector(initialValue)
                val end = converter.convertFromVector(targetValue)
                val interpolated = Offset(
                    lerp(start.x, end.x, driftT),
                    lerp(start.y, end.y, driftT)
                )
                return converter.convertToVector(interpolated)
            }

            override fun getVelocityFromNanos(
                playTimeNanos: Long,
                initialValue: V,
                targetValue: V,
                initialVelocity: V
            ): V = converter.convertToVector(Offset.Zero)
        }
}
