package dev.nstv.easing.symphony.animationspec.easing

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import kotlin.math.PI
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin

fun getEasingMapWithNames() = mapOf(
    "Linear" to LinearEasing,
    "FastOutSlowIn" to FastOutSlowInEasing,
    "FastOutLinearIn" to FastOutLinearInEasing,
    "LinearOutSlowIn" to LinearOutSlowInEasing,
    "EaseOutBounce" to EaseOutBounce,
    "EaseInBounce" to EaseInBounce,
    "CubicBezier" to CustomCubicBezier,
    "Sine" to SineWaveEasing(),
    "EaseInQuad" to SquaredEasing,
    "Stepper" to StepperEasing,
    "Magnetic" to MagneticEasing(),
    "EaseInElastic" to EaseInElastic,
)


enum class CustomEasingType {
    Squared,
    Magnetic,
    Sine,
    InBounce,
    Bounce,
    Elastic,
    InElastic,
    CubicBezier,
    Stepper,
    Spiral,
    Log,
    Linear;

    fun getEasing(): Easing {
        return when (this) {
            Squared -> SquaredEasing
            Magnetic -> MagneticEasing()
            Sine -> SineWaveEasing()
            InBounce -> EaseInBounce
            Bounce -> EaseOutBounce
            Elastic -> EaseOutElastic
            InElastic -> EaseInElastic
            CubicBezier -> CustomCubicBezier
            Stepper -> StepperEasing
            Spiral -> SpiralEasing
            Log -> LogEasing
            Linear -> LinearEasing
        }
    }

    companion object {
        fun getEasingList(): List<Easing> = entries.map { it.getEasing() }

        fun getEasingMap(): Map<String, Easing> = entries.associate { it.name to it.getEasing() }
    }
}

// Option 1: Play with CubicBezierEasing
val CustomCubicBezier = CubicBezierEasing(0.68f, -0.55f, 0.27f, 1.55f)

// Option 2: Create a custom Easing function
val SquaredEasing = Easing { x -> x * x }

fun MagneticEasing(threshold: Float = 0.2f): Easing = Easing { x ->
    when {
        x < threshold -> 0f
        else -> (x - threshold) / (1f - threshold)
    }
}

fun SineWaveEasing(
    waveCount: Int = 3,
    amplitude: Float = 0.1f
): Easing = Easing { fraction ->
    fraction + amplitude * sin(fraction * waveCount * 2f * PI).toFloat()
}

// Keyframes ignores the rest of the easing curve once it reaches the final value
// Didn't work :(
val EaseOutBounceAdjusted: Easing = Easing { fraction ->
    val n1 = 7.5625f
    val d1 = 2.75f

    val raw = when {
        fraction < 1f / d1 -> {
            n1 * fraction * fraction
        }

        fraction < 2f / d1 -> {
            val x2 = fraction - 1.5f / d1
            n1 * x2 * x2 + 0.75f
        }

        fraction < 2.5f / d1 -> {
            val x2 = fraction - 2.25f / d1
            n1 * x2 * x2 + 0.9375f
        }

        else -> {
            val x2 = fraction - 2.625f / d1
            n1 * x2 * x2 + 0.984375f
        }
    }
    if (raw == 1f && fraction != 1f) {
        0.9f
    } else {
        raw
    }
}

val EaseInBounce: Easing = Easing { fraction ->
    1f - EaseOutBounce.transform(1f - fraction)
}


// source: https://easings.net/#easeOutBounce
val EaseOutBounce: Easing = Easing { fraction ->
    val n1 = 7.5625f
    val d1 = 2.75f

    when {
        fraction < 1f / d1 -> {
            n1 * fraction * fraction
        }

        fraction < 2f / d1 -> {
            val x2 = fraction - 1.5f / d1
            n1 * x2 * x2 + 0.75f
        }

        fraction < 2.5f / d1 -> {
            val x2 = fraction - 2.25f / d1
            n1 * x2 * x2 + 0.9375f
        }

        else -> {
            val x2 = fraction - 2.625f / d1
            n1 * x2 * x2 + 0.984375f
        }
    }
}

// source: https://easings.net/#easeOutBounce
val EaseOutElastic: Easing = Easing { x ->
    val c4 = (2 * PI) / 3

    when {
        x == 0f -> 0f
        x == 1f -> 1f
        else -> {
            val pow = 2.0.pow(-10 * x.toDouble())
            val sin = sin((x * 10 - 0.75) * c4)
            (pow * sin + 1).toFloat()
        }
    }
}

// source: https://easings.net/#easeOutBounce
val EaseInElastic: Easing = Easing { x ->
    val c4 = (2 * PI) / 3

    when {
        x == 0f -> 0f
        x == 1f -> 1f
        else -> {
            val pow = 2.0.pow(10.0 * x - 10)
            val sin = sin((x * 10 - 10.75) * c4)
            (-pow * sin).toFloat()
        }
    }
}

val LogEasing = Easing { t ->
    // Prevent log(0), shift input to start at 0.01
    val adjustedT = t.coerceIn(0.01f, 1f)
    val base = 10f
    val logStart = log10(base * 0.01f)
    val logEnd = log10(base * 1f)
    val logRange = logEnd - logStart

    // Normalize result between 0 and 1
    (log10(base * adjustedT) - logStart) / logRange
}

val SpiralEasing = Easing { t ->
    val adjustedT = t.coerceIn(0.01f, 1f)

    // Logarithmic-like start
    val base = 10f
    val logStart = kotlin.math.log10(base * 0.01f)
    val logEnd = kotlin.math.log10(base * 1f)
    val logComponent = (kotlin.math.log10(base * adjustedT) - logStart) / (logEnd - logStart)

    // Spiral overshoot: damped sine wave
    val overshootAmplitude = 0.1f  // how far it overshoots
    val frequency = 6f             // number of oscillations
    val damping = (1f - t)         // fade the oscillation
    val spiralComponent =
        overshootAmplitude * kotlin.math.sin(frequency * t * PI).toFloat() * damping

    (logComponent + spiralComponent).coerceIn(0f, 1.2f)
}

fun stepperEasing(steps: Int = 5): Easing = Easing { fraction ->
    floor(fraction * steps) / steps
}

val StepperEasing = Easing { fraction ->
    floor(fraction * 5) / 5
}