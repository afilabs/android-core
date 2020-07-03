package com.support.core

import com.support.core.executors.InstantTaskExecutors
import org.junit.Test

class AppExecutorInstantTest {
    @Test
    fun testExecutor() {
        AppExecutors.setDelegate(InstantTaskExecutors())
        AppExecutors.diskIO.execute {

        }
    }
}