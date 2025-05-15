package dev.nstv.easing.symphony.audio

import kotlin.math.PI
import kotlin.math.cos
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