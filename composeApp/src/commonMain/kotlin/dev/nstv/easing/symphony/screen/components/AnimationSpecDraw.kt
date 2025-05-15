package dev.nstv.easing.symphony.screen.components

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import dev.nstv.easing.symphony.design.TileColor

@Composable
fun DrawAnimationSpecPath(
    spec: AnimationSpec<Offset>,
    from: Offset,
    to: Offset,
    steps: Int = 1000,
    color: Color = TileColor.LightGray,
    modifier: Modifier = Modifier
) {
    val converter = Offset.VectorConverter
    val vectorizedSpec = spec.vectorize(converter)
    val initialVec = converter.convertToVector(from)
    val targetVec = converter.convertToVector(to)
    val initialVelocity = AnimationVector2D(0f, 0f)
    val durationNanos = vectorizedSpec.getDurationNanos(initialVec, targetVec, initialVelocity)

    Canvas(modifier = modifier) {
        val path = Path()

        for (i in 0..steps) {
            val t = (i / steps.toFloat()) * durationNanos
            val vector =
                vectorizedSpec.getValueFromNanos(
                    t.toLong(),
                    initialVec,
                    targetVec,
                    initialVelocity
                )
            val offset = converter.convertFromVector(vector)

            if (i == 0) path.moveTo(offset.x, offset.y)
            else path.lineTo(offset.x, offset.y)
        }

        drawPath(path, color = color, style = Stroke(width = 2f))

        drawLine(
            start = from,
            end = to,
            color = Color.Yellow,
            strokeWidth = 2f
        )
    }
}