package com.support.core.navigation

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import kotlin.reflect.KClass


fun Navigator.navigate(
        destination: KClass<out Fragment>,
        args: FragmentArgs? = null,
        navOptions: NavOptions? = null
) {
    navigate(destination, args?.toBundle(), navOptions)
}

/**
 * Navigate to fragment view container with specify child fragment
 * @param id Fragment view container Id
 * @param destination child fragment class
 * @param args
 * @param navOptions
 */
fun Navigator.navigateToHost(@IdRes id: Int, destination: KClass<out Fragment>, args: Bundle? = null, navOptions: NavOptions? = null) {
    val bundle = Bundle()
    bundle.putString(NavHostFragment.START_CLASS_NAME, destination.java.name)
    bundle.putInt(NavHostFragment.ID, id)
    bundle.putParcelable(NavHostFragment.NAV_OPTION, navOptions)
    bundle.putBundle(NavHostFragment.ARGUMENT, args)
    navigate(NavHostFragment::class, bundle)
}

fun FragmentActivity.findNavigator(@IdRes containerId: Int = 0): Navigator {
    if (this is NavigationOwner) {
        if (containerId == 0) return this.navigator
        else if (containerId == this.navigator.container) return this.navigator
    }

    if (containerId == 0) return supportFragmentManager.fragments.find { it is NavHostFragment }?.findNavigator()
            ?: error("Not found navigator")

    return (supportFragmentManager.findFragmentById(containerId) as? NavHostFragment)?.navigator
            ?: error("Not found navigator")
}

fun Fragment.findNavigator(@IdRes containerId: Int = 0): Navigator {
    if (this is NavigationOwner) {
        if (containerId == 0) return this.navigator
        else if (containerId == this.navigator.container) return this.navigator
    }

    if (containerId != 0) {
        val navigator =
                (childFragmentManager.findFragmentById(containerId) as? NavHostFragment)?.navigator
        if (navigator != null) return navigator
    }

    if (parentFragment == null) {
        val activity = this.activity
        if (activity is NavigationOwner) {
            if (containerId == 0) return activity.navigator
            else if (containerId == activity.navigator.container) return activity.navigator
        }
        error("Not found navigator")
    }
    return parentFragment!!.findNavigator(containerId)
}