package dev.nstv.easing.symphony.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.util.lerp
import dev.nstv.easing.symphony.design.Grid
import dev.nstv.easing.symphony.design.TileColor

fun getSizeForPercentage(
    percentage: Float,
    maxSize: Dp = Grid.Ten,
    minSize: Dp = Grid.Three,
) = lerp(minSize, maxSize, percentage)

fun getColorForPercentage(
    percentage: Float,
    maxColor: Color = TileColor.Blue,
    minColor: Color = TileColor.Green,
) = lerp(minColor, maxColor, percentage)


fun getAlphaForPercentage(
    percentage: Float,
    maxAlpha: Float = 0.9f,
    minAlpha: Float = 0.2f,
) = lerp(minAlpha, maxAlpha, percentage)

