package androidx.fragment.app

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryOwner

private val FragmentManager.host: Any
    get() {
        if (this !is FragmentManagerImpl) error("${this.javaClass.name} is not instance of FragmentManagerImpl")
        if (mParent != null) return mParent
        val activity = mHost?.activity ?: error("Activity not attached yet!")
        if (activity !is FragmentActivity) error("Activity ${activity.javaClass.simpleName} should be instance of FragmentActivity")
        return activity
    }

internal val FragmentManager.lifecycle: Lifecycle
    get() = (host as LifecycleOwner).lifecycle

internal val FragmentManager.savedStateRegistry: SavedStateRegistry
    get() = (host as SavedStateRegistryOwner).savedStateRegistry