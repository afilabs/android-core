package com.support.core.navigation.v2

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.support.core.navigation.Destination
import java.util.*
import kotlin.reflect.KClass

class DestinationStack {
    private val mStack = Stack<Destination>()

    val size get() = mStack.size
    val isEmpty get() = mStack.isEmpty()
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

    fun pop(): Destination? {
        if (mStack.empty()) return null
        return mStack.pop()
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

    fun popBack(listener: OnPopListener) {

        while (true) {
            if (mStack.empty()) return
            val current = mStack.lastElement()

            if (listener.shouldPop(current)) {
                mStack.pop()
                listener.onPop(current)
            }

            if (!listener.shouldNext(current)) break
        }
    }

    fun hasTagId(tagId: Long): Boolean {
        return mStack.findLast { it.tagId == tagId } != null
    }

    interface OnPopListener {
        fun shouldPop(des: Destination): Boolean
        fun shouldNext(des: Destination): Boolean
        fun onPop(des: Destination) {}
    }

    companion object {
        private const val STACK = "android:destination:stack"
    }
}