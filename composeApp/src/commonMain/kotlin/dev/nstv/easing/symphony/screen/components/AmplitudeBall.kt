package dev.nstv.easing.symphony.screen.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import dev.nstv.easing.symphony.animationspec.easing.CustomEasingType.InBounce
import dev.nstv.easing.symphony.animationspec.easing.EaseInBounce
import dev.nstv.easing.symphony.animationspec.easing.EaseOutBounce
import dev.nstv.easing.symphony.animationspec.easing.EaseOutBounceAdjusted
import dev.nstv.easing.symphony.animationspec.easing.LogEasing
import dev.nstv.easing.symphony.animationspec.sineWaveSpec
import dev.nstv.easing.symphony.design.Grid
import dev.nstv.easing.symphony.design.TileColor
import dev.nstv.easing.symphony.design.components.Ball
import dev.nstv.easing.symphony.musicvisualizer.reader.MusicReader
import dev.nstv.easing.symphony.musicvisualizer.reader.MusicReader.Companion.FRAME_DELAY_MILLIS
import kotlinx.coroutines.launch

enum class AmplitudeBallType {
    Simple,
    Animated,
    Keyframes,
    KeyframesWithBounce,
    Sine,
    Bounce,
}

const val AMPLITUDE_ANIMATION_DURATION = FRAME_DELAY_MILLIS.toInt()

@Composable
fun AmplitudeAnimation(
    musicReader: MusicReader,
) {
    val amplitude by musicReader.amplitude.collectAsState()
    var screenHeight by remember { mutableStateOf(0) }

    Box(
        Modifier.fillMaxSize()
            .onGloballyPositioned {
                screenHeight = it.size.height
            }
    ) {
        Box(
            Modifier
                .background(color = Color.Blue, shape = CircleShape)
                .graphicsLayer {
                    translationY = -amplitude * screenHeight
                }
        )
    }
}

@Composable
fun AmplitudeBallContainer(
    amplitude: Float,
    modifier: Modifier = Modifier,
    amplitudeBallType: AmplitudeBallType = AmplitudeBallType.Simple,
    ballSize: Dp = Grid.Ten,
    ballColor: Color = TileColor.Blue,
    durationInMillis: Int = AMPLITUDE_ANIMATION_DURATION,
) {
    var screenHeight by remember { mutableStateOf(0) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color.Transparent)
            .onGloballyPositioned {
                screenHeight = it.size.height
            }
    ) {
        when (amplitudeBallType) {
            AmplitudeBallType.Simple -> SimpleBall(
                amplitude = amplitude,
                screenHeight = screenHeight,
                size = ballSize,
                ballColor = ballColor,
            )

            AmplitudeBallType.Animated -> AnimatedBall(
                amplitude = amplitude,
                screenHeight = screenHeight,
                size = ballSize,
                ballColor = ballColor,
            )

            AmplitudeBallType.Sine -> SineBall(
                amplitude = amplitude,
                screenHeight = screenHeight,
                size = ballSize,
                ballColor = ballColor,
                durationInMillis = durationInMillis,
            )

            AmplitudeBallType.Keyframes -> KeyframesBall(
                amplitude = amplitude,
                screenHeight = screenHeight,
                size = ballSize,
                ballColor = ballColor,
                durationInMillis = durationInMillis,
            )

            AmplitudeBallType.KeyframesWithBounce -> KeyframesWithBounceBall(
                amplitude = amplitude,
                screenHeight = screenHeight,
                size = ballSize,
                ballColor = ballColor,
                durationInMillis = durationInMillis,
            )

            AmplitudeBallType.Bounce -> BounceBall(
                amplitude = amplitude,
                screenHeight = screenHeight,
                size = ballSize,
                ballColor = ballColor,
                durationInMillis = durationInMillis,
            )
        }

    }
}

@Composable
fun BoxScope.SimpleBall(
    amplitude: Float,
    screenHeight: Int,
    size: Dp = Grid.Ten,
    ballColor: Color = TileColor.Blue,
    modifier: Modifier = Modifier,
) {
    Ball(
        size = size,
        color = ballColor,
        modifier = modifier
            .align(Alignment.BottomCenter)
            .graphicsLayer {
                translationY = -amplitude * screenHeight
            },
    )
}

@Composable
fun BoxScope.AnimatedBall(
    amplitude: Float,
    screenHeight: Int,
    size: Dp = Grid.Ten,
    ballColor: Color = TileColor.Blue,
    modifier: Modifier = Modifier,
) {
    val translationY by animateFloatAsState(-amplitude * screenHeight)

    Ball(
        size = size,
        color = ballColor,
        modifier = modifier
            .align(Alignment.BottomCenter)
            .graphicsLayer {
                this.translationY = translationY
            },
    )
}

@Composable
fun BoxScope.KeyframesBall(
    amplitude: Float,
    screenHeight: Int,
    size: Dp = Grid.Ten,
    ballColor: Color = TileColor.Blue,
    durationInMillis: Int = AMPLITUDE_ANIMATION_DURATION,
    modifier: Modifier = Modifier,
) {
    val newTranslationY = -amplitude * screenHeight
    val translationY by animateFloatAsState(
        newTranslationY,
        animationSpec = keyframes {
            durationMillis = durationInMillis
            0f atFraction .2f using LinearEasing
        }
    )

    Ball(
        size = size,
        color = ballColor,
        modifier = modifier
            .align(Alignment.BottomCenter)
            .graphicsLayer {
                this.translationY = translationY
            },
    )
}

@Composable
fun BoxScope.KeyframesWithBounceBall(
    amplitude: Float,
    screenHeight: Int,
    size: Dp = Grid.Ten,
    ballColor: Color = TileColor.Blue,
    durationInMillis: Int = AMPLITUDE_ANIMATION_DURATION,
    modifier: Modifier = Modifier,
) {
    val newTranslationY = -amplitude * screenHeight
    val translationY by animateFloatAsState(
        newTranslationY,
        animationSpec =
//            tween(durationMillis = durationInMillis, easing = EaseOutBounce)
            keyframes {
                durationMillis = durationInMillis
                0f atFraction .2f using LinearEasing
                // Bouncy easings don't work :(
                // newTranslationY atFraction .9f using EaseOutBounceAdjusted
                newTranslationY atFraction 1f using EaseInBounce

            }
    )

    Ball(
        size = size,
        color = ballColor,
        modifier = modifier
            .align(Alignment.BottomCenter)
            .graphicsLayer {
                this.translationY = translationY
            },
    )
}

@Composable
fun BoxScope.BounceBall(
    amplitude: Float,
    screenHeight: Int,
    size: Dp = Grid.Ten,
    ballColor: Color = TileColor.Blue,
    durationInMillis: Int = AMPLITUDE_ANIMATION_DURATION,
    modifier: Modifier = Modifier,
) {
    val newTranslationY = -amplitude * screenHeight
    val translationY = remember { Animatable(0f) }

    val goingDownTime = remember { durationInMillis / 5 }
    val goingUpTime = remember { durationInMillis - goingDownTime }

    LaunchedEffect(amplitude) {
        translationY.animateTo(0f, tween(goingDownTime, easing = LinearEasing))
        translationY.animateTo(newTranslationY, tween(goingUpTime, easing = EaseOutBounce))
    }

    Ball(
        size = size,
        color = ballColor,
        modifier = modifier
            .align(Alignment.BottomCenter)
            .graphicsLayer {
                this.translationY = translationY.value
            },
    )
}

@Composable
fun BoxScope.SineBall(
    amplitude: Float,
    screenHeight: Int,
    size: Dp = Grid.Ten,
    ballColor: Color = TileColor.Blue,
    durationInMillis: Int = AMPLITUDE_ANIMATION_DURATION,
    modifier: Modifier = Modifier,
) {
    val targetOffset = Offset(0f, -amplitude * screenHeight)
    val translation by animateOffsetAsState(
        targetOffset,
        animationSpec = sineWaveSpec(durationMillis = durationInMillis)
    )

    Ball(
        size = size,
        color = ballColor,
        modifier = modifier
            .align(Alignment.BottomCenter)
            .graphicsLayer {
                this.translationX = translation.x
                this.translationY = translation.y
            },
    )
}