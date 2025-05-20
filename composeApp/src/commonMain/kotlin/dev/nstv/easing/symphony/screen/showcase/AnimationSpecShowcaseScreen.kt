package dev.nstv.easing.symphony.screen.showcase

import androidx.compose.foundation.border
import androidx.compose.foundation.content.MediaType.Companion.Text
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nstv.easing.symphony.design.Grid
import dev.nstv.easing.symphony.design.TileColor
import dev.nstv.easing.symphony.design.components.DropDownWithArrows
import dev.nstv.easing.symphony.musicvisualizer.reader.MusicReader.Companion.FRAME_DELAY_MILLIS
import dev.nstv.easing.symphony.musicvisualizer.reader.MusicReaderWrapper
import dev.nstv.easing.symphony.musicvisualizer.reader.musicPlayerControl
import dev.nstv.easing.symphony.screen.components.AmplitudeBallContainer
import dev.nstv.easing.symphony.screen.components.AmplitudeBallType
import dev.nstv.easing.symphony.screen.music.ShowOnlyOneBall
import dev.nstv.easing.symphony.screen.musicFilePath
import dev.nstv.easing.symphony.util.getAlphaForPercentage
import dev.nstv.easing.symphony.util.getColorForPercentage
import dev.nstv.easing.symphony.util.getSizeForPercentage
import easingsymphony.composeapp.generated.resources.Res
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlin.math.ceil
import kotlin.math.min

private const val UseDummyFlow = true
private const val DummyDelay = 750L

val dummyAmplitudeFlow = flow {
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
fun AnimationSpecShowcaseScreen(
    modifier: Modifier = Modifier,
    numberOfBalls: Int = 1,
) {
    Column(modifier = modifier.padding(Grid.One)) {
        MusicReaderWrapper(
            fileUri = Res.getUri(musicFilePath),
            playOnLoad = false,
        ) { musicReader ->
            val ballTypes = listOf(
                AmplitudeBallType.Spring,
                AmplitudeBallType.Tween,
                AmplitudeBallType.Keyframes,
            )

            val amplitudeFlow = if (UseDummyFlow) dummyAmplitudeFlow else musicReader.amplitude

            val amplitude by amplitudeFlow.collectAsStateWithLifecycle(0f)

            val savedAmplitude by remember { mutableStateOf(FloatArray(numberOfBalls)) }
            val animationDuration: Int =
                if (UseDummyFlow) DummyDelay.toInt() else numberOfBalls * FRAME_DELAY_MILLIS.toInt()

            var counter by remember { mutableStateOf(0) }

            LaunchedEffect(amplitude) {
                counter++
                savedAmplitude[counter % numberOfBalls] = amplitude
            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .musicPlayerControl(musicReader) {
                        savedAmplitude.fill(0f)
                    },
                horizontalArrangement = spacedBy(Grid.Two)
            ) {
                ballTypes.forEach { ballType ->
                    Column(Modifier.weight(1f)) {
                        Text(
                            modifier = Modifier.fillMaxWidth().padding(bottom = Grid.One),
                            text = ballType.name,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Box(Modifier.border(width = 1.dp, color = TileColor.LightGray)) {
                            if (ShowOnlyOneBall) {
                                val index = min(ceil(numberOfBalls / 2f).toInt(), numberOfBalls - 1)
                                val itemAmplitude = savedAmplitude[index]
                                val percentage = 1f

                                AmplitudeBallContainer(
                                    amplitude = itemAmplitude,
                                    amplitudeBallType = ballType,
                                    ballSize = getSizeForPercentage(percentage),
                                    ballColor = getColorForPercentage(percentage).copy(
                                        alpha = getAlphaForPercentage(
                                            percentage
                                        )
                                    ),
                                    durationInMillis = animationDuration
                                )
                            } else {
                                savedAmplitude.forEachIndexed { index, itemAmplitude ->
                                    val percentage = 1f - index.toFloat() / numberOfBalls
                                    AmplitudeBallContainer(
                                        amplitude = itemAmplitude,
                                        amplitudeBallType = ballType,
                                        ballSize = getSizeForPercentage(percentage),
                                        ballColor = getColorForPercentage(percentage).copy(
                                            alpha = getAlphaForPercentage(
                                                percentage
                                            )
                                        ),
                                        durationInMillis = animationDuration
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}