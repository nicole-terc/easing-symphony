package dev.nstv.easing.symphony.musicvisualizer.reader

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import dev.nstv.easing.symphony.util.DisposableEffectWithLifecycle
import kotlinx.coroutines.launch

@Composable
fun MusicReaderWrapper(
    fileUri: String,
    playOnLoad: Boolean = true,
    normalized: Boolean = true,
    frameContent: @Composable (
        musicReader: MusicReader,
    ) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val musicReader = provideMusicReader(normalized, playOnLoad)
    coroutineScope.launch { musicReader.loadFile(fileUri) }

    MusicReaderWrapperContent(musicReader, frameContent)
}

@Composable
private fun MusicReaderWrapperContent(
    musicReader: MusicReader,
    frameContent: @Composable (
        musicReader: MusicReader,
    ) -> Unit
) {
    DisposableEffectWithLifecycle(
        musicReader,
        onPause = { musicReader.pause() },
        onResume = { musicReader.play() }
    )

    frameContent(musicReader)
}
