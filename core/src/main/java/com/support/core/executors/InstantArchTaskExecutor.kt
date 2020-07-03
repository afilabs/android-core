package com.support.core.executors

import android.annotation.SuppressLint
import androidx.arch.core.executor.TaskExecutor

@SuppressLint("RestrictedApi")
class InstantArchTaskExecutor : TaskExecutor() {
    override fun executeOnDiskIO(runnable: Runnable) {
        runnable.run()
    }

    override fun postToMainThread(runnable: Runnable) {
        runnable.run()
    }

    override fun isMainThread(): Boolean {
        return true
    }
}