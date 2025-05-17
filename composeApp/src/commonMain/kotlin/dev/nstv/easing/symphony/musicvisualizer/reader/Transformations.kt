package dev.nstv.easing.symphony.musicvisualizer.reader

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.ln1p
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

fun FloatArray.fft(): FloatArray {
    val n = this.size
    val real = this.map { it.toDouble() }.toDoubleArray()
    val imag = DoubleArray(n)

    var j = 0
    for (i in 1 until n) {
        var bit = n shr 1
        while (j >= bit) {
            j -= bit
            bit = bit shr 1
        }
        j += bit
        if (i < j) {
            val temp = real[i]
            real[i] = real[j]
            real[j] = temp
        }
    }

    var len = 2
    while (len <= n) {
        val angle = -2.0 * PI / len
        val wlenCos = cos(angle)
        val wlenSin = sin(angle)
        for (i in 0 until n step len) {
            var wr = 1.0
            var wi = 0.0
            for (j2 in 0 until len / 2) {
                val uRe = real[i + j2]
                val uIm = imag[i + j2]
                val tRe = wr * real[i + j2 + len / 2] - wi * imag[i + j2 + len / 2]
                val tIm = wr * imag[i + j2 + len / 2] + wi * real[i + j2 + len / 2]
                real[i + j2] = uRe + tRe
                imag[i + j2] = uIm + tIm
                real[i + j2 + len / 2] = uRe - tRe
                imag[i + j2 + len / 2] = uIm - tIm
                val nextWr = wr * wlenCos - wi * wlenSin
                wi = wr * wlenSin + wi * wlenCos
                wr = nextWr
            }
        }
        len = len shl 1
    }

    return FloatArray(n / 2) { i -> sqrt(real[i].pow(2) + imag[i].pow(2)).toFloat() }
}

fun FloatArray.fftNormalized(): FloatArray {
    val magnitudes = this.fft()
    val max = magnitudes.maxOrNull()?.takeIf { it > 0f } ?: 1f
    return magnitudes.map { it / max }.toFloatArray()
}

fun FloatArray.fftNormalizedLog(gain: Float = 1f): FloatArray {
    val magnitudes = this.fft()
    val max = magnitudes.maxOrNull()?.takeIf { it > 0f } ?: 1f
    return magnitudes.map {
        val linear = it / max
        (ln1p(linear * gain) / ln1p(gain)).coerceIn(0f, 1f)
    }.toFloatArray()
}

fun FloatArray.fftInPlace(output: FloatArray = FloatArray(this.size / 2)): FloatArray {
    val n = this.size
    val real = this.copyOf().map { it.toDouble() }.toDoubleArray()
    val imag = DoubleArray(n)

    var j = 0
    for (i in 1 until n) {
        var bit = n shr 1
        while (j >= bit) {
            j -= bit
            bit = bit shr 1
        }
        j += bit
        if (i < j) {
            val temp = real[i]
            real[i] = real[j]
            real[j] = temp
        }
    }

    var len = 2
    while (len <= n) {
        val angle = -2.0 * PI / len
        val wlenCos = cos(angle)
        val wlenSin = sin(angle)
        for (i in 0 until n step len) {
            var wr = 1.0
            var wi = 0.0
            for (j2 in 0 until len / 2) {
                val uRe = real[i + j2]
                val uIm = imag[i + j2]
                val tRe = wr * real[i + j2 + len / 2] - wi * imag[i + j2 + len / 2]
                val tIm = wr * imag[i + j2 + len / 2] + wi * real[i + j2 + len / 2]
                real[i + j2] = uRe + tRe
                imag[i + j2] = uIm + tIm
                real[i + j2 + len / 2] = uRe - tRe
                imag[i + j2 + len / 2] = uIm - tIm
                val nextWr = wr * wlenCos - wi * wlenSin
                wi = wr * wlenSin + wi * wlenCos
                wr = nextWr
            }
        }
        len = len shl 1
    }

    // Write magnitudes into provided buffer
    for (i in 0 until output.size) {
        output[i] = sqrt(real[i].pow(2) + imag[i].pow(2)).toFloat()
    }

    return output
}

fun FloatArray.fftNormalizedInPlace(output: FloatArray = FloatArray(this.size / 2)): FloatArray {
    val result = this.fftInPlace(output)
    val max = result.maxOrNull()?.takeIf { it > 0f } ?: 1f
    for (i in result.indices) result[i] /= max
    return result
}

fun FloatArray.fftNormalizedLogInPlace(
    output: FloatArray = FloatArray(this.size / 2),
    gain: Float = 15f
): FloatArray {
    val result = this.fftInPlace(output)
    val max = result.maxOrNull()?.takeIf { it > 0f } ?: 1f
    val logNormalizer = ln1p(gain)

    for (i in result.indices) {
        val normalized = result[i] / max
        result[i] = (ln1p(normalized * gain) / logNormalizer).coerceIn(0f, 1f)
    }

    return result
}

fun FloatArray.fftNormalizedLog10InPlace(
    output: FloatArray = FloatArray(this.size / 2),
    gain: Float = 15f,
    power: Float = 0.8f
): FloatArray {
    val result = this.fftInPlace(output)
    val max = result.maxOrNull()?.takeIf { it > 0f } ?: 1f
    val logNormalizer = log10(1 + gain)

    for (i in result.indices) {
        val normalized = (result[i] / max).coerceIn(0f, 1f)
        val logScaled = log10(1 + normalized * gain) / logNormalizer
        result[i] = logScaled.pow(power).coerceIn(0f, 1f)
    }

    return result
}


