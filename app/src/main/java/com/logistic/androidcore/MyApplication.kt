package com.logistic.androidcore

import android.app.Application
import com.support.core.dependencies

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        dependencies {
            single<AuthRepository> { AuthRepository1() }
            single<AuthRepository>(override = true) { AuthRepository2() }
        }
    }
}