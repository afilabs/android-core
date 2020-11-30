package com.support.core.navigation

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

interface ArgumentChangeable {
    fun onNewArguments(arguments: Bundle)
}

fun Fragment.notifyArgumentChangeIfNeeded(args: Bundle?) {
    arguments = args
    args ?: return
    if (this !is ArgumentChangeable) return
    if (isAdded) {
        onNewArguments(args)
        return
    }
    lifecycle.addObserver(object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        fun onEvent() {
            lifecycle.removeObserver(this)
            onNewArguments(args)
        }
    })
}