package com.logistic.androidcore

import android.app.Application
import androidx.fragment.app.FragmentManager
import com.support.core.dependencies
import com.support.core.navigation.FragmentNavigatorFactory
import com.support.core.navigation.Navigator
import com.support.core.navigation.v2.FragmentNavigator

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        dependencies {
            single<AuthRepository> { AuthRepository1() }
            single<AuthRepository>(override = true) { AuthRepository2() }
        }
    }
}