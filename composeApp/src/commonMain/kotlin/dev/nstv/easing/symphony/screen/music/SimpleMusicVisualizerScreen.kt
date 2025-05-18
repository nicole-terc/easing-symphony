package dev.nstv.easing.symphony.screen.music

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.nstv.easing.symphony.design.components.DropDownWithArrows
import dev.nstv.easing.symphony.musicvisualizer.EffectVisualizer
import dev.nstv.easing.symphony.musicvisualizer.reader.MusicPlayer
import dev.nstv.easing.symphony.musicvisualizer.VisualizerType
import dev.nstv.easing.symphony.screen.musicFilePath
import easingsymphony.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class, ExperimentalFoundationApi::class)
@Composable
fun SimpleMusicVisualizerScreen(
    modifier: Modifier = Modifier,
) {
    MusicPlayer(
        fileUri = Res.getUri(musicFilePath),
        playOnLoad = false,
        normalized = false,
    ) { fftData, amplitudeData, togglePlayback ->

        var visualizerType by remember { mutableStateOf(VisualizerType.Simple) }

        Column(
            modifier = modifier.fillMaxSize().combinedClickable(
                onDoubleClick = { togglePlayback() },
            ) {}
        ) {
            DropDownWithArrows(
                options = VisualizerType.entries.map { it.name },
                onSelectionChanged = { visualizerType = VisualizerType.entries[it] },
                loopSelection = true,
            )
            EffectVisualizer(
                fft = fftData,
                modifier = Modifier.fillMaxSize(),
                visualizerType = visualizerType,
            )
        }
    }
}