package com.support.core.base

import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistryOwner
import com.support.core.AppExecutors
import com.support.core.ConcurrentContext
import com.support.core.ConcurrentScope
import com.support.core.event.LoadingEvent
import com.support.core.event.PostAble
import com.support.core.event.SingleLiveEvent
import com.support.core.extension.LoadCacheLiveData
import com.support.core.extension.doAsync
import com.support.core.extension.lazyNone
import com.support.core.extension.post
import com.support.core.factory.SavableViewModelFactory
import com.support.core.factory.ViewModelFactory
import com.support.core.functional.Form
import com.support.core.functional.LocalStoreOwner
import com.support.core.functional.SavedStateCreatable
import com.support.core.isOnMainThread

abstract class BaseViewModel : ViewModel() {
    private val mConcurrent = ConcurrentContext()

    val refresh = MutableLiveData<Any>()
    val error = SingleLiveEvent<Throwable>()
    val loading = LoadingEvent()
    val viewLoading = LoadingEvent()

    override fun onCleared() {
        super.onCleared()
        mConcurrent.cancel()
    }

    fun <T, V> LiveData<T>.async(
        loadingEvent: LoadingEvent? = loading,
        errorEvent: SingleLiveEvent<Throwable>? = error,
        function: ConcurrentScope.(T) -> V?
    ): LiveData<V> {
        val next = MediatorLiveData<V>()
        next.addSource(this) {
            next.doAsync(it, mConcurrent, loadingEvent, errorEvent, function)
        }
        return next
    }

    fun <T, V> LoadCacheLiveData<T, V>.orAsync(
        loadingEvent: LoadingEvent? = loading,
        errorEvent: SingleLiveEvent<Throwable>? = error,
        function: ConcurrentScope.(T) -> V?
    ): LiveData<V> {
        val next = MediatorLiveData<V>()
        next.addSource(this) {
            if (it.second != null) {
                next.value = it.second
                return@addSource
            }
            next.doAsync(it.first, mConcurrent, loadingEvent, errorEvent, function)
        }
        return next
    }

    fun <T, V> LoadCacheLiveData<T, V>.thenAsync(
        loadingEvent: LoadingEvent? = loading,
        errorEvent: SingleLiveEvent<Throwable>? = error,
        function: ConcurrentScope.(T) -> V?
    ): LiveData<V> {
        val next = MediatorLiveData<V>()
        next.addSource(this) {
            if (it.second != null) next.value = it.second
            next.doAsync(it.first, mConcurrent, loadingEvent, errorEvent, function)
        }
        return next
    }

    fun async(
        loadingEvent: LoadingEvent? = loading,
        errorEvent: PostAble<Throwable>? = error,
        function: ConcurrentScope.() -> Unit
    ) {
        loadingEvent?.post(true)

        mConcurrent.launch {
            try {
                function()
            } catch (t: Throwable) {
                errorEvent?.postValue(t)
                t.printStackTrace()
            } finally {
                loadingEvent?.postValue(false)
            }
        }
    }

    fun diskIO(showError: Boolean = false, function: () -> Unit) {
        val callable = {
            try {
                function()
            } catch (t: Throwable) {
                t.printStackTrace()
                if (showError) error.postValue(t)
            }
        }

        if (isOnMainThread) AppExecutors.diskIO.execute(callable)
        else callable()
    }

    fun <T> LiveData<T>.validate(function: (T) -> Unit): LiveData<T> {
        val next = MediatorLiveData<T>()
        next.addSource(this) {
            try {
                function(it)
                next.value = it
            } catch (t: Throwable) {
                error.value = t
            }
        }
        return next
    }

    fun validate(form: Form): Any? = validate(form::validate)

    fun validate(function: () -> Unit): Any? {
        return try {
            function()
            null
        } catch (t: Throwable) {
            error.value = t
            this
        }
    }
}

interface ViewModelRegistrable : LocalStoreOwner {

    @CallSuper
    fun registry(viewModel: BaseViewModel) {
        var viewModelId = "registry:view:model:${viewModel.javaClass.name}"
        if (this is ViewModelStoreOwner) viewModelId =
            "$viewModelId:shared:${viewModel.isShared(this)}"

        if (!localStore.get(viewModelId) { false }) {
            onRegistryViewModel(viewModel)
            localStore[viewModelId] = true
        }
    }

    fun onRegistryViewModel(viewModel: BaseViewModel)
}

class EmptyViewModel : BaseViewModel()

private fun <T : ViewModel> ViewModelStoreOwner.getOrCreateViewModel(viewModelClass: Class<T>): T {
    val factory = if (SavedStateCreatable::class.java.isAssignableFrom(viewModelClass)
        && this is SavedStateRegistryOwner
    ) SavableViewModelFactory(this)
    else ViewModelFactory()

    return ViewModelProvider(this, factory).get(viewModelClass).also {
        if (it is BaseViewModel && this is ViewModelRegistrable) registry(it)
    }
}

fun <T : ViewModel> ViewModelStoreOwner.getViewModel(viewModelClass: Class<T>): T {
    return if (this is LocalStoreOwner) localStore.get("vm:${javaClass.simpleName}:${this.javaClass.simpleName}") {
        getOrCreateViewModel(viewModelClass)
    } else getOrCreateViewModel(viewModelClass)
}

@Deprecated("unused", ReplaceWith("getViewModel()"))
inline fun <reified T : ViewModel> LocalStoreOwner.getViewModel(owner: ViewModelStoreOwner): T {
    return localStore.get("vm:${javaClass.simpleName}:${owner.javaClass.simpleName}") {
        owner.getViewModel()
    }
}

inline fun <reified T : ViewModel> ViewModelStoreOwner.getViewModel(): T {
    return getViewModel(T::class.java)
}

inline fun <reified T : ViewModel> AppCompatActivity.viewModel(): Lazy<T> =
    lazyNone { getViewModel() }

inline fun <reified T : ViewModel> Fragment.viewModel(): Lazy<T> =
    lazyNone { getViewModel() }

inline fun <reified T : ViewModel> Fragment.shareViewModel(): Lazy<T> =
    lazyNone { requireActivity().getViewModel() }

inline fun <reified T : ViewModel> viewModel(crossinline function: () -> ViewModelStoreOwner): Lazy<T> =
    lazyNone { function().getViewModel() }

