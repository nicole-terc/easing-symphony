package dev.nstv.easing.symphony.screen

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion
import dev.nstv.easing.symphony.design.components.DropDownWithArrows
import dev.nstv.easing.symphony.design.Grid
import dev.nstv.easing.symphony.design.slidesBackground
import dev.nstv.easing.symphony.screen.Screen.*
import dev.nstv.easing.symphony.screen.animationspec.AnimationSpecScreen
import dev.nstv.easing.symphony.screen.animationspec.EasingScreen
import dev.nstv.easing.symphony.screen.showcase.EasingShowcaseScreen
import dev.nstv.easing.symphony.screen.animationspec.OffsetAnimationSpecScreen
import dev.nstv.easing.symphony.screen.`fun`.LogSpiralScreen
import dev.nstv.easing.symphony.screen.music.AdvancedAmplitudeVisualizersScreen
import dev.nstv.easing.symphony.screen.music.AmplitudeVisualizersScreen
import dev.nstv.easing.symphony.screen.music.BurstMusicVisualizerScreen
import dev.nstv.easing.symphony.screen.music.MusicPropertiesScreen
import dev.nstv.easing.symphony.screen.music.SimpleMusicVisualizerScreen
import dev.nstv.easing.symphony.screen.music.TheMusicVisualizer
import dev.nstv.easing.symphony.screen.music.TheMusicVisualizerPartOne
import dev.nstv.easing.symphony.screen.showcase.AnimationSpecEasingShowcaseScreen
import dev.nstv.easing.symphony.screen.showcase.AnimationSpecShowcaseScreen


private enum class Screen {
    EASING,
    ANIMATION_SPEC,
    ANIMATION_SPEC_OFFSET,
    LOG_SPIRAL,
    MUSIC_PROPERTIES,
    SIMPLE_MUSIC_VISUALIZER,
    BURST_MUSIC_VISUALIZER,
    AMPLITUDE_VISUALIZER,
    ADV_AMPLITUDE_VISUALIZER,
    SHOWCASE_ANIMATION_SPEC,
    SHOWCASE_EASING,
    SHOWCASE_AMPLITUDE_EASING,
    THE_MUSIC_VISUALIZER,
    VISUALIZER_PART_1,
}

const val musicFileName = "nicmix.wav"
const val musicFilePath = "files/$musicFileName"
const val UseSlidesBackground = false
const val HideOptions = false

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
    ) {
        var selectedScreen by remember { mutableStateOf(VISUALIZER_PART_1) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(if (UseSlidesBackground) slidesBackground else Companion.Unspecified)
        ) {
            DropDownWithArrows(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentSize(Alignment.TopStart)
                    .padding(Grid.One),
                options = Screen.entries.map { it.name }.toList(),
                selectedIndex = Screen.entries.indexOf(selectedScreen),
                onSelectionChanged = { selectedScreen = Screen.entries[it] },
                textStyle = MaterialTheme.typography.headlineSmall,
                loopSelection = true,
            )
            HorizontalDivider()
            Crossfade(
                modifier = Modifier.padding(vertical = Grid.Two, horizontal = Grid.Two),
                targetState = selectedScreen,
                animationSpec = tween(durationMillis = 500)
            ) { screen ->
                when (screen) {
                    EASING -> EasingScreen()
                    ANIMATION_SPEC -> AnimationSpecScreen()
                    MUSIC_PROPERTIES -> MusicPropertiesScreen()
                    SIMPLE_MUSIC_VISUALIZER -> SimpleMusicVisualizerScreen()
                    BURST_MUSIC_VISUALIZER -> BurstMusicVisualizerScreen()
                    ANIMATION_SPEC_OFFSET -> OffsetAnimationSpecScreen()
                    LOG_SPIRAL -> LogSpiralScreen()
                    AMPLITUDE_VISUALIZER -> AmplitudeVisualizersScreen()
                    ADV_AMPLITUDE_VISUALIZER -> AdvancedAmplitudeVisualizersScreen()
                    SHOWCASE_ANIMATION_SPEC -> AnimationSpecShowcaseScreen()
                    SHOWCASE_EASING -> EasingShowcaseScreen()
                    SHOWCASE_AMPLITUDE_EASING -> AnimationSpecEasingShowcaseScreen()
                    THE_MUSIC_VISUALIZER -> TheMusicVisualizer()
                    VISUALIZER_PART_1 -> TheMusicVisualizerPartOne()
                }
            }
        }
    }
}
