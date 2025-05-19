package dev.nstv.easing.symphony.animationspec

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.VectorizedAnimationSpec
import androidx.compose.animation.core.VectorizedFiniteAnimationSpec
import androidx.compose.ui.geometry.Offset
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.hypot
import kotlin.math.ln
import kotlin.math.sin

const val DEFAULT_DURATION = 1000

class SpiralAnimationSpec(
    private val cycles: Int, // Spiral turns, angleSweeps, rotations
    private val durationMillis: Int,
    private val inverted: Boolean,
    private val radiusOffset: (progress: Float, angle: Float) -> Float
) : AnimationSpec<Offset> {
    override fun <V : AnimationVector> vectorize(
        converter: TwoWayConverter<Offset, V>
    ): VectorizedAnimationSpec<V> {
        @Suppress("UNCHECKED_CAST")
        return if (converter == Offset.VectorConverter) {
            SpiralVectorizedSpec(
                cycles = cycles,
                durationMillis = durationMillis,
                inverted = inverted,
                radiusOffset = radiusOffset
            ) as VectorizedAnimationSpec<V>
        } else {
            error("OffsetFormulaAnimationSpec only supports Offset.VectorConverter.")
        }
    }
}

class SpiralVectorizedSpec(
    private val cycles: Int, // Spiral turns, angleSweeps, rotations
    private val durationMillis: Int,
    private val inverted: Boolean,
    private val radiusOffset: (progress: Float, angle: Float) -> Float
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

        val rStart = hypot(dx, dy)
        val thetaStart = atan2(dy, dx)
        val deltaTheta = (cycles * 2f * PI).toFloat()
        val thetaEnd = if (inverted) thetaStart - deltaTheta else thetaStart + deltaTheta

        val theta = thetaStart + (thetaEnd - thetaStart) * progress

        // Base radius shrinking toward 0 (or outward if inverted)
        val baseRadius = rStart * (1 - progress)

        // Apply custom offset formula
        val offset = radiusOffset(progress, theta)
        val r = baseRadius + offset

        val x = r * cos(theta) + endX
        val y = r * sin(theta) + endY

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

fun sineSpiralSpec(
    cycles: Int = 3,
    durationMillis: Int = DEFAULT_DURATION,
    inverted: Boolean = false,
    amplitude: Float = 20f,
    frequency: Float = 10f
): SpiralAnimationSpec = SpiralAnimationSpec(
    cycles = cycles,
    durationMillis = durationMillis,
    inverted = inverted,
    radiusOffset = { _, angle ->
        amplitude * sin(frequency * angle)
    }
)

fun sineWaveCircleSpec(
    durationMillis: Int = DEFAULT_DURATION,
    waveCount: Int = 10,
    amplitude: Float = 20f,
    clockwise: Boolean = true
): SpiralAnimationSpec = SpiralAnimationSpec(
    cycles = 1, // just one full loop
    durationMillis = durationMillis,
    inverted = false, // doesn’t matter much here
    radiusOffset = { _, angle ->
        amplitude * sin(waveCount * angle) * if (clockwise) 1f else -1f
    }
)


fun wiggleSpiralSpec(
    cycles: Int = 3,
    durationMillis: Int = DEFAULT_DURATION,
    inverted: Boolean = false,
    amplitude: Float = 15f,
    freq1: Float = 12f,
    freq2: Float = 7f
): SpiralAnimationSpec = SpiralAnimationSpec(
    cycles = cycles,
    durationMillis = durationMillis,
    inverted = inverted,
    radiusOffset = { progress, angle ->
        val wiggle = sin(freq1 * angle) + sin(freq2 * angle)
        wiggle * amplitude * (1 - progress)
    }
)

fun logSpiralSpec(
    cycles: Int = 3,
    durationMillis: Int = DEFAULT_DURATION,
    inverted: Boolean = false,
    rStart: Float = 100f,
    rEnd: Float = 0.01f
): SpiralAnimationSpec {

    val epsilon = 0.0001f

    val safeRStart = rStart.coerceAtLeast(epsilon)
    val safeREnd = rEnd.coerceAtLeast(epsilon)

    val deltaTheta = cycles * 2f * PI.toFloat()
    val thetaStart = 0f
    val thetaEnd = if (inverted) thetaStart - deltaTheta else thetaStart + deltaTheta

    val thetaDelta = (thetaEnd - thetaStart).takeIf { abs(it) >= epsilon } ?: epsilon

    val lnRRatio = ln(safeRStart / safeREnd)
    val b = lnRRatio / thetaDelta
    val a = safeRStart / exp(b * thetaStart)

    return SpiralAnimationSpec(
        cycles = cycles,
        durationMillis = durationMillis,
        inverted = inverted,
        radiusOffset = { progress, theta ->
            val spiralR = a * exp(b * theta)
            val baseR = safeRStart * (1 - progress)
            spiralR - baseR
        }
    )
}

fun archimedeanSpiralSpec(
    cycles: Int = 4,
    durationMillis: Int = DEFAULT_DURATION,
    inverted: Boolean = false,
    a: Float = 0f,
    b: Float = 15f
): SpiralAnimationSpec = SpiralAnimationSpec(
    cycles = cycles,
    durationMillis = durationMillis,
    inverted = inverted,
    radiusOffset = { progress, angle ->
        val arch = a + b * angle
        arch - (arch * progress) // fade toward center
    }
)

fun fibonacciSpiralSpec(
    cycles: Int = 4,
    durationMillis: Int = DEFAULT_DURATION,
    inverted: Boolean = false,
    a: Float = 1f,
    goldenRatio: Float = 1.618f
): SpiralAnimationSpec {
    val b = ln(goldenRatio) / (PI.toFloat() / 2f) // approx 0.306349
    return SpiralAnimationSpec(
        cycles = cycles,
        durationMillis = durationMillis,
        inverted = inverted,
        radiusOffset = { progress, angle ->
            val r = a * exp(b * angle)
            r * (1 - progress) // spiral inward
        }
    )
}


