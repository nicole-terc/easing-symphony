package dev.nstv.easing.symphony.screen

import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import dev.nstv.easing.symphony.animationspec.CustomOffsetAnimationSpec
import dev.nstv.easing.symphony.design.Grid
import dev.nstv.easing.symphony.design.TileColor
import dev.nstv.easing.symphony.design.components.Ball
import dev.nstv.easing.symphony.design.components.DropDownWithArrows
import dev.nstv.easing.symphony.screen.components.DrawAnimationSpecPath

@Composable
fun OffsetAnimationSpecScreen(
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current

    val options = CustomOffsetAnimationSpec.getAnimationSpecMap()
    var selected by remember { mutableStateOf(options.entries.first().value) }

    val ballSize = Grid.Five
    val ballSizePx = with(density) { ballSize.toPx() }
    val ballSizePxHalf = ballSizePx / 2f
    val theaterHeight = 400.dp
    val theaterHeightPixels = with(density) { theaterHeight.toPx() - ballSize.toPx() }
    var theaterWidthPixels by remember { mutableStateOf(0f) }

    var trigger by remember { mutableStateOf(false) }

    val offset by animateOffsetAsState(
        targetValue = if (trigger) Offset(
            theaterWidthPixels,
            theaterHeightPixels
        ) else Offset.Zero,
        animationSpec = selected
    )

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
                trigger = !trigger
            }
        ) {
            Text(text = "Animate")
        }

        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .height(theaterHeight + Grid.One)
                    .border(
                        width = 1.dp,
                        shape = RoundedCornerShape(Grid.Half),
                        color = TileColor.LightGray
                    )
                    .padding(Grid.Half)
                    .graphicsLayer {
                        clip = false
                    }
                    .onGloballyPositioned {
                        with(density) {
                            theaterWidthPixels = it.size.width - ballSize.toPx()
                        }
                    }
        ) {
            Ball(
                size = ballSize,
                modifier = Modifier
                    .graphicsLayer {
                        this.translationY = -offset.y
                        this.translationX = offset.x
                    }
                    .align(Alignment.BottomStart)
            )

            DrawAnimationSpecPath(
                spec = selected,
                from = Offset(ballSizePxHalf, -ballSizePxHalf),
                to = Offset(
                    theaterWidthPixels + ballSizePxHalf,
                    -(theaterHeightPixels + ballSizePxHalf)
                ),
                steps = 100,
                color = TileColor.LightGray,
                modifier = Modifier.fillMaxSize().background(Color.Red.copy(alpha = 0.5f)).graphicsLayer{
                    clip = false
                }
            )
        }
    }
}