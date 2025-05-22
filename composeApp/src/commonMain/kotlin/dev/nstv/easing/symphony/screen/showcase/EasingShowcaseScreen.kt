package dev.nstv.easing.symphony.screen.showcase

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.nstv.easing.symphony.animationspec.easing.getEasingMapWithNames
import dev.nstv.easing.symphony.design.Grid
import dev.nstv.easing.symphony.design.TileColor
import dev.nstv.easing.symphony.design.components.Ball
import kotlinx.coroutines.delay
import kotlin.math.min

private const val ShowEasingGraph = true
private const val ShowEasingPath = true
private const val ShowExampleBall = true
private const val DurationMillis = 1500

@Composable
fun EasingShowcaseScreen(
    modifier: Modifier = Modifier,
) {

    val easingMap = getEasingMapWithNames()

    val t = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            if (t.value == 1f) {
                t.animateTo(
                    0f,
                    tween(durationMillis = DurationMillis, easing = LinearEasing)
                )
            } else {
                t.animateTo(
                    1f,
                    tween(durationMillis = DurationMillis, easing = LinearEasing)
                )
            }
            delay(DurationMillis.toLong())
        }
    }

    LazyVerticalGrid(GridCells.Fixed(4), modifier = modifier) {
        easingMap.forEach { (name, easing) ->
            item {
                Column(Modifier.padding(horizontal = Grid.One, vertical = Grid.Two)) {
                    Text(
                        modifier = Modifier.fillMaxWidth().padding(Grid.One),
                        text = name,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                    )
                    EasingCell(
                        easing = easing,
                        t = t.value,
                    )
                }
            }
        }
        item {
            Spacer(modifier.fillMaxWidth().height(Grid.Ten))
        }
    }
}


@Composable
fun EasingCell(
    easing: Easing,
    t: Float,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current

    var theaterSize by remember { mutableStateOf(0.dp) }
    var theaterSizePixels by remember { mutableStateOf(0f) }

    val ballSize by animateDpAsState(theaterSize / 5)

    val x = remember { Animatable(0f) }
    val y = remember { Animatable(0f) }

    LaunchedEffect(t) {
        x.snapTo(t * theaterSizePixels)
        y.snapTo(easing.transform(t) * theaterSizePixels)
    }

    Row(modifier.fillMaxWidth().onGloballyPositioned {
        if (!ShowEasingGraph) {
            with(density) {
                theaterSizePixels =
                    min(it.size.width, it.size.height) - ballSize.toPx()
                theaterSize = theaterSizePixels.toDp()
            }
        }
    }) {
        if (ShowEasingGraph) {
            Column(Modifier.weight(4f)) { // Slider + ball
                Box(
                    modifier =
                        Modifier.fillMaxWidth()
                            .aspectRatio(1f)
                            .border(
                                width = 1.dp,
                                shape = RoundedCornerShape(Grid.Half),
                                color = TileColor.LightGray
                            )
                            .padding(Grid.Half)
                            .onGloballyPositioned {
                                with(density) {
                                    theaterSizePixels =
                                        min(it.size.width, it.size.height) - ballSize.toPx()
                                    theaterSize = theaterSizePixels.toDp()
                                }
                            }
                ) {
                    if (ShowEasingPath) {
                        Canvas(modifier = Modifier.fillMaxSize().padding(ballSize / 2)) {
                            val steps = 1000
                            val path = Path().apply {
                                moveTo(0f, size.width)
                            }
                            for (step in 0..steps) {
                                val time = (step / steps.toFloat())
                                path.lineTo(
                                    time * size.width,
                                    size.width - easing.transform(time) * size.width
                                )
                            }
                            drawPath(
                                path = path,
                                color = TileColor.DarkGray,
                                style = Stroke(width = 3.dp.toPx())
                            )
                        }
                    }

                    Ball(
                        size = ballSize,
                        color = TileColor.Blue.copy(alpha = 0.8f),
                        modifier = Modifier
                            .graphicsLayer {
                                this.translationY = -y.value
                                this.translationX = x.value
                            }
                            .align(Alignment.BottomStart)
                    )
                }
            }
        }
        if (ShowExampleBall) {
            Box(
                Modifier.weight(1f)
                    .height(theaterSize + ballSize + Grid.One)
//                    .padding(start = Grid.One)
                    .border(
                        width = 1.dp,
                        shape = RoundedCornerShape(Grid.Half),
                        color = TileColor.LightGray
                    )
                    .padding(Grid.Half)

            ) { // Animated ball
                Ball(
                    color = TileColor.Pink,
                    size = ballSize,
                    modifier = Modifier.align(Alignment.BottomCenter)
                        .graphicsLayer {
                            translationY = -y.value
                        }
                )
            }
        }
    }
}