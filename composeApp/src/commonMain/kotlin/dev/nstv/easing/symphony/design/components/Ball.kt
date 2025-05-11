package dev.nstv.easing.symphony.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import dev.nstv.easing.symphony.design.Grid
import dev.nstv.easing.symphony.design.TileColor

@Composable
fun Ball(
    modifier: Modifier = Modifier,
    size: Dp = Grid.Five,
    color: Color = TileColor.Blue,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(shape = CircleShape, color = color)
    )
}