package dev.nstv.easing.symphony.screen.music

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Label
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nstv.easing.symphony.animationspec.CustomOffsetAnimationSpec
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
import dev.nstv.easing.symphony.design.components.CheckBoxLabel
import dev.nstv.easing.symphony.musicvisualizer.reader.MusicReaderWrapper
import dev.nstv.easing.symphony.musicvisualizer.reader.musicPlayerControl
import dev.nstv.easing.symphony.screen.HideOptions
import dev.nstv.easing.symphony.screen.components.AmplitudeBallPhased
import dev.nstv.easing.symphony.screen.components.AmplitudeBallType
import dev.nstv.easing.symphony.screen.components.PhasedMultipleAmplitudeBalls
import dev.nstv.easing.symphony.screen.musicFilePath
import easingsymphony.composeapp.generated.resources.Res
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
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

@OptIn(ExperimentalResourceApi::class)
@Composable
fun TheMusicVisualizerPartFour(
    modifier: Modifier = Modifier,
    _numberOfBalls: Int = 8,
) {
    val numberOfBalls = if (UseDummyFlow) 1 else _numberOfBalls

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

    val animationSpecsBigBall = listOf(
        { durationMillis: Int -> CustomOffsetAnimationSpec.SpiralLog.toAnimationSpec(durationMillis) },
        { durationMillis: Int ->
            CustomOffsetAnimationSpec.CartesianSine.toAnimationSpec(
                durationMillis
            )
        },
        { durationMillis: Int -> CustomOffsetAnimationSpec.Spring.toAnimationSpec(durationMillis) },
    )

    Column(modifier = modifier.fillMaxHeight()) {

        var showOptions by remember { mutableStateOf(false) }
        var changeColor by remember { mutableStateOf(true) }
        var changeSize by remember { mutableStateOf(true) }
        var changeAlpha by remember { mutableStateOf(false) }
        var fading by remember { mutableStateOf(true) }
        var changeByAmplitude by remember { mutableStateOf(true) }
        val icon = if (showOptions) "^" else "v"
        if (!HideOptions) {
            Text(
                text = icon + "Options",
                modifier = Modifier.clickable { showOptions = !showOptions })
            AnimatedVisibility(showOptions) {
                Column {
                    CheckBoxLabel(
                        text = "Change Color",
                        checked = changeColor,
                        onCheckedChange = { changeColor = it }
                    )

                    CheckBoxLabel(
                        text = "Change Size",
                        checked = changeSize,
                        onCheckedChange = { changeSize = it }
                    )

                    CheckBoxLabel(
                        text = "Change Alpha",
                        checked = changeAlpha,
                        onCheckedChange = { changeAlpha = it }
                    )

                    CheckBoxLabel(
                        text = "Fading",
                        checked = fading,
                        onCheckedChange = { fading = it }
                    )
                    CheckBoxLabel(
                        text = "Change by Amplitude",
                        checked = changeByAmplitude,
                        onCheckedChange = { changeByAmplitude = it }
                    )
                }
            }
        }

        Column(modifier = Modifier.padding(Grid.One)) {
            MusicReaderWrapper(
                fileUri = Res.getUri(musicFilePath),
                playOnLoad = false,
            ) { musicReader ->
                val amplitudeFlow = if (UseDummyFlow) dummyAmplitudeFlow else musicReader.amplitude

                val amplitude by amplitudeFlow.collectAsStateWithLifecycle(0f)

//                var amplitude by remember { mutableStateOf(0f) }

//                LaunchedEffect(amplitudeFlow) {
//                    amplitudeFlow.take(16).collect {
//                        amplitude = it
//                    }
//                }

                Box(modifier = Modifier.fillMaxSize().musicPlayerControl(musicReader)) {

                    PhasedMultipleAmplitudeBalls(
                        modifier = Modifier.graphicsLayer {
                            rotationZ = 180f
//                            rotationY = 180f
                        },
                        amplitude = amplitude,
                        numberOfBalls = numberOfBalls,
                        easings = easings,
                        ballColors = TileColor.Blue to TileColor.Purple,
                        ballSizes = Grid.Two to Grid.Five,
//                        fading = fading,
                        changeColor = changeColor,
                        changeSize = changeSize,
//                        changeAlpha = changeAlpha,
                        changeByEasing = false,
                    )

                    PhasedMultipleAmplitudeBalls(
                        amplitude = amplitude,
                        numberOfBalls = numberOfBalls,
                        easings = easings,
                        ballColors = TileColor.Orange to TileColor.Pink,
                        ballSizes = Grid.Two to Grid.Five,
//                        fading = fading,
                        changeColor = changeColor,
                        changeSize = changeSize,
//                        changeAlpha = changeAlpha,
                        changeByEasing = false,
                    )

                    // Middle ball
                    AmplitudeBallPhased(
                        modifier = Modifier.align(Alignment.Center).fillMaxHeight(),
                        amplitude = amplitude,
                        ballType = AmplitudeBallType.Sine_Bigger,
                        easing = FastOutSlowInEasing,
                        showBorder = false,
                        ballSizes = Grid.Ten to Grid.Twenty,
                        ballColors = TileColor.Purple to TileColor.Green,
                        changeColor = changeColor,
                        changeSize = changeSize,
//                        changeAlpha = changeAlpha,
                    )

//                    AmplitudeBallPhased(
//                        modifier = Modifier.fillMaxHeight(0.5f).align(Alignment.Center),
//                        amplitude = amplitude,
//                        ballType = AmplitudeBallType.Tween,
//                        easing = FastOutSlowInEasing,
//                        showBorder = false,
//                        ballSizes = Grid.Five to Grid.Ten,
//                        ballColors = TileColor.Yellow to TileColor.Red,
//                        changeColor = changeColor,
//                        changeSize = changeSize,
//                        changeAlpha = changeAlpha,
//                        offsetAnimationSpec = {
//                            CustomOffsetAnimationSpec.SpiralArchimedean.toAnimationSpec(it)
//                        }
//                    )
                }
            }
        }
    }
}
