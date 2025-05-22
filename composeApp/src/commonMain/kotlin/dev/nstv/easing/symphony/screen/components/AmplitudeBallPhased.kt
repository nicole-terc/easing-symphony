package dev.nstv.easing.symphony.screen.components

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import dev.nstv.easing.symphony.design.TileColor
import dev.nstv.easing.symphony.musicvisualizer.reader.MusicReader.Companion.FRAME_DELAY_MILLIS
import dev.nstv.easing.symphony.util.getAlphaForPercentage
import dev.nstv.easing.symphony.util.getColorForPercentage
import dev.nstv.easing.symphony.util.getSizeForPercentage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.math.ceil
import kotlin.math.min

@Composable
fun AmplitudeBallPhased(
    amplitude: Float,
    ballType: AmplitudeBallType,
    easing: Easing = LinearEasing,
    numberOfBalls: Int = 5,
    showOnlyOneBall: Boolean = false,
    reset: Flow<Boolean> = flowOf(),
    offsetAnimationSpec: (durationInMillis: Int) -> AnimationSpec<Offset> = { duration ->
        tween(
            durationMillis = duration,
            easing = easing
        )
    }
) {
    val savedAmplitude by remember { mutableStateOf(FloatArray(numberOfBalls)) }
    val animationDuration: Int = numberOfBalls * FRAME_DELAY_MILLIS.toInt()

    var counter by remember { mutableStateOf(0) }

    LaunchedEffect(reset) {
        reset.collect {
            if (it) {
                savedAmplitude.fill(0f)
            }
        }
    }

    LaunchedEffect(amplitude) {
        counter++
        savedAmplitude[counter % numberOfBalls] = amplitude
    }

    Box(Modifier.border(width = 1.dp, color = TileColor.LightGray)) {
        if (showOnlyOneBall) {
            val index = min(ceil(numberOfBalls / 2f).toInt(), numberOfBalls - 1)
            val itemAmplitude = savedAmplitude[index]
            val percentage = 1f
            AmplitudeBallContainer(
                amplitude = itemAmplitude,
                easing = easing,
                amplitudeBallType = ballType,
                ballSize = getSizeForPercentage(percentage),
                ballColor = getColorForPercentage(percentage).copy(
                    alpha = getAlphaForPercentage(
                        percentage
                    )
                ),
                durationInMillis = animationDuration,
                offsetAnimationSpec = offsetAnimationSpec,
            )
        } else {
            savedAmplitude.forEachIndexed { index, itemAmplitude ->
                val percentage = 1f - index.toFloat() / numberOfBalls
                AmplitudeBallContainer(
                    amplitude = itemAmplitude,
                    easing = easing,
                    amplitudeBallType = ballType,
                    ballSize = getSizeForPercentage(percentage),
                    ballColor = getColorForPercentage(percentage).copy(
                        alpha = getAlphaForPercentage(
                            percentage
                        )
                    ),
                    durationInMillis = animationDuration,
                    offsetAnimationSpec = offsetAnimationSpec,
                )
            }
        }
    }
}