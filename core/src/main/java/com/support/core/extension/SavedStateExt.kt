package com.support.core.extension

import androidx.savedstate.SavedStateRegistry
import com.support.core.functional.SavedStateCallback

fun SavedStateRegistry.registry(key: String, savedStateCallback: SavedStateCallback) {
    registerSavedStateProvider(key) {
        savedStateCallback.onSavedState()
    }
    if (!this.isRestored) return
    val saved = consumeRestoredStateForKey(key) ?: return
    savedStateCallback.onRestoreState(saved)
}