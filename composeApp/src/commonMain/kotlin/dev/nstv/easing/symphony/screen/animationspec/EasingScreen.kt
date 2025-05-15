package dev.nstv.easing.symphony.screen.animationspec

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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

@Composable
fun EasingScreen(
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    val options = CustomEasingType.getEasingMap()
    var selected by remember { mutableStateOf(options.entries.first().value) }

    val ballSize = Grid.Five
    val theaterHeight = 400.dp
    val theaterHeightPixels = with(density) { theaterHeight.toPx() - ballSize.toPx() }
    var theaterWidthPixels = 0f

    val t = remember { Animatable(0f) }
    val x = remember { Animatable(0f) }
    val y = remember { Animatable(0f) }

    LaunchedEffect(t.value) {
        x.snapTo(t.value * theaterWidthPixels)
        y.snapTo(selected.transform(t.value) * theaterHeightPixels)
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
                        t.animateTo(0f)
                    } else {
                        t.animateTo(1f)
                    }
                }
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
}