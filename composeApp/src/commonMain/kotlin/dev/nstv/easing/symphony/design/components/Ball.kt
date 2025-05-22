package dev.nstv.easing.symphony.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
            .aspectRatio(1f)
            .background(shape = CircleShape, color = color)
            .border(shape = CircleShape, color = MaterialTheme.colorScheme.background, width = 1.dp)
    )
}