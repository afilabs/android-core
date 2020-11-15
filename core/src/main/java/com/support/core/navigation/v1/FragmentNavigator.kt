package com.support.core.navigation.v1

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.support.core.functional.Backable
import com.support.core.navigation.Destination
import com.support.core.navigation.NavOptions
import com.support.core.navigation.Navigator
import com.support.core.navigation.notifyArgumentChangeIfNeeded
import java.util.*
import kotlin.reflect.KClass

class FragmentNavigator(
    fragmentManager: FragmentManager,
    container: Int
) : Navigator(fragmentManager, container) {

    private val mStack = DestinationStack()

    override val lastDestination: Destination? get() = mStack.last

    override fun navigate(kClass: KClass<out Fragment>, args: Bundle?, navOptions: NavOptions?) {
        transaction {
            val isStart = mStack.isEmpty
            val lastVisible = mStack.last

            val fragmentRemoves = if (navOptions?.popupTo == null) emptyList()
            else popBackStack(kClass, navOptions)

            val (destination, fragment) = findOrCreate(kClass, navOptions)

            fragment.notifyArgumentChangeIfNeeded(args)

            setNavigateAnim(destination, lastVisible, isStart)

            lastVisible
                ?.requireFragment
                ?.takeIf { !fragmentRemoves.contains(it) }
                ?.also { hide(it) }

            fragmentRemoves.forEach { remove(it) }
            if (fragment.isAdded) show(fragment)
            else add(container, fragment, destination.tag)
            notifyDestinationChange(kClass)
            Log.i("Stack", mStack.toString())
        }
    }

    private fun findOrCreate(
        kClass: KClass<out Fragment>,
        navOptions: NavOptions?
    ): Pair<Destination, Fragment> {
        if (navOptions?.singleTask == true) {
            val des = mStack.find(kClass)
            if (des != null) {
                mStack.remove(des)
                mStack.push(des)
                return des to des.requireFragment
            }
        }
        val keepInstance = navOptions?.reuseInstance ?: false

        val tagId = if (keepInstance) kClass.tagId ?: System.currentTimeMillis()
        else System.currentTimeMillis()

        val des = mStack.create(kClass, tagId, navOptions)
        if (des.keepInstance) {
            val fragment = des.fragment
            if (fragment != null) return des to fragment
        }
        return des to des.createFragment()
    }

    private fun popBackStack(
        kClass: KClass<out Fragment>,
        navOptions: NavOptions
    ): ArrayList<Fragment> {
        val removes = arrayListOf<Fragment>()
        fun popup(it: Destination) {
            if (it.keepInstance) return
            if (kClass != it.kClass || !navOptions.singleTask) removes.add(it.requireFragment)
        }

        if (navOptions.inclusive) mStack.removeUntil(navOptions, kClass, ::popup)
        else mStack.removeAllIgnore(navOptions, kClass, ::popup)
        return removes
    }

    override fun navigateUp(): Boolean {
        if (mStack.isEmpty) return false
        val currentFragment = mStack.last!!.requireFragment
        if (currentFragment is Backable && currentFragment.onInterceptBackPress()) return true

        if (mStack.size == 1) return false
        val current = mStack.pop() ?: return false
        val previous = mStack.last
        transaction {
            setPopupAnim(current, previous)

            if (current.keepInstance) hide(currentFragment)
            else remove(currentFragment)

            if (previous != null) {
                val fragment=previous.requireFragment
                show(fragment)
                notifyDestinationChange(fragment.javaClass.kotlin)
            }
        }
        Log.i("Stack", mStack.toString())
        return previous != null
    }

    override fun onSaveInstance(state: Bundle) {
        super.onSaveInstance(state)
        mStack.onSaveInstance(state)
        Log.i("Stack", "Saved")
    }

    override fun onRestoreInstance(saved: Bundle) {
        super.onRestoreInstance(saved)
        mStack.onRestoreInstance(saved)
        Log.i("Stack", mStack.toString())
    }

}
