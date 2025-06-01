package dev.nstv.easing.symphony.screen.components

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.nstv.easing.symphony.animationspec.easing.CustomCubicBezier
import dev.nstv.easing.symphony.animationspec.easing.EaseInBounce
import dev.nstv.easing.symphony.animationspec.easing.EaseInElastic
import dev.nstv.easing.symphony.animationspec.easing.EaseOutBounce
import dev.nstv.easing.symphony.animationspec.easing.MagneticEasing
import dev.nstv.easing.symphony.animationspec.easing.SineWaveEasing
import dev.nstv.easing.symphony.animationspec.easing.SquaredEasing
import dev.nstv.easing.symphony.animationspec.easing.StepperEasing
import dev.nstv.easing.symphony.design.Grid
import dev.nstv.easing.symphony.design.TileColor
import dev.nstv.easing.symphony.screen.SheepIt


private fun getDefaultEasings() = listOf(
    LinearEasing,
    FastOutSlowInEasing,
    FastOutLinearInEasing,
    LinearOutSlowInEasing,
    EaseOutBounce,
    EaseInBounce,
    CustomCubicBezier,
    SineWaveEasing(),
    SquaredEasing,
    StepperEasing,
    MagneticEasing(),
    EaseInElastic,
)

@Composable
fun PhasedMultipleAmplitudeBalls(
    modifier: Modifier = Modifier,
    numberOfBalls: Int = 5,
    amplitude: Float,
    ballColors: Pair<Color, Color> = Pair(TileColor.Pink, TileColor.Pink),
    ballSizes: Pair<Dp, Dp> = Pair(Grid.Five, Grid.Five),
    easings: List<Easing> = getDefaultEasings(),
    fading: Boolean = false,
    changeColor: Boolean = false,
    changeSize: Boolean = false,
    changeAlpha: Boolean = false,
    changeByAmplitude: Boolean = false,
    changeByEasing: Boolean = false,
    sheepIt: Boolean = false,
) {
    Row(
        modifier = modifier
            .fillMaxSize(),
        horizontalArrangement = if (SheepIt) spacedBy(0.dp) else spacedBy(Grid.Half)
    ) {
        easings.forEach { easingEntry ->
            Column(Modifier.weight(1f)) {
                AmplitudeBallPhased(
                    numberOfBalls = numberOfBalls,
                    amplitude = amplitude,
                    ballType = if (fading) AmplitudeBallType.Restart else AmplitudeBallType.Bounce,
                    easing = easingEntry,
                    changeColor = changeColor,
                    changeSize = changeSize,
                    changeAlpha = changeAlpha,
                    showBorder = false,
                    ballColors = ballColors,
                    ballSizes = ballSizes,
                    changeByAmplitude = changeByAmplitude,
                    changeByEasing = changeByEasing,
                    sheepIt = sheepIt,
                )
            }
        }
    }
}