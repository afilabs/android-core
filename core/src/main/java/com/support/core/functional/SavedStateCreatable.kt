package com.support.core.functional

import android.os.Bundle

interface SavedStateCreatable {
    fun onCreate(savedState: Bundle?)

    fun onSavedState(): Bundle = Bundle()
}