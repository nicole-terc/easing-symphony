package dev.nstv.easing.symphony.screen.music

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.nstv.easing.symphony.musicvisualizer.SimpleMusicVisualizer
import dev.nstv.easing.symphony.screen.musicFilePath
import easingsymphony.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun SimpleMusicVisualizerScreen(
    modifier: Modifier = Modifier,
) {
    SimpleMusicVisualizer(
        fileUri = Res.getUri(musicFilePath),
        modifier
    )
}