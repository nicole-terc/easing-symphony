package dev.nstv.easing.symphony.screen.`fun`

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.Draggable2DState
import androidx.compose.foundation.gestures.draggable2D
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import dev.nstv.easing.symphony.design.Grid
import dev.nstv.easing.symphony.design.TileColor
import dev.nstv.easing.symphony.design.components.Ball
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.hypot
import kotlin.math.ln
import kotlin.math.sin

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LogSpiralScreen(
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val ballSize = Grid.Five
    val ballSizeHalfPixels = with(LocalDensity.current) { ballSize.toPx() / 2 }

    var startPoint by remember { mutableStateOf(Offset(300f, 200f)) }
    var endPoint by remember { mutableStateOf(Offset(600f, 400f)) }

    val pointOne = remember { Animatable(Offset(300f, 200f), Offset.VectorConverter) }
    val pointTwo = remember { Animatable(Offset(600f, 400f), Offset.VectorConverter) }

    var pointsAreMoving by remember { mutableStateOf(false) }

    fun dragStopped() {
        coroutineScope.launch {
            startPoint = pointOne.value
            endPoint = pointTwo.value
            pointsAreMoving = false
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Ball(
            size = ballSize,
            color = TileColor.Blue.copy(alpha = 0.8f),
            modifier = Modifier.graphicsLayer {
                translationX = pointOne.value.x - ballSizeHalfPixels
                translationY = pointOne.value.y - ballSizeHalfPixels
            }.draggable2D(
                onDragStarted = {
                    coroutineScope.launch {
                        pointsAreMoving = true
                    }
                },
                onDragStopped = {
                    dragStopped()
                },
                state = Draggable2DState {
                    coroutineScope.launch {
                        pointOne.snapTo(pointOne.value + it)
                    }
                }
            )
        )

        Ball(
            size = ballSize,
            color = TileColor.Yellow.copy(alpha = 0.8f),
            modifier = Modifier.graphicsLayer {
                translationX = pointTwo.value.x - ballSizeHalfPixels
                translationY = pointTwo.value.y - ballSizeHalfPixels
            }.draggable2D(
                onDragStarted = {
                    coroutineScope.launch {
                        delay(100)
                        pointsAreMoving = true
                    }
                },
                onDragStopped = {
                    dragStopped()
                },
                state = Draggable2DState {
                    coroutineScope.launch {
                        pointTwo.snapTo(pointTwo.value + it)
                    }
                }
            )
        )

        LogSpiralPlot(
            start = startPoint,
            end = endPoint,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun LogSpiralPlot(
    start: Offset,
    end: Offset,
    modifier: Modifier = Modifier,
    color: Color = TileColor.Purple,
    spiralTurns: Int = 4,
    resolution: Int = 800,
    strokeWidth: Float = 10f,
    inverted: Boolean = false
) {
    Canvas(modifier = modifier) {
        val (center, target) = if (inverted) start to end else end to start

        // Vector from center to target
        val dx = target.x - center.x
        val dy = target.y - center.y

        val rFar = hypot(dx, dy)
        val thetaFar = atan2(dy, dx)
        val deltaTheta = (spiralTurns * 2f * PI).toFloat()

        val (theta1, theta2) = if (inverted) {
            val t1 = thetaFar - deltaTheta
            t1 to thetaFar
        } else {
            val t2 = thetaFar + deltaTheta
            thetaFar to t2
        }

        val rNear = 0.01f
        val lnRRatio = ln(rFar / rNear)
        val b = lnRRatio / (theta2 - theta1)
        val a = rFar / exp(b * theta2)

        val path = Path()
        for (i in 0..resolution) {
            val t = theta1 + (i / resolution.toFloat()) * (theta2 - theta1)
            val r = a * exp(b * t)
            val x = r * cos(t)
            val y = r * sin(t)
            val point = Offset(x, y) + center

            if (i == 0) path.moveTo(point.x, point.y)
            else path.lineTo(point.x, point.y)
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}