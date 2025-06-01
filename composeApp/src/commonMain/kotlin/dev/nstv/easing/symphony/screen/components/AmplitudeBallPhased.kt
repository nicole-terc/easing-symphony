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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.nstv.easing.symphony.design.Grid
import dev.nstv.easing.symphony.design.TileColor
import dev.nstv.easing.symphony.musicvisualizer.reader.MusicReader.Companion.FRAME_DELAY_MILLIS
import dev.nstv.easing.symphony.util.getAlphaForPercentage
import dev.nstv.easing.symphony.util.getColorForPercentage
import dev.nstv.easing.symphony.util.getSizeForPercentage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.math.ceil
import kotlin.math.min

private const val DEFAULT_ALPHA = 0.6f

@Composable
fun AmplitudeBallPhased(
    modifier: Modifier = Modifier,
    amplitude: Float,
    ballType: AmplitudeBallType,
    easing: Easing = LinearEasing,
    numberOfBalls: Int = 5,
    showOnlyOneBall: Boolean = false,
    changeColor: Boolean = false,
    changeAlpha: Boolean = false,
    changeSize: Boolean = false,
    showBorder: Boolean = false,
    changeByAmplitude: Boolean = false,
    changeByEasing: Boolean = false,
    sheepIt: Boolean = false,
    ballColors: Pair<Color, Color> = Pair(TileColor.Blue, TileColor.Blue),
    ballSizes: Pair<Dp, Dp> = Pair(Grid.Three, Grid.Ten),
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

    LaunchedEffect(amplitude) {
        counter++
        savedAmplitude[counter % numberOfBalls] = amplitude
    }

    LaunchedEffect(reset) {
        reset.collect {
            if (it) {
                savedAmplitude.fill(0f)
            }
        }
    }
    val actualModifier = if (showBorder) {
        modifier.border(1.dp, TileColor.Blue)
    } else {
        modifier
    }

    fun getPercentage(itemAmplitude: Float, index: Int) = when {
        changeByEasing -> 1f - easing.transform(itemAmplitude)
        changeByAmplitude -> 1f - itemAmplitude
        showOnlyOneBall -> 1f
        else -> 1f - index.toFloat() / numberOfBalls
    }

    Box(actualModifier) {
        if (showOnlyOneBall) {
            val index = min(ceil(numberOfBalls / 2f).toInt(), numberOfBalls - 1)
            val itemAmplitude = savedAmplitude[index]
            val percentage = getPercentage(itemAmplitude, index)
            AmplitudeBallContainer(
                amplitude = itemAmplitude,
                easing = easing,
                amplitudeBallType = ballType,
                ballSize = if (changeSize) getSizeForPercentage(
                    percentage = percentage,
                    minSize = ballSizes.first,
                    maxSize = ballSizes.second,
                ) else ballSizes.second,
                ballColor = if (changeColor) getColorForPercentage(
                    percentage = percentage,
                    minColor = ballColors.first,
                    maxColor = ballColors.second
                ).copy(
                    alpha = if (changeAlpha) getAlphaForPercentage(
                        percentage = percentage
                    ) else DEFAULT_ALPHA
                ) else ballColors.first.copy(DEFAULT_ALPHA),
                durationInMillis = animationDuration,
                offsetAnimationSpec = offsetAnimationSpec,
                sheepIt = sheepIt,
            )
        } else {
            savedAmplitude.forEachIndexed { index, itemAmplitude ->
                val percentage = getPercentage(itemAmplitude, index)
                AmplitudeBallContainer(
                    amplitude = itemAmplitude,
                    easing = easing,
                    amplitudeBallType = ballType,
                    ballSize = if (changeSize) getSizeForPercentage(
                        percentage = percentage,
                        minSize = ballSizes.first,
                        maxSize = ballSizes.second,
                    ) else ballSizes.second,
                    ballColor = if (changeColor) getColorForPercentage(
                        percentage = percentage,
                        minColor = ballColors.first,
                        maxColor = ballColors.second
                    ).copy(
                        alpha = if (changeAlpha) getAlphaForPercentage(
                            percentage = percentage
                        ) else DEFAULT_ALPHA
                    ) else ballColors.first.copy(DEFAULT_ALPHA),
                    durationInMillis = animationDuration,
                    offsetAnimationSpec = offsetAnimationSpec,
                    sheepIt = sheepIt,
                )
            }
        }
    }
}