package dev.nstv.easing.symphony.animationspec

import androidx.compose.animation.core.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.util.lerp
import kotlin.math.*
import kotlin.random.Random

enum class CustomAnimationSpecType {
    Recoil,
    Stepper,
    Magnetic,
    Drift,
    Twitch,
    Spline,
    Gravity;

    fun toAnimationSpec(): AnimationSpec<Float> {
        return when (this) {
            Recoil -> RecoilSpec()
            Stepper -> StepperSpec()
            Magnetic -> MagneticSnapSpec()
            Drift -> DriftSpec()
            Twitch -> TwitchSpec()
            Spline -> SplineSpec()
            Gravity -> GravitySpec()
        }
    }

    companion object {
        fun getAnimationSpecMap(): Map<String, AnimationSpec<Float>> =
            entries.associate { it.name to it.toAnimationSpec() }

    }
}

// --- RecoilSpec ---
class RecoilSpec(private val durationMillis: Int = 400) : AnimationSpec<Float> {
    override fun <V : AnimationVector> vectorize(converter: TwoWayConverter<Float, V>): VectorizedAnimationSpec<V> {
        val durationNanos = durationMillis * 1_000_000L
        return object : VectorizedAnimationSpec<V> {
            override val isInfinite: Boolean = false
            override fun getDurationNanos(
                initialValue: V,
                targetValue: V,
                initialVelocity: V
            ): Long = durationNanos

            override fun getValueFromNanos(
                playTimeNanos: Long,
                initialValue: V,
                targetValue: V,
                initialVelocity: V
            ): V {
                val t = playTimeNanos / durationNanos.toFloat()
                val f = when {
                    t < 0.2f -> 2.5f * t
                    t < 0.4f -> 1.0f - 1.2f * (t - 0.2f)
                    else -> 1.0f + 0.3f * (1f - t)
                }.coerceIn(0f, 2f)
                val v = lerp(
                    converter.convertFromVector(initialValue),
                    converter.convertFromVector(targetValue),
                    f
                )
                return converter.convertToVector(v)
            }

            override fun getVelocityFromNanos(
                playTimeNanos: Long,
                initialValue: V,
                targetValue: V,
                initialVelocity: V
            ): V = converter.convertToVector(0f)
        }
    }
}

// --- StepperSpec ---
class StepperSpec(
    private val durationMillis: Int = 1000,
    private val steps: Int = 5
) : AnimationSpec<Float> {
    override fun <V : AnimationVector> vectorize(converter: TwoWayConverter<Float, V>) =
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
                val t = (playTimeNanos / 1_000_000f) / durationMillis
                val stepped = floor(t * steps) / steps
                val v = lerp(
                    converter.convertFromVector(initialValue),
                    converter.convertFromVector(targetValue),
                    stepped
                )
                return converter.convertToVector(v)
            }

            override fun getVelocityFromNanos(
                playTimeNanos: Long,
                initialValue: V,
                targetValue: V,
                initialVelocity: V
            ): V = converter.convertToVector(0f)
        }
}

// --- MagneticSnapSpec ---
class MagneticSnapSpec(
    private val durationMillis: Int = 1000,
    private val threshold: Float = 0.2f
) : AnimationSpec<Float> {
    val durationNanos = durationMillis * 1_000_000L
    override fun <V : AnimationVector> vectorize(converter: TwoWayConverter<Float, V>) =
        object : VectorizedAnimationSpec<V> {
            override val isInfinite: Boolean = false
            override fun getDurationNanos(
                initialValue: V,
                targetValue: V,
                initialVelocity: V
            ): Long = durationNanos

            override fun getValueFromNanos(
                playTimeNanos: Long,
                initialValue: V,
                targetValue: V,
                initialVelocity: V
            ): V {
                val t = (playTimeNanos / 1_000_000f) / durationMillis
                val effectiveT = if (t < threshold) 0f else (t - threshold) / (1f - threshold)
                val v = lerp(
                    converter.convertFromVector(initialValue),
                    converter.convertFromVector(targetValue),
                    effectiveT
                )
                return converter.convertToVector(v)
            }

            override fun getVelocityFromNanos(
                playTimeNanos: Long,
                initialValue: V,
                targetValue: V,
                initialVelocity: V
            ): V = converter.convertToVector(0f)
        }
}

// --- TwitchSpec ---

class TwitchSpec(
    private val durationMillis: Int = 1000,
    private val magnitude: Float = 5f
) : AnimationSpec<Float> {
    val durationNanos = durationMillis * 1_000_000L

    override fun <V : AnimationVector> vectorize(converter: TwoWayConverter<Float, V>) =
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
                val t = playTimeNanos / durationNanos.toFloat()
                val base = lerp(
                    converter.convertFromVector(initialValue),
                    converter.convertFromVector(targetValue),
                    t
                )
                val jitter = (Random.nextFloat() - 0.5f) * magnitude
                return converter.convertToVector(base + jitter)
            }

            override fun getVelocityFromNanos(
                playTimeNanos: Long,
                initialValue: V,
                targetValue: V,
                initialVelocity: V
            ): V = converter.convertToVector(0f)
        }
}

// --- SplineSpec ---
class SplineSpec(private val durationMillis: Int = 1000) : AnimationSpec<Float> {
    override fun <V : AnimationVector> vectorize(converter: TwoWayConverter<Float, V>) =
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
                val curved = t * t * (3 - 2 * t) // Smoothstep
                val v = lerp(
                    converter.convertFromVector(initialValue),
                    converter.convertFromVector(targetValue),
                    curved
                )
                return converter.convertToVector(v)
            }

            override fun getVelocityFromNanos(
                playTimeNanos: Long,
                initialValue: V,
                targetValue: V,
                initialVelocity: V
            ): V = converter.convertToVector(0f)
        }
}

// --- GravitySpec ---
class GravitySpec(private val durationMillis: Int = 1000) : AnimationSpec<Float> {
    override fun <V : AnimationVector> vectorize(converter: TwoWayConverter<Float, V>) =
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
                val v = lerp(
                    converter.convertFromVector(initialValue),
                    converter.convertFromVector(targetValue),
                    t * t
                )
                return converter.convertToVector(v)
            }

            override fun getVelocityFromNanos(
                playTimeNanos: Long,
                initialValue: V,
                targetValue: V,
                initialVelocity: V
            ): V = converter.convertToVector(0f)
        }
}

// --- DriftSpec ---
class DriftSpec(private val durationMillis: Int = 1000) : AnimationSpec<Float> {
    override fun <V : AnimationVector> vectorize(converter: TwoWayConverter<Float, V>) =
        object : VectorizedAnimationSpec<V> {
            val durationNanos = durationMillis * 1_000_000L
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
                val t = playTimeNanos / 1_000_000f / durationMillis * 2f
                val driftT = t * 0.5f + sin(t * PI).toFloat() * 0.5f
                val v = lerp(
                    converter.convertFromVector(initialValue),
                    converter.convertFromVector(targetValue),
                    driftT
                )
                return converter.convertToVector(v)
            }

            override fun getVelocityFromNanos(
                playTimeNanos: Long,
                initialValue: V,
                targetValue: V,
                initialVelocity: V
            ): V {
                val t = playTimeNanos / 1_000_000f / durationMillis
                val v = 1 - sin(t * PI).toFloat()
                return converter.convertToVector(v)
            }
        }
}
