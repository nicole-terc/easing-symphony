package dev.nstv.easing.symphony.screen.music

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nstv.easing.symphony.design.Grid
import dev.nstv.easing.symphony.design.TileColor
import dev.nstv.easing.symphony.design.components.Ball
import dev.nstv.easing.symphony.design.components.DropDownWithArrows
import dev.nstv.easing.symphony.musicvisualizer.reader.MusicReaderWrapper
import dev.nstv.easing.symphony.musicvisualizer.reader.musicPlayerControl
import dev.nstv.easing.symphony.screen.components.AmplitudeBallContainer
import dev.nstv.easing.symphony.screen.components.AmplitudeBallType
import dev.nstv.easing.symphony.screen.musicFilePath
import easingsymphony.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun AmplitudeVisualizersScreen(
    modifier: Modifier = Modifier,
) {
    var ballType by remember { mutableStateOf(AmplitudeBallType.Sine) }

    Column(modifier = modifier.padding(Grid.One)) {
        MusicReaderWrapper(
            fileUri = Res.getUri(musicFilePath),
        ) { musicReader ->
            val amplitude by musicReader.amplitude.collectAsStateWithLifecycle(0f)
            val accumulatedAmplitude = remember { mutableStateListOf<Float>() }

            LaunchedEffect(amplitude) {
                accumulatedAmplitude.add(amplitude)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .musicPlayerControl(musicReader)
            ) {
                DropDownWithArrows(
                    modifier = Modifier.fillMaxWidth(),
                    options = AmplitudeBallType.entries.map { it.name },
                    onSelectionChanged = { index ->
                        ballType = AmplitudeBallType.entries[index]
                    },
                    loopSelection = true,
                )
                AmplitudeBallContainer(
                    amplitude = amplitude,
                    modifier = Modifier
                        .border(width = 1.dp, color = TileColor.LightGray),
                    amplitudeBallType = ballType,
                )
            }
        }
    }
}

