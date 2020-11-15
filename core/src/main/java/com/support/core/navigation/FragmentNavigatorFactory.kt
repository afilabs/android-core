package com.support.core.navigation

import androidx.fragment.app.FragmentManager
import com.support.core.InjectBy
import com.support.core.Injectable
import com.support.core.navigation.v1.FragmentNavigator

@InjectBy(DefaultFragmentNavigatorFactory::class)
interface FragmentNavigatorFactory{
    fun create(manager: FragmentManager, containerId: Int): Navigator
}

class DefaultFragmentNavigatorFactory : FragmentNavigatorFactory, Injectable {
    override fun create(manager: FragmentManager, containerId: Int): Navigator {
        return FragmentNavigator(manager, containerId)
    }
}