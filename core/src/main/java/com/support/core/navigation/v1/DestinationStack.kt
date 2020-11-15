package com.support.core.navigation.v1

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.support.core.navigation.Destination
import com.support.core.navigation.NavOptions
import java.util.*
import kotlin.reflect.KClass

class DestinationStack {
    val size get() = mStack.size
    val isEmpty get() = mStack.isEmpty()
    private val mStack = Stack<Destination>()
    val last: Destination?
        get() {
            if (mStack.empty()) return null
            return mStack.lastElement()
        }

    fun find(
            kClass: KClass<out Fragment>,
            accept: ((Destination) -> Boolean)? = null
    ): Destination? {
        if (accept != null) return mStack.findLast { (it.kClass == kClass) && accept(it) }
        return mStack.findLast { it.kClass == kClass }
    }

    fun create(kClass: KClass<out Fragment>, tagId: Long, navOptions: NavOptions?): Destination {
        return Destination(
                kClass,
                tagId,
                navOptions?.reuseInstance ?: false
        ).also {
            if (navOptions != null) {
                it.animEnter = navOptions.animEnter
                it.animExit = navOptions.animExit
                it.animPopEnter = navOptions.animPopEnter
                it.animPopExit = navOptions.animPopExit
            }
            mStack.push(it)
        }
    }

    fun pop(): Destination? {
        if (mStack.empty()) return null
        return mStack.pop()
    }

    fun removeUntil(
            navOptions: NavOptions,
            kClassNavigate: KClass<out Fragment>,
            function: (Destination) -> Unit
    ) {
        removeAll(navOptions, kClassNavigate, function) {
            mStack.pop().also(function)
        }
    }

    fun removeAllIgnore(
            navOptions: NavOptions,
            kClassNavigate: KClass<out Fragment>,
            function: (Destination) -> Unit
    ) {
        removeAll(navOptions, kClassNavigate, function)
    }

    private fun removeAll(
            navOptions: NavOptions,
            kClassNavigate: KClass<out Fragment>,
            onNext: (Destination) -> Unit,
            doFinish: ((Destination) -> Destination?)? = null
    ) {
        var isOverDestination = false
        var singleTop: Destination? = null
        while (true) {
            if (mStack.empty()) break
            val current = mStack.lastElement()
            var isAtSingleTop = false

            if (current.kClass == kClassNavigate) {
                isOverDestination = true
                if (navOptions.singleTask) isAtSingleTop = true
            }

            if (current.kClass == navOptions.popupTo && isOverDestination) {
                val next = doFinish?.invoke(current)
                if (isAtSingleTop) singleTop = next
                break
            }

            val next = mStack.pop().also(onNext)
            if (isAtSingleTop) singleTop = next
        }
        singleTop?.also { mStack.push(it) }
    }

    override fun toString(): String {
        return mStack.joinToString()
    }

    fun onSaveInstance(state: Bundle) {
        val bundle = Bundle()
        mStack.forEachIndexed { index, destination ->
            bundle.putBundle(index.toString(), destination.toBundle())
        }
        state.putBundle(STACK, bundle)
    }

    fun onRestoreInstance(saved: Bundle) {
        val bundle = saved.getBundle(STACK) ?: error("Error restore state")
        bundle.keySet().forEach {
            mStack.push(
                    Destination.of(
                            bundle.getBundle(it) ?: error("Error restore destination")
                    )
            )
        }
    }

    fun remove(des: Destination) {
        mStack.remove(des)
    }

    fun push(des: Destination) {
        mStack.push(des)
    }

    companion object {
        private const val STACK = "android:destination:stack"
    }
}