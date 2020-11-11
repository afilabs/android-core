package androidx.fragment.app

import androidx.lifecycle.Lifecycle

internal val FragmentManager.fragmentLifecycle: Lifecycle
    get() {
        if (this !is FragmentManagerImpl) error("${this.javaClass.name} is not instance of FragmentManagerImpl")
        return mParent?.lifecycle ?: error("Fragment is not attached, please call after onAttached")
    }