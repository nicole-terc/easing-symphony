package dev.nstv.easing.symphony.animationspec.easing

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import kotlin.math.PI
import kotlin.math.floor
import kotlin.math.log10

enum class CustomEasingType {
    Stepper,
    Spiral,
    Log,
    Linear;

    fun getEasing(): Easing {
        return when (this) {
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