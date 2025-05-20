package dev.nstv.easing.symphony.screen.animationspec

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import dev.nstv.easing.symphony.animationspec.easing.CustomEasingType
import dev.nstv.easing.symphony.design.Grid
import dev.nstv.easing.symphony.design.TileColor
import dev.nstv.easing.symphony.design.components.Ball
import dev.nstv.easing.symphony.design.components.DropDownWithArrows
import kotlinx.coroutines.launch
import kotlin.math.min

const val ShowEasingGraph = true
const val ShowExampleBall = true
const val DurationMillis = 1500

@Composable
fun EasingScreen(
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    val options = CustomEasingType.getEasingMap()
    var selected by remember { mutableStateOf(options.entries.first().value) }

    val ballSize = Grid.Five
    var theaterSize by remember { mutableStateOf(0.dp) }
    var theaterSizePixels by remember { mutableStateOf(0f) }

    val t = remember { Animatable(0f) }
    val x = remember { Animatable(0f) }
    val y = remember { Animatable(0f) }

    LaunchedEffect(t.value) {
        x.snapTo(t.value * theaterSizePixels)
        y.snapTo(selected.transform(t.value) * theaterSizePixels)
    }

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Grid.Three, Alignment.Top),
    ) {
        DropDownWithArrows(
            options = options.keys.toList(),
            onSelectionChanged = { selected = options.entries.elementAt(it).value },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Button(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            onClick = {
                coroutineScope.launch {
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
                }
            }
        ) {
            Text(text = "Animate")
        }

        Row(Modifier.fillMaxWidth()) {
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
                    if (ShowEasingGraph) {
                        Canvas(modifier = Modifier.fillMaxSize().padding(ballSize / 2)) {
                            val steps = 1000
                            val path = Path().apply {
                                moveTo(0f, size.width)
                            }
                            for (step in 0..steps) {
                                val time = (step / steps.toFloat())
                                path.lineTo(
                                    time * size.width,
                                    size.width - selected.transform(time) * size.width
                                )
                            }
                            drawPath(
                                path = path,
                                color = TileColor.LightGray,
                                style = Stroke(width = 2.dp.toPx())
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        modifier = Modifier.width(Grid.Three),
                        text = "t:",
                    )
                    Slider(
                        modifier = Modifier.padding(end = Grid.Three),
                        value = t.value,
                        onValueChange = {
                            coroutineScope.launch {
                                t.snapTo(it)
                            }
                        },
                    )
                }
            }
            if (ShowExampleBall) {
                Box(
                    Modifier.weight(1f)
                        .height(theaterSize + ballSize + Grid.One)
                        .padding(start = Grid.One)
                        .border(
                            width = 1.dp,
                            shape = RoundedCornerShape(Grid.Half),
                            color = TileColor.LightGray
                        )
                        .padding(Grid.Half)

                ) { // Animated ball
                    Ball(
                        color = TileColor.Pink,
                        size = Grid.Five,
                        modifier = Modifier.align(Alignment.BottomCenter)
                            .graphicsLayer {
                                translationY = -y.value
                            }
                    )
                }
            }
        }
    }
}