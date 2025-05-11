package dev.nstv.easing.symphony.screen

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Divider
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


private enum class Screen {
    EASING,
    ANIMATION_SPEC,
    ANIMATION_SPEC_OFFSET,
    MUSIC_VISUALIZER,
    LOG_SPIRAL,
}

const val UseSlidesBackground = false

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
    ) {
        var selectedScreen by remember { mutableStateOf(ANIMATION_SPEC_OFFSET) }

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
                modifier = Modifier.padding(vertical = Grid.One, horizontal = Grid.Two),
                targetState = selectedScreen,
                animationSpec = tween(durationMillis = 500)
            ) { screen ->
                when (screen) {
                    EASING -> EasingScreen()
                    ANIMATION_SPEC -> AnimationSpecScreen()
                    MUSIC_VISUALIZER -> MusicVisualizerScreen()
                    ANIMATION_SPEC_OFFSET -> OffsetAnimationSpecScreen()
                    LOG_SPIRAL -> LogSpiralScreen()
                }
            }
        }
    }
}
