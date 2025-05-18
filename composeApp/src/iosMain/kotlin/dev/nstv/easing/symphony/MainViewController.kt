package dev.nstv.easing.symphony

import androidx.compose.ui.uikit.ComposeUIViewControllerDelegate
import androidx.compose.ui.window.ComposeUIViewController
import dev.nstv.easing.symphony.util.LifecycleEvent
import dev.nstv.easing.symphony.util.LifecycleOwner

fun MainViewController() = ComposeUIViewController(
    configure = {
        delegate = CustomDelegate()
    }
) { App() }

private class CustomDelegate() : ComposeUIViewControllerDelegate {
    override fun viewDidAppear(animated: Boolean) {
        super.viewDidAppear(animated)
        LifecycleOwner.onLifecycleEvent(LifecycleEvent.onResume)
    }

    override fun viewDidLoad() {
        super.viewDidLoad()
        LifecycleOwner.onLifecycleEvent(LifecycleEvent.onCreate)
    }

    override fun viewWillDisappear(animated: Boolean) {
        super.viewWillDisappear(animated)
        LifecycleOwner.onLifecycleEvent(LifecycleEvent.onPause)
    }

    override fun viewWillAppear(animated: Boolean) {
        super.viewWillAppear(animated)
        LifecycleOwner.onLifecycleEvent(LifecycleEvent.onStart)
    }

    override fun viewDidDisappear(animated: Boolean) {
        super.viewDidDisappear(animated)
        LifecycleOwner.onLifecycleEvent(LifecycleEvent.onDestroy)
    }
}