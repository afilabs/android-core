package com.support.core.event

import android.annotation.SuppressLint
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.support.core.base.BaseFragment

class LocalEvent<T> : Event<T> {
    private val mObservers = hashMapOf<Observer<*>, ObserverWrapper>()
    private var mValue: T? = null

    var version: Int = START_VERSION
        private set

    val value: T? get() = mValue

    @Deprecated("unused", ReplaceWith("post(value)"))
    override fun set(value: T?) {
        post(value)
    }

    fun clearValue() {
        mValue = null
    }

    @SuppressLint("RestrictedApi")
    fun post(value: T? = null) {
        ArchTaskExecutor.getInstance().executeOnMainThread { doPost(value) }
    }

    private fun doPost(value: T?) {
        mValue = value
        version += 1
        notifyChanges()
    }

    private fun notifyChanges() {
        mObservers.values.forEach { it.notifyChange() }
    }

    override fun observe(owner: LifecycleOwner, function: (T?) -> Unit) {
        observe(owner, Observer { function(it) })
    }

    fun observeNotNull(owner: LifecycleOwner, function: (T) -> Unit) {
        observe(owner, Observer { it?.also(function) })
    }

    fun observe(owner: LifecycleOwner, observer: Observer<T>) {
        val index = mObservers.values.asSequence().filterIsInstance<LocalEvent<*>.LifeObserver>()
            .count { it.owner == owner }
        mObservers[observer] = LifeObserver(owner, observer, index).apply { onAttached() }
    }

    fun observeForever(observer: Observer<T>) {
        mObservers[observer] = ForeverObserver(observer).apply { onAttached() }
    }

    fun observeForever(function: (T?) -> Unit) {
        observeForever(Observer { function(it) })
    }

    fun observeForeverNotNull(function: (T) -> Unit) {
        observeForever(Observer { it?.also(function) })
    }

    fun removeObserver(observer: Observer<T>) {
        mObservers.remove(observer)?.onDetached()
    }

    private abstract class ObserverWrapper {
        open fun onAttached() {}
        open fun onDetached() {}
        abstract fun notifyChange()
    }

    private inner class LifeObserver(
        val owner: LifecycleOwner,
        private val observer: Observer<T>,
        private val index: Int
    ) : ObserverWrapper(), LifecycleEventObserver {

        private val lifecycle = when (owner) {
            is BaseFragment -> owner.visibleOwner
            is Fragment -> owner.viewLifecycleOwner
            else -> owner
        }.lifecycle

        private var mSavedState: SavedStateViewModel? = null

        private var mVersion = version
        private var mValue: T? = null

        override fun onAttached() {
            if (owner !is ViewModelStoreOwner) error("owner should be ViewModelStoreOwner")
            mSavedState = owner.viewModelStore
                .getOrCreate(SavedStateViewModel::class.java.simpleName) {
                    SavedStateViewModel()
                }
            restoreState(mSavedState!!)
            lifecycle.addObserver(this)
        }

        @Suppress("UNCHECKED_CAST")
        private fun restoreState(savedState: SavedStateViewModel) {
            val data = savedState[index] ?: return
            mValue = data.value as? T
            mVersion = data.version
        }

        override fun onDetached() {
            mValue = null
            mSavedState = null
            lifecycle.removeObserver(this)
        }

        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if (event == Lifecycle.Event.ON_DESTROY) {
                removeObserver(observer)
            } else if (event == Lifecycle.Event.ON_START) {
                if (mVersion != version) considerNotify()
            }
        }

        override fun notifyChange() {
            mValue = value
            mSavedState?.set(index, SavedData(mValue, mVersion))
            considerNotify()
        }

        private fun considerNotify() {
            if (!lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) return
            observer.onChanged(mValue)

            mValue = null
            mVersion = version

            mSavedState?.set(index, SavedData(mValue, mVersion))
        }
    }

    private inner class ForeverObserver(private val observer: Observer<T>) : ObserverWrapper() {

        override fun notifyChange() {
            observer.onChanged(value)
        }
    }

    class SavedStateViewModel : ViewModel() {
        private val mData = hashMapOf<Int, SavedData>()

        operator fun set(index: Int, data: SavedData) {
            mData[index] = data
        }

        operator fun get(index: Int): SavedData? {
            return mData[index]
        }

        override fun onCleared() {
            super.onCleared()
            mData.clear()
        }
    }

    class SavedData(val value: Any?, val version: Int)

    companion object {
        const val START_VERSION = -1
    }

}