package com.support.core.functional

import android.os.Bundle

interface SavedStateCallback {
    fun onSavedState(): Bundle
    fun onRestoreState(savedState: Bundle)
}