package dev.nstv.easing.symphony.screen.music

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import dev.nstv.easing.symphony.musicvisualizer.reader.MusicReaderWrapper
import dev.nstv.easing.symphony.musicvisualizer.reader.musicPlayerControl
import dev.nstv.easing.symphony.screen.components.PhasedMultipleAmplitudeBalls
import dev.nstv.easing.symphony.screen.musicFilePath
import easingsymphony.composeapp.generated.resources.Res
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import org.jetbrains.compose.resources.ExperimentalResourceApi

private const val UseDummyFlow = false
private const val DummyDelay = 750L
private const val ShowOnlyOneBall = true

private val dummyAmplitudeFlow = flow {
    while (true) {
        emit(0f)
        delay(DummyDelay * 2)
        emit(0.45f)
        delay(DummyDelay * 2)
        emit(0.85f)
        delay(DummyDelay * 3)
    }
}
private const val fading = true

@OptIn(ExperimentalResourceApi::class)
@Composable
fun TheMusicVisualizerPartThree(
    modifier: Modifier = Modifier,
    _numberOfBalls: Int = 5,
) {
    val numberOfBalls = if (UseDummyFlow) 1 else _numberOfBalls
    Column(modifier = modifier.padding(Grid.One)) {
        MusicReaderWrapper(
            fileUri = Res.getUri(musicFilePath),
            playOnLoad = false,
        ) { musicReader ->

            val easings = listOf(
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

            val amplitudeFlow = if (UseDummyFlow) dummyAmplitudeFlow else musicReader.amplitude

            val amplitude by amplitudeFlow.collectAsStateWithLifecycle(0f)

            Box(modifier = Modifier.fillMaxSize().musicPlayerControl(musicReader)) {
//                AmplitudeBallPhased(
//                    modifier = Modifier.align(Alignment.Center).fillMaxHeight(),
//                    amplitude = amplitude,
//                    ballType = AmplitudeBallType.Tween,
//                    easing = FastOutSlowInEasing,
//                    showBorder = false,
//                    ballSizes = Grid.Ten to Grid.Ten,
//                )

//                PhasedMultipleAmplitudeBalls(
//                    modifier = Modifier.graphicsLayer {
//                        rotationZ = 180f
//                        rotationY = 180f
//                    },
//                    amplitude = amplitude,
//                    numberOfBalls = numberOfBalls,
//                    easings = easings,
//                    ballColors = TileColor.Purple to TileColor.Purple,
//                    fading = false,
//                    ballSizes = Grid.Four to Grid.Four,
//                )

                PhasedMultipleAmplitudeBalls(
                    amplitude = amplitude,
                    numberOfBalls = numberOfBalls,
                    easings = easings,
                    fading = false,
                )
            }
        }
    }
}