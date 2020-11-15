package com.support.core.navigation

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.annotation.IdRes
import androidx.fragment.app.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.support.core.extension.registry
import com.support.core.functional.SavedStateCallback
import java.util.*
import kotlin.reflect.KClass

abstract class Navigator(private val fragmentManager: FragmentManager, @IdRes val container: Int) {
    companion object {
        private const val KEY_SAVED_STATE = "com:support:core:navigation:navigator"
    }

    abstract val lastDestination: Destination?
    private val mTransactionManager = TransactionManager()
    private var mExecutable: Boolean = true

    private var mDestinationChangeListeners = arrayListOf<OnDestinationChangeListener>()

    private val mSavedStateListener = object : SavedStateCallback {
        override fun onSavedState(): Bundle = Bundle().apply(::onSaveInstance)

        override fun onRestoreState(savedState: Bundle) = onRestoreInstance(savedState)
    }

    // Fix for case "Can not perform this action after onSaveInstanceState"
    private val mObserver = object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            when (event) {
                Lifecycle.Event.ON_CREATE -> {
                    fragmentManager.savedStateRegistry.registry(KEY_SAVED_STATE, mSavedStateListener)
                }
                Lifecycle.Event.ON_DESTROY -> {
                    fragmentManager.savedStateRegistry.unregisterSavedStateProvider(KEY_SAVED_STATE)
                    source.lifecycle.removeObserver(this)
                    mDestinationChangeListeners.clear()
                }
                Lifecycle.Event.ON_START -> {
                    if (!mExecutable) {
                        mExecutable = true
                        mTransactionManager.executeIfNeeded()
                    }
                }
                else -> {
                }
            }
        }
    }

    init {
        fragmentManager.lifecycle.addObserver(mObserver)
    }

    protected fun notifyDestinationChange(kClass: KClass<out Fragment>) {
        mDestinationChangeListeners.forEach { it.onDestinationChanged(kClass) }
    }

    fun addDestinationChangeListener(function: OnDestinationChangeListener) {
        if (mDestinationChangeListeners.contains(function)) return
        mDestinationChangeListeners.add(function)
    }

    fun removeDestinationChangeListener(function: OnDestinationChangeListener) {
        mDestinationChangeListeners.remove(function)
    }

    abstract fun navigate(
            kClass: KClass<out Fragment>,
            args: Bundle? = null,
            navOptions: NavOptions? = null
    )

    abstract fun navigateUp(): Boolean

    @CallSuper
    protected open fun onSaveInstance(state: Bundle) {
        mExecutable = false
    }

    protected open fun onRestoreInstance(saved: Bundle) {}

    protected fun transaction(function: FragmentTransaction. () -> Unit) {
        mTransactionManager.push(Transaction(function))
    }

    protected fun FragmentTransaction.setNavigateAnim(
        destination: Destination,
        visible: Destination?,
        isStart: Boolean
    ) {
        setCustomAnimations(if (isStart) 0 else destination.animEnter, visible?.animExit ?: 0)
    }

    protected fun FragmentTransaction.setPopupAnim(
        current: Destination,
        previous: Destination?
    ) {
        setCustomAnimations(previous?.animPopEnter ?: 0, current.animPopExit)
    }

    private inner class TransactionManager {
        private val mTransactions = ArrayDeque<Transaction>()
        private val isEmpty get() = mTransactions.isEmpty()
        private val next get() = mTransactions.takeIf { it.isNotEmpty() }?.peekFirst()

        fun push(transaction: Transaction) = synchronized(this) {
            transaction.onFinishListener = {
                mTransactions.pop()
                next?.execute()
            }

            val shouldExecute = isEmpty && mExecutable
            mTransactions.add(transaction)
            if (shouldExecute) transaction.execute()
        }

        fun executeIfNeeded() {
            if (mTransactions.isEmpty()) return
            mTransactions.first.execute()
        }
    }

    private inner class Transaction(private val function: FragmentTransaction.() -> Unit) {
        var onFinishListener: (() -> Unit)? = null

        fun execute() {
            fragmentManager.beginTransaction().also {
                it.function()
                it.runOnCommit { onFinishListener?.invoke() }
                it.commit()
            }
        }
    }

}