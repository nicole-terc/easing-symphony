package dev.nstv.easing.symphony.animationspec.easing

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import kotlin.math.PI
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.sin

enum class CustomEasingType {
    Magnetic,
    Sine,
    InBounce,
    Bounce,
    Stepper,
    Spiral,
    Log,
    Linear;

    fun getEasing(): Easing {
        return when (this) {
            Magnetic -> MagneticEasing()
            Sine -> SineWaveEasing()
            InBounce -> EaseInBounce
            Bounce -> EaseOutBounce
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
/*function easeOutBounce(x: number): number {
    const n1 = 7.5625;
    const d1 = 2.75;

    if (x < 1 / d1) {
        return n1 * x * x;
    } else if (x < 2 / d1) {
        return n1 * (x -= 1.5 / d1) * x + 0.75;
    } else if (x < 2.5 / d1) {
        return n1 * (x -= 2.25 / d1) * x + 0.9375;
    } else {
        return n1 * (x -= 2.625 / d1) * x + 0.984375;
    }
}
*/
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