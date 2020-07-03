package com.support.core

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

interface TaskExecutors {
    val diskIO: Executor
    val scheduler: ScheduledExecutorService
    val mainIO: Executor

    val launchIO: ExecutorService
    val concurrentIO: ExecutorService
    val isOnMainThread: Boolean
}

class DefaultTaskExecutor : TaskExecutors {
    override val diskIO: Executor = Executors.newSingleThreadExecutor()

    override val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(2)

    override val mainIO: Executor = MainExecutor()

    override val launchIO: ExecutorService = Executors.newFixedThreadPool(3)

    override val concurrentIO: ExecutorService = Executors.newCachedThreadPool()

    override val isOnMainThread: Boolean get() = Looper.getMainLooper() == Looper.myLooper()

}

class AppExecutors : TaskExecutors {
    private var mDefaultTaskExecutor: TaskExecutors? = null
    private var mDelegate: TaskExecutors? = null

    private val defaultTaskExecutor: TaskExecutors
        get() {
            if (mDefaultTaskExecutor == null) mDefaultTaskExecutor = DefaultTaskExecutor()
            return mDefaultTaskExecutor!!
        }

    private val delegate: TaskExecutors
        get() {
            if (mDelegate == null) synchronized(this) {
                if (mDelegate == null) mDelegate = defaultTaskExecutor
            }
            return mDelegate!!
        }

    override val diskIO: Executor get() = delegate.diskIO
    override val launchIO: ExecutorService get() = delegate.launchIO
    override val scheduler: ScheduledExecutorService get() = delegate.scheduler
    override val mainIO: Executor get() = delegate.mainIO
    override val concurrentIO: ExecutorService get() = delegate.concurrentIO
    override val isOnMainThread get() = delegate.isOnMainThread

    companion object {
        private val sInstance: AppExecutors by lazy { AppExecutors() }

        val isOnMainThread get() = sInstance.isOnMainThread

        val diskIO: Executor get() = sInstance.diskIO
        val scheduler: ScheduledExecutorService get() = sInstance.scheduler
        val mainIO: Executor get() = sInstance.mainIO
        val launchIO: ExecutorService get() = sInstance.launchIO
        val concurrentIO: ExecutorService get() = sInstance.concurrentIO

        fun setDelegate(delegate: TaskExecutors?) {
            sInstance.mDelegate = delegate
        }

        fun <T> loadInBackGround(function: () -> T): ExecutorConcurrent<T> {
            return ExecutorConcurrent(function)
        }
    }
}

class ExecutorConcurrent<T>(private val function: () -> T) {

    fun postOnUi(uiFunction: (T) -> Unit) {
        AppExecutors.diskIO.execute {
            val result = function()
            AppExecutors.mainIO.execute {
                uiFunction(result)
            }
        }
    }
}

class MainExecutor : Executor {
    private val mHandler = Handler(Looper.getMainLooper())

    override fun execute(command: Runnable) {
        if (!isOnMainThread) mHandler.post(command)
        else command.run()
    }
}

val isOnMainThread get() = AppExecutors.isOnMainThread

