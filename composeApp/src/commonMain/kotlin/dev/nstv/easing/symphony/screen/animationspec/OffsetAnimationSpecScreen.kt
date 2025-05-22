package dev.nstv.easing.symphony.screen.animationspec

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.nstv.easing.symphony.animationspec.CustomOffsetAnimationSpec
import dev.nstv.easing.symphony.design.Grid
import dev.nstv.easing.symphony.design.TileColor
import dev.nstv.easing.symphony.design.components.Ball
import dev.nstv.easing.symphony.design.components.DropDownWithArrows
import dev.nstv.easing.symphony.screen.components.DrawAnimationSpecPath
import kotlin.math.min

private const val ShowPath = true
private const val ShowLine = true

@Composable
fun OffsetAnimationSpecScreen(
    modifier: Modifier = Modifier,
) {
    val options = CustomOffsetAnimationSpec.getAnimationSpecMap()
    var selected by remember { mutableStateOf(options.entries.first().value) }
    var trigger by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()).padding(Grid.Two),
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

        AnimationSpecOffsetVisualizer(
            animationSpec = selected,
            modifier = Modifier.fillMaxWidth(),
            trigger = trigger,
        )

    }
}

@Composable
fun AnimationSpecOffsetVisualizer(
    animationSpec: AnimationSpec<Offset>,
    trigger: Boolean, // True = Animate to targetPosition, false = go back to origin
    modifier: Modifier = Modifier,
    ballSize: Dp = Grid.Five,
    animateBack: Boolean = true,
) {
    val density = LocalDensity.current
    val ballSizePx = with(density) { ballSize.toPx() }
    val ballSizePxHalf = ballSizePx / 2f
    var theaterWidth by remember { mutableStateOf(0.dp) }
    var theaterWidthPixels by remember { mutableStateOf(0f) }

    val offset by animateOffsetAsState(
        targetValue = if (trigger) Offset(
            theaterWidthPixels,
            theaterWidthPixels
        ) else Offset.Zero,
        animationSpec = if (animateBack || trigger) animationSpec else tween(easing = LinearEasing),
    )

    Box(
        modifier =
            modifier.fillMaxWidth()
                .aspectRatio(1f)
                .graphicsLayer {
                    clip = false
                    // Lazy way of starting from bottomLeft to topRight
                    rotationZ = -90f
                }
                .padding(Grid.Half)
                .border(
                    width = 1.dp,
                    shape = RoundedCornerShape(Grid.Half),
                    color = TileColor.LightGray
                )
                .padding(Grid.Half)
                .onGloballyPositioned {
                    with(density) {
                        theaterWidthPixels = min(it.size.width, it.size.height) - ballSize.toPx()
                        theaterWidth = theaterWidthPixels.toDp()
                    }
                }
    ) {
        val startDrawOffset = Offset(ballSizePxHalf, ballSizePxHalf)
        val endDrawOffset = Offset(
            theaterWidthPixels + ballSizePxHalf,
            (theaterWidthPixels + ballSizePxHalf)
        )

        DrawAnimationSpecPath(
            spec = animationSpec,
            from = if (animateBack && !trigger) endDrawOffset else startDrawOffset,
            to = if (animateBack && !trigger) startDrawOffset else endDrawOffset,
            steps = 100,
            color = TileColor.Pink,
            showPath = ShowPath,
            showLine = ShowLine,
            modifier = Modifier.fillMaxSize()
                .graphicsLayer {
                    clip = false
                }
        )

        Ball(
            size = ballSize,
            color = TileColor.Blue.copy(0.8f),
            modifier = Modifier
                .graphicsLayer {
                    this.translationY = offset.y
                    this.translationX = offset.x
                }
        )
    }
}