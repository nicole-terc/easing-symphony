package dev.nstv.easing.symphony.screen.music

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nstv.easing.symphony.animationspec.CustomOffsetAnimationSpec
import dev.nstv.easing.symphony.animationspec.easing.getEasingMapWithNames
import dev.nstv.easing.symphony.design.Grid
import dev.nstv.easing.symphony.design.TileColor
import dev.nstv.easing.symphony.design.components.DropDownWithArrows
import dev.nstv.easing.symphony.musicvisualizer.reader.MusicReader.Companion.FRAME_DELAY_MILLIS
import dev.nstv.easing.symphony.musicvisualizer.reader.MusicReaderWrapper
import dev.nstv.easing.symphony.musicvisualizer.reader.musicPlayerControl
import dev.nstv.easing.symphony.screen.components.AmplitudeBallContainer
import dev.nstv.easing.symphony.screen.components.AmplitudeBallPhased
import dev.nstv.easing.symphony.screen.components.AmplitudeBallType
import dev.nstv.easing.symphony.screen.musicFilePath
import dev.nstv.easing.symphony.util.getAlphaForPercentage
import dev.nstv.easing.symphony.util.getColorForPercentage
import dev.nstv.easing.symphony.util.getSizeForPercentage
import easingsymphony.composeapp.generated.resources.Res
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlin.math.ceil
import kotlin.math.min


@OptIn(ExperimentalResourceApi::class)
@Composable
fun TheMusicVisualizer(
    modifier: Modifier = Modifier,
    numberOfBalls: Int = 5,
    showOnlyOneBall: Boolean = false,
) {
    val coroutineScope = rememberCoroutineScope()
    val resetFlow = MutableSharedFlow<Boolean>()
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

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .musicPlayerControl(musicReader) {
                        coroutineScope.launch {
                            resetFlow.emit(true)
                        }
                    }
            ) {
                DropDownWithArrows(
                    modifier = Modifier.fillMaxWidth(),
                    options = AmplitudeBallType.entries.map { it.name },
                    selectedIndex = AmplitudeBallType.entries.indexOf(ballType),
                    onSelectionChanged = { index ->
                        ballType = AmplitudeBallType.entries[index]
                    },
                    loopSelection = true,
                )
                AnimatedVisibility(
                    visible = ballType.supportsAnimationSpec()
                ) {
                    DropDownWithArrows(
                        modifier = Modifier.fillMaxWidth(),
                        options = animationSpecEntries.map { it.name },
                        selectedIndex = selectedAnimationSpecIndex,
                        onSelectionChanged = { index ->
                            selectedAnimationSpecIndex = index
                        },
                        loopSelection = true,
                    )
                }
                AmplitudeBallPhased(
                    reset = resetFlow,
                    amplitude = amplitude,
                    ballType = ballType,
                    numberOfBalls = numberOfBalls,
                    showOnlyOneBall = showOnlyOneBall,
                    offsetAnimationSpec = { duration ->
                        animationSpecEntries[selectedAnimationSpecIndex].toAnimationSpec(duration)
                    }
                )

            }
        }
    }
}
