package com.support.core.navigation

import androidx.fragment.app.FragmentManager
import com.support.core.InjectBy
import com.support.core.Injectable
import com.support.core.navigation.v1.FragmentNavigator

@InjectBy(FragmentNavigatorFactoryV2::class)
interface FragmentNavigatorFactory{
    fun create(manager: FragmentManager, containerId: Int): Navigator
}

class FragmentNavigatorFactoryV1 : FragmentNavigatorFactory, Injectable {
    override fun create(manager: FragmentManager, containerId: Int): Navigator {
        return FragmentNavigator(manager, containerId)
    }
}

class FragmentNavigatorFactoryV2 : FragmentNavigatorFactory, Injectable {
    override fun create(manager: FragmentManager, containerId: Int): Navigator {
        return com.support.core.navigation.v2.FragmentNavigator(manager, containerId)
    }
}