package dev.nstv.easing.symphony.screen.music

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nstv.easing.symphony.animationspec.CustomOffsetAnimationSpec
import dev.nstv.easing.symphony.design.Grid
import dev.nstv.easing.symphony.design.components.Ball
import dev.nstv.easing.symphony.design.components.DropDownWithArrows
import dev.nstv.easing.symphony.musicvisualizer.reader.MusicReader.Companion.FRAME_DELAY_MILLIS
import dev.nstv.easing.symphony.musicvisualizer.reader.MusicReaderWrapper
import dev.nstv.easing.symphony.musicvisualizer.reader.musicPlayerControl
import dev.nstv.easing.symphony.screen.components.AmplitudeBallPhased
import dev.nstv.easing.symphony.screen.components.AmplitudeBallType
import dev.nstv.easing.symphony.screen.musicFilePath
import easingsymphony.composeapp.generated.resources.Res
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi


@OptIn(ExperimentalResourceApi::class)
@Composable
fun TheMusicVisualizerPartOne(
    modifier: Modifier = Modifier,
//    numberOfBalls: Int = 5,
//    showOnlyOneBall: Boolean = false,
) {
    val coroutineScope = rememberCoroutineScope()
    val resetFlow = MutableSharedFlow<Boolean>()
    var screenHeight by remember { mutableStateOf(0) }

    var ballType by remember { mutableStateOf(AmplitudeBallType.Sine) }

    // Easing
//    val easingMap = getEasingMapWithNames()
//    var selectedEasingIndex by remember { mutableStateOf(0) }

    // AnimationSpec
    val animationSpecEntries = CustomOffsetAnimationSpec.entries
    var selectedAnimationSpecIndex by remember { mutableStateOf(0) }

    Column(modifier = modifier.padding(Grid.One)) {
        MusicReaderWrapper(
            fileUri = Res.getUri(musicFilePath),
            playOnLoad = false,
        ) { musicReader ->
            val amplitude by musicReader.amplitude.collectAsStateWithLifecycle(0f)

            val numberOfFrames = 5
            val savedAmplitude by remember { mutableStateOf(FloatArray(numberOfFrames)) }
            val animationDuration: Int = numberOfFrames * FRAME_DELAY_MILLIS.toInt()

            var counter by remember { mutableStateOf(0) }
            LaunchedEffect(amplitude) {
                savedAmplitude[counter % numberOfFrames] = amplitude
                counter++
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .onGloballyPositioned {
                        screenHeight = it.size.height
                    }
                    .musicPlayerControl(musicReader) {
                        coroutineScope.launch {
                            resetFlow.emit(true)
                        }
                    }
            ) {
                savedAmplitude.forEach { itemAmplitude ->
                    val translationY by animateFloatAsState(
                        targetValue = -itemAmplitude * screenHeight,
                        animationSpec = keyframes {
                            durationMillis = animationDuration
                            0f atFraction .2f using LinearEasing
                        }
                    )

                    Box(
                        modifier = modifier
                            .fillMaxSize()
                            .padding(Grid.Two)
                            .background(color = Color.Transparent)
                    ) {

                        Ball(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .graphicsLayer {
                                    this.translationY = translationY
                                }
                        )
                    }
                }
            }
        }
    }
}

