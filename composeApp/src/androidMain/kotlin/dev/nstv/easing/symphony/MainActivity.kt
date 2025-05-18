package dev.nstv.easing.symphony

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import dev.nstv.easing.symphony.util.LifecycleEvent
import dev.nstv.easing.symphony.util.LifecycleOwner as CustomLifecycleOwner

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            App()
        }
        lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                when (event) {
                    Lifecycle.Event.ON_CREATE -> CustomLifecycleOwner.onLifecycleEvent(LifecycleEvent.onCreate)
                    Lifecycle.Event.ON_START -> CustomLifecycleOwner.onLifecycleEvent(LifecycleEvent.onStart)
                    Lifecycle.Event.ON_RESUME -> CustomLifecycleOwner.onLifecycleEvent(LifecycleEvent.onResume)
                    Lifecycle.Event.ON_PAUSE -> CustomLifecycleOwner.onLifecycleEvent(LifecycleEvent.onPause)
                    Lifecycle.Event.ON_STOP -> CustomLifecycleOwner.onLifecycleEvent(LifecycleEvent.onStop)
                    Lifecycle.Event.ON_DESTROY -> CustomLifecycleOwner.onLifecycleEvent(
                        LifecycleEvent.onDestroy
                    )
                    Lifecycle.Event.ON_ANY -> CustomLifecycleOwner.onLifecycleEvent(LifecycleEvent.onAny)
                }
            }
        })
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}