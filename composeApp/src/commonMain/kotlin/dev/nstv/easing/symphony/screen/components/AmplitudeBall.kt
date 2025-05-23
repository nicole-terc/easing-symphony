package dev.nstv.easing.symphony.screen.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import dev.nstv.easing.symphony.animationspec.easing.CustomCubicBezier
import dev.nstv.easing.symphony.animationspec.easing.EaseInBounce
import dev.nstv.easing.symphony.animationspec.easing.EaseOutBounce
import dev.nstv.easing.symphony.animationspec.sineWaveSpec
import dev.nstv.easing.symphony.design.Grid
import dev.nstv.easing.symphony.design.TileColor
import dev.nstv.easing.symphony.design.components.Ball
import dev.nstv.easing.symphony.musicvisualizer.reader.MusicReader.Companion.FRAME_DELAY_MILLIS
import kotlinx.coroutines.launch

enum class AmplitudeBallType {
    Simple,
    Animated,
    Tween,
    Tween_LinearEasing,
    Tween_FastOutSlowInEasing,
    Tween_FastOutLinearInEasing,
    Tween_LinearOutSlowInEasing,
    Spring,
    Keyframes,
    KeyframesWithBounce,
    Sine,
    Sine_Bigger,
    Bounce,
    Restart;

    fun supportsAnimationSpec() = this in setOf(
        Tween,
        Bounce,
        Restart,
    )
}

const val AMPLITUDE_ANIMATION_DURATION = FRAME_DELAY_MILLIS.toInt()

//@Composable
//fun AmplitudeAnimation(
//    musicReader: MusicReader,
//) {
//    val amplitude by musicReader.amplitude.collectAsState()
//    var screenHeight by remember { mutableStateOf(0) }
//
//    val translationY by animateFloatAsState(
//        targetValue = -amplitude * screenHeight,
//        animationSpec = tween()
//    )
//
//    Box(
//        Modifier.fillMaxSize()
//            .onGloballyPositioned {
//                screenHeight = it.size.height
//            }
//    ) {
//        Box(
//            Modifier
//                .background(color = Color.Blue, shape = CircleShape)
//                .graphicsLayer {
//                    this.translationY = translationY
//                }
//        )
//    }
//}


@Composable
fun AmplitudeBallContainer(
    amplitude: Float,
    modifier: Modifier = Modifier,
    amplitudeBallType: AmplitudeBallType = AmplitudeBallType.Simple,
    easing: Easing = LinearEasing,
    ballSize: Dp = Grid.Ten,
    ballColor: Color = TileColor.Blue,
    durationInMillis: Int = AMPLITUDE_ANIMATION_DURATION,
    offsetAnimationSpec: (durationInMillis: Int) -> AnimationSpec<Offset> = { duration ->
        tween(
            durationMillis = duration,
            easing = easing
        )
    },
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

            AmplitudeBallType.Tween -> TweenBall(
                easing = easing,
                offsetAnimationSpec = offsetAnimationSpec,
                amplitude = amplitude,
                screenHeight = screenHeight,
                size = ballSize,
                ballColor = ballColor,
                durationInMillis = durationInMillis,
            )

            AmplitudeBallType.Tween_LinearEasing -> TweenBall(
                amplitude = amplitude,
                screenHeight = screenHeight,
                easing = LinearEasing,
                size = ballSize,
                ballColor = ballColor,
                durationInMillis = durationInMillis,
            )

            AmplitudeBallType.Tween_FastOutSlowInEasing -> TweenBall(
                amplitude = amplitude,
                screenHeight = screenHeight,
                easing = FastOutSlowInEasing,
                size = ballSize,
                ballColor = ballColor,
                durationInMillis = durationInMillis,
            )

            AmplitudeBallType.Tween_FastOutLinearInEasing -> TweenBall(
                amplitude = amplitude,
                screenHeight = screenHeight,
                easing = FastOutLinearInEasing,
                size = ballSize,
                ballColor = ballColor,
                durationInMillis = durationInMillis,
            )

            AmplitudeBallType.Tween_LinearOutSlowInEasing -> TweenBall(
                amplitude = amplitude,
                screenHeight = screenHeight,
                easing = LinearOutSlowInEasing,
                size = ballSize,
                ballColor = ballColor,
                durationInMillis = durationInMillis,
            )

            AmplitudeBallType.Spring -> SpringBall(
                amplitude = amplitude,
                screenHeight = screenHeight,
                size = ballSize,
                ballColor = ballColor,
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
                offsetAnimationSpec = offsetAnimationSpec,
                easing = easing,
                amplitude = amplitude,
                screenHeight = screenHeight,
                size = ballSize,
                ballColor = ballColor,
                durationInMillis = durationInMillis,
            )

            AmplitudeBallType.Restart -> BounceBallInvisibleBack(
                offsetAnimationSpec = offsetAnimationSpec,
                easing = easing,
                amplitude = amplitude,
                screenHeight = screenHeight,
                size = ballSize,
                ballColor = ballColor,
                durationInMillis = durationInMillis,
            )

            AmplitudeBallType.Sine -> SineBall(
                amplitude = amplitude,
                screenHeight = screenHeight,
                size = ballSize,
                ballColor = ballColor,
                durationInMillis = durationInMillis,
            )

            AmplitudeBallType.Sine_Bigger -> SineBall(
                amplitude = amplitude,
                screenHeight = screenHeight,
                size = ballSize,
                ballColor = ballColor,
                durationInMillis = durationInMillis,
                sineAmplitude = 40f,
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
fun BoxScope.TweenBall(
    amplitude: Float,
    screenHeight: Int,
    easing: Easing = FastOutSlowInEasing,
    size: Dp = Grid.Ten,
    ballColor: Color = TileColor.Blue,
    durationInMillis: Int = AMPLITUDE_ANIMATION_DURATION,
    modifier: Modifier = Modifier,
    offsetAnimationSpec: (durationInMillis: Int) -> AnimationSpec<Offset> = { duration ->
        tween(
            durationMillis = duration,
            easing = easing
        )
    },
) {
    val targetOffset = Offset(0f, -amplitude * screenHeight)

    val translation by animateOffsetAsState(
        targetValue = targetOffset,
        animationSpec = offsetAnimationSpec(durationInMillis),
    )

    Ball(
        size = size,
        color = ballColor,
        modifier = modifier
            .align(Alignment.BottomCenter)
            .graphicsLayer {
                this.translationY = translation.y
                this.translationX = translation.x
            },
    )
}

@Composable
fun BoxScope.SpringBall(
    amplitude: Float,
    screenHeight: Int,
    size: Dp = Grid.Ten,
    ballColor: Color = TileColor.Blue,
    modifier: Modifier = Modifier,
) {
    val translationY by animateFloatAsState(
        targetValue = -amplitude * screenHeight,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        )
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
fun BoxScope.BounceBallInvisibleBack(
    amplitude: Float,
    screenHeight: Int,
    easing: Easing = EaseOutBounce,
    size: Dp = Grid.Ten,
    ballColor: Color = TileColor.Blue,
    durationInMillis: Int = AMPLITUDE_ANIMATION_DURATION,
    modifier: Modifier = Modifier,
    offsetAnimationSpec: (durationInMillis: Int) -> AnimationSpec<Offset> = { duration ->
        tween(
            durationMillis = duration,
            easing = easing
        )
    },
) {
    val alpha = remember { Animatable(1f) }
    val targetOffset = Offset(0f, -amplitude * screenHeight)
    val translation = remember { Animatable(Offset.Zero, Offset.VectorConverter) }

    LaunchedEffect(amplitude) {
        translation.snapTo(Offset.Zero)
        alpha.snapTo(1f)
        launch {
            translation.animateTo(targetOffset, offsetAnimationSpec(durationInMillis))
        }
        launch {
            alpha.animateTo(0.1f, tween(durationInMillis, easing = CustomCubicBezier))
        }
    }

    Ball(
        size = size,
        color = ballColor,
        modifier = modifier
            .align(Alignment.BottomCenter)
            .graphicsLayer {
                this.translationY = translation.value.y
                this.translationX = translation.value.x
                this.alpha = alpha.value
            },
    )
}

@Composable
fun BoxScope.BounceBall(
    amplitude: Float,
    screenHeight: Int,
    easing: Easing = EaseOutBounce,
    size: Dp = Grid.Ten,
    ballColor: Color = TileColor.Blue,
    durationInMillis: Int = AMPLITUDE_ANIMATION_DURATION,
    modifier: Modifier = Modifier,
    offsetAnimationSpec: (durationInMillis: Int) -> AnimationSpec<Offset> = { duration ->
        tween(
            durationMillis = duration,
            easing = easing
        )
    },
) {

    val targetOffset = Offset(0f, -amplitude * screenHeight)
    val translation = remember { Animatable(Offset.Zero, Offset.VectorConverter) }

    val goingDownTime = remember { durationInMillis / 5 }
    val goingUpTime = remember { durationInMillis - goingDownTime }

    LaunchedEffect(amplitude) {
        translation.animateTo(Offset.Zero, tween(goingDownTime, easing = LinearEasing))
        translation.animateTo(targetOffset, offsetAnimationSpec(goingUpTime))
    }

    Ball(
        size = size,
        color = ballColor,
        modifier = modifier
            .align(Alignment.BottomCenter)
            .graphicsLayer {
                this.translationY = translation.value.y
                this.translationX = translation.value.x
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
    sineAmplitude: Float = 20f,
    waveCount: Int = 3
) {
    val targetOffset = Offset(0f, -amplitude * screenHeight)
    val translation by animateOffsetAsState(
        targetValue = targetOffset,
        animationSpec = sineWaveSpec(
            durationMillis = durationInMillis,
            waveCount = waveCount,
            amplitude = sineAmplitude,
        )
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