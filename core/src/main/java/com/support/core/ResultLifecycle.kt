package com.support.core

import android.app.Activity
import android.content.Intent

interface ResultOwner {
    val resultLife: ResultLifecycle
}

interface ResultLifecycle {

    /**
     * Registry onActivityResult for fragment or activity
     * It should be registry onCreate or onViewCreated if at fragment
     * and alive until onDestroy (if activity) and onViewDestroyed (if fragment) called
     *
     * @param requestCode Request code to request an activity
     * @param resultCode Result code return from activity finished
     * @param data Result data return from activity finished
     */
    fun onActivityResult(requestCode: Int, callback: (resultCode: Int, data: Intent?) -> Unit)

    /**
     * Registry onActivityResult for fragment or activity just with success result
     * @see onActivityResult for alternate
     */
    fun onActivitySuccessResult(requestCode: Int, callback: (data: Intent?) -> Unit)

    /**
     * Registry onPermissionsResult for fragment or activity
     * It should be registry onCreate or onViewCreated if at fragment
     * and alive until onDestroy (if activity) and onViewDestroyed (if fragment) called
     * @param requestCode Request code of request
     */
    fun onPermissionsResult(requestCode: Int, callback: (permissions: Array<out String>, grantResults: IntArray) -> Unit)
}

class ResultRegistry : ResultLifecycle {

    private var mPendingActivityResult: ActivityResult? = null
    private var mPendingPermissionResult: PermissionResult? = null
    private val mPermissionCallbacks = hashMapOf<Int, (Array<out String>, IntArray) -> Unit>()
    private val mActivityResultCallbacks = hashMapOf<Int, ActivityResultCallback>()

    fun clear() {
        mPendingActivityResult = null
        mPendingPermissionResult = null

        mActivityResultCallbacks.clear()
        mPermissionCallbacks.clear()
    }

    override fun onPermissionsResult(requestCode: Int, callback: (permissions: Array<out String>, grantResults: IntArray) -> Unit) {
        mPermissionCallbacks[requestCode] = callback
        val result = mPendingPermissionResult ?: return
        if (result.requestCode != requestCode) return
        callback(result.permissions, result.grantResults)
        mPendingPermissionResult = null
    }

    override fun onActivitySuccessResult(requestCode: Int, callback: (data: Intent?) -> Unit) {
        onActivityResult(requestCode) { resultCode, data ->
            if (resultCode == Activity.RESULT_OK) {
                callback(data)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, callback: (resultCode: Int, data: Intent?) -> Unit) {
        mActivityResultCallbacks[requestCode] = callback
        val result = mPendingActivityResult ?: return
        if (result.requestCode != requestCode) return
        callback(result.resultCode, result.data)
        mPendingActivityResult = null
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val callback = mActivityResultCallbacks[requestCode]
        if (callback != null) {
            callback(resultCode, data)
            mPendingActivityResult = null
            return
        }
        mPendingActivityResult = ActivityResult(requestCode, resultCode, data)
    }

    fun handlePermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        val callback = mPermissionCallbacks[requestCode]
        if (callback != null) {
            callback(permissions, grantResults)
            mPendingPermissionResult = null
            return
        }
        mPendingPermissionResult = PermissionResult(requestCode, permissions, grantResults)
    }

    private data class ActivityResult(
            val requestCode: Int,
            val resultCode: Int,
            val data: Intent?
    )

    private class PermissionResult(
            val requestCode: Int,
            val permissions: Array<out String>,
            val grantResults: IntArray
    )
}

typealias ActivityResultCallback = (resultCode: Int, data: Intent?) -> Unit
