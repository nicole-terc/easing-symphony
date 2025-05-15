package dev.nstv.easing.symphony.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

// TODO: migrate lifecycle logic to use appyx lifecycle
enum class LifecycleEvent {
    onCreate, onStart, onResume, onPause, onStop, onDestroy, onAny
}

object LifecycleOwner {
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private val _lifecycleEvents = MutableSharedFlow<LifecycleEvent>()
    val lifecycleEvents = _lifecycleEvents.asSharedFlow()

    fun onLifecycleEvent(event: LifecycleEvent) {
        println("LifecycleEvent: ${event.name}")

        coroutineScope.launch {
            _lifecycleEvents.emit(event)
        }
    }
}

@Composable
fun observeLifecycle(
    key: Any? = Unit,
    onCreate: () -> Unit = {},
    onStart: () -> Unit = {},
    onResume: () -> Unit = {},
    onPause: () -> Unit = {},
    onStop: () -> Unit = {},
    onDestroy: () -> Unit = {},
    onAny: () -> Unit = {},
) {
    LaunchedEffect(key) {
        LifecycleOwner.lifecycleEvents.collect {
            when (it) {
                LifecycleEvent.onCreate -> onCreate()
                LifecycleEvent.onStart -> onStart()
                LifecycleEvent.onResume -> onResume()
                LifecycleEvent.onPause -> onPause()
                LifecycleEvent.onStop -> onStop()
                LifecycleEvent.onDestroy -> onDestroy()
                LifecycleEvent.onAny -> onAny()
            }
        }
    }
}

@Composable
fun DisposableEffectWithLifecycle(
    key: Any? = Unit,
    onPause: () -> Unit = {},
    onResume: () -> Unit = {},
) {
    var onResumeToggle by remember { mutableStateOf(false) }

    observeLifecycle(
        key = Unit,
        onPause = { onPause() },
        onResume = { onResumeToggle = !onResumeToggle }
    )

    DisposableEffect(onResumeToggle, key) {
        onResume()
        onDispose {
            onPause()
        }
    }
}

