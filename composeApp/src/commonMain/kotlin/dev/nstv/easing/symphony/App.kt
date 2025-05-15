package dev.nstv.easing.symphony

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.nstv.easing.symphony.design.EasingSymphonyTheme
import dev.nstv.easing.symphony.screen.MainScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    EasingSymphonyTheme {
        MainScreen(modifier = Modifier.fillMaxSize().safeDrawingPadding())
    }
}