package dev.nstv.easing.symphony.animationspec.easing

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import kotlin.math.PI
import kotlin.math.floor
import kotlin.math.log10

enum class CustomEasingType {
    Bounce,
    Stepper,
    Spiral,
    Log,
    Linear;

    fun getEasing(): Easing {
        return when (this) {
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
val EaseOutBounce: Easing = Easing { x ->
    val n1 = 7.5625f
    val d1 = 2.75f

    when {
        x < 1f / d1 -> {
            n1 * x * x
        }
        x < 2f / d1 -> {
            val x2 = x - 1.5f / d1
            n1 * x2 * x2 + 0.75f
        }
        x < 2.5f / d1 -> {
            val x2 = x - 2.25f / d1
            n1 * x2 * x2 + 0.9375f
        }
        else -> {
            val x2 = x - 2.625f / d1
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