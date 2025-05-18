package dev.nstv.easing.symphony.util

import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sin

fun generateSineWave(
    durationSeconds: Float,
    sampleRate: Int = 44100,
    frequencyRange: ClosedFloatingPointRange<Float> = 200f..880f,
    amplitudeRange: ClosedFloatingPointRange<Float> = 0.2f..1.0f
): FloatArray {
    val totalSamples = (durationSeconds * sampleRate).toInt()
    val twoPi = 2f * PI.toFloat()

    return FloatArray(totalSamples) { i ->
        val t = i / sampleRate.toFloat()

        val frequency = frequencyRange.start + (frequencyRange.endInclusive - frequencyRange.start) * (t / durationSeconds)
        val amplitude = amplitudeRange.start + (amplitudeRange.endInclusive - amplitudeRange.start) * (t / durationSeconds)

        amplitude * sin(twoPi * frequency * t)
    }
}

fun generateSineWaveExponential(
    durationSeconds: Float,
    sampleRate: Int = 44100,
    frequencyRange: ClosedFloatingPointRange<Float> = 220f..880f,
    amplitudeRange: ClosedFloatingPointRange<Float> = 0.2f..1.0f
): FloatArray {
    val totalSamples = (durationSeconds * sampleRate).toInt()
    val twoPi = 2f * PI.toFloat()
    val freqRatio = frequencyRange.endInclusive / frequencyRange.start
    val ampRatio = amplitudeRange.endInclusive / amplitudeRange.start

    return FloatArray(totalSamples) { i ->
        val t = i / sampleRate.toFloat()
        val normT = t / durationSeconds

        val frequency = frequencyRange.start * freqRatio.pow(normT)
        val amplitude = amplitudeRange.start * ampRatio.pow(normT)

        amplitude * sin(twoPi * frequency * t)
    }
}
