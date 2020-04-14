package com.support.core.base

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.support.core.ResultLifecycle
import com.support.core.ResultOwner
import com.support.core.ResultRegistry
import com.support.core.functional.FragmentVisibleObserver
import com.support.core.functional.LocalStore
import com.support.core.functional.LocalStoreOwner

abstract class BaseFragment(contentLayoutId: Int) : Fragment(contentLayoutId), ResultOwner,
    LocalStoreOwner {
    private val mResultRegistry = ResultRegistry()
    private val visibleRegistry get() = visibleOwner.lifecycle as VisibleLifecycleRegistry

    override val localStore: LocalStore by lazy(LazyThreadSafetyMode.NONE) { LocalStore() }
    val visibleOwner by lazy(LazyThreadSafetyMode.NONE) { VisibleLifecycleOwner(this) }

    override val resultLife: ResultLifecycle get() = mResultRegistry

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        visibleRegistry.create()
        if (this is FragmentVisibleObserver) visibleRegistry.addObserver(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        visibleRegistry.destroy()
    }

    override fun onStart() {
        super.onStart()
        visibleRegistry.start()
    }

    override fun onStop() {
        super.onStop()
        visibleRegistry.stop()
    }

    override fun onResume() {
        super.onResume()
        visibleRegistry.resume()
    }

    override fun onPause() {
        super.onPause()
        visibleRegistry.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        localStore.clear()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        visibleRegistry.hide(hidden)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mResultRegistry.handleActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        mResultRegistry.handlePermissionsResult(requestCode, permissions, grantResults)
    }

}

class VisibleLifecycleRegistry(provider: LifecycleOwner, private val fragment: Fragment) :
    LifecycleRegistry(provider) {
    companion object {
        private const val STATE_NONE = -1
        private const val STATE_CREATED = 1
        private const val STATE_DESTROYED = 2
        private const val STATE_STARTED = 3
        private const val STATE_STOPPED = 4
        private const val STATE_RESUMED = 5
        private const val STATE_PAUSED = 6

        private val EVENT = hashMapOf(
            STATE_CREATED to Event.ON_CREATE,
            STATE_DESTROYED to Event.ON_DESTROY,
            STATE_STARTED to Event.ON_START,
            STATE_STOPPED to Event.ON_STOP,
            STATE_RESUMED to Event.ON_RESUME,
            STATE_PAUSED to Event.ON_PAUSE
        )
    }

    private var mCurrentState = STATE_NONE

    private val Fragment.isVisibleOnScreen: Boolean
        get() = !isHidden && (parentFragment?.isVisibleOnScreen ?: true)

    private val childVisibleLifecycle
        get() = (fragment.childFragmentManager.fragments
            .find { !it.isHidden } as? BaseFragment)
            ?.visibleOwner?.lifecycle as? VisibleLifecycleRegistry

    fun create() {
        next(STATE_CREATED)
    }

    fun destroy() {
        next(STATE_DESTROYED)
    }

    fun start() {
        if (fragment.isVisibleOnScreen) next(STATE_STARTED)
    }

    fun stop() {
        if (fragment.isVisibleOnScreen) next(STATE_STOPPED)
    }

    fun resume() {
        if (fragment.isVisibleOnScreen) next(STATE_RESUMED)
    }

    fun pause() {
        if (fragment.isVisibleOnScreen) next(STATE_PAUSED)
    }

    fun hide(hidden: Boolean) {
        if (hidden) {
            hidePause()
            hideStop()
        } else {
            showStart()
            showResume()
        }
    }

    private fun hidePause() {
        childVisibleLifecycle?.hidePause()
        next(STATE_PAUSED)

    }

    private fun hideStop() {
        childVisibleLifecycle?.hideStop()
        next(STATE_STOPPED)
    }

    private fun showStart() {
        next(STATE_STARTED)
        childVisibleLifecycle?.showStart()
    }

    private fun showResume() {
        next(STATE_RESUMED)
        childVisibleLifecycle?.showResume()
    }


    private fun next(state: Int): VisibleLifecycleRegistry {
        if (mCurrentState == state) return this
        val event = EVENT[state] ?: error("Not accept state $state")
        Log.i("LifecycleEvent", "${fragment.javaClass.simpleName} - $event")
        handleLifecycleEvent(event)
        mCurrentState = state
        return this
    }

}

class VisibleLifecycleOwner(fragment: Fragment) : LifecycleOwner {
    private val mLifecycle =
        VisibleLifecycleRegistry(this, fragment)

    override fun getLifecycle(): Lifecycle {
        return mLifecycle
    }
}
