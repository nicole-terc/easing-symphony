package dev.nstv.easing.symphony.screen.music

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nstv.easing.symphony.design.Grid
import dev.nstv.easing.symphony.design.TileColor
import dev.nstv.easing.symphony.musicvisualizer.reader.MusicReaderWrapper
import dev.nstv.easing.symphony.musicvisualizer.reader.musicPlayerControl
import dev.nstv.easing.symphony.screen.components.AmplitudeBallPhased
import dev.nstv.easing.symphony.screen.components.AmplitudeBallType
import dev.nstv.easing.symphony.screen.musicFilePath
import easingsymphony.composeapp.generated.resources.Res
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi

private const val UseDummyFlow = false
private const val DummyDelay = 750L
private const val ShowOnlyOneBall = false

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
fun TheMusicVisualizerPartOne(
    modifier: Modifier = Modifier,
    _numberOfBalls: Int = 5,
) {
    val coroutineScope = rememberCoroutineScope()
    val numberOfBalls = if (UseDummyFlow) 1 else _numberOfBalls
    val resetFlow = MutableSharedFlow<Boolean>()


    Column(modifier = modifier.padding(Grid.One)) {
        MusicReaderWrapper(
            fileUri = Res.getUri(musicFilePath),
            playOnLoad = false,
        ) { musicReader ->
            val ballTypes = mapOf(
                "Spring" to AmplitudeBallType.Spring,
                "Tween" to AmplitudeBallType.Tween,
                "Keyframes" to AmplitudeBallType.Keyframes,
            )

            val amplitudeFlow = if (UseDummyFlow) dummyAmplitudeFlow else musicReader.amplitude
            val amplitude by amplitudeFlow.collectAsStateWithLifecycle(0f)


            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .musicPlayerControl(musicReader) {
                        coroutineScope.launch {
                            resetFlow.emit(true)
                        }
                    },
                horizontalArrangement = spacedBy(Grid.Two)
            ) {
                ballTypes.forEach { ballEntry ->
                    Column(Modifier.weight(1f)) {
                        Text(
                            modifier = Modifier.fillMaxWidth().padding(bottom = Grid.One),
                            text = ballEntry.key,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Box(Modifier.border(width = 1.dp, color = TileColor.LightGray)) {
                            AmplitudeBallPhased(
                                reset = resetFlow,
                                numberOfBalls = numberOfBalls,
                                amplitude = amplitude,
                                ballType = ballEntry.value,
                                changeAlpha = false,
                                changeSize = false,
                                changeColor = false,
                                showBorder = true,
                            )
                        }
                    }
                }
            }
        }
    }
}