package com.support.core

import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.support.core.extension.safe

interface PermissionResultRegistry {
    fun handlePermissionResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    )

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

    fun clear()
}

interface PermissionAccessible {
    fun check(
        vararg permissions: String,
        options: PermissionSettingOptions? = null,
        onPermission: (Boolean) -> Unit
    ): PermissionRequest

    fun access(
        vararg permissions: String,
        options: PermissionSettingOptions? = null,
        onPermission: () -> Unit
    ): PermissionRequest

    fun checkAny(
        vararg permissions: String,
        options: PermissionSettingOptions? = null,
        onPermission: (Boolean) -> Unit
    ): PermissionRequest

    fun accessAny(
        vararg permissions: String,
        options: PermissionSettingOptions? = null,
        onPermission: () -> Unit
    ): PermissionRequest

    fun forceAccess(
        vararg permissions: String,
        options: PermissionSettingOptions? = null,
        onPermission: () -> Unit
    ): PermissionRequest

    fun forceAccessAny(
        vararg permissions: String,
        options: PermissionSettingOptions? = null,
        onPermission: () -> Unit
    ): PermissionRequest

}

class PermissionSettingOptions(
    val titleDenied: String = "Permission denied",
    val messageDenied: String = "You need to allow permission to use this feature",
    val positive: String = "Ok"
)

class PermissionAccessibleImpl : PermissionResultRegistry, PermissionAccessible {

    private val mDispatcher: PermissionDispatcher

    constructor(activity: FragmentActivity) {
        mDispatcher = ActivityDispatcher(activity)
    }

    constructor(fragment: Fragment) {
        mDispatcher = FragmentDispatcher(fragment)
    }

    override fun handlePermissionResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        mDispatcher.handlePermissionResult(requestCode, permissions, grantResults)
    }

    override fun handleActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        mDispatcher.handleActivityResult(requestCode, resultCode, data)
    }

    override fun check(
        vararg permissions: String,
        options: PermissionSettingOptions?,
        onPermission: (Boolean) -> Unit
    ): PermissionRequest {
        return PermissionRequestImpl(
            *permissions,
            options = options,
            dispatcher = mDispatcher,
            onPermission = { onPermission(it) }
        )
    }

    override fun access(
        vararg permissions: String,
        options: PermissionSettingOptions?,
        onPermission: () -> Unit
    ): PermissionRequest {
        return PermissionRequestImpl(
            *permissions,
            options = options,
            dispatcher = mDispatcher,
            onPermission = {
                if (it) onPermission()
            })
    }

    override fun checkAny(
        vararg permissions: String,
        options: PermissionSettingOptions?,
        onPermission: (Boolean) -> Unit
    ): PermissionRequest {
        return PermissionRequestImpl(
            *permissions,
            options = options,
            dispatcher = mDispatcher,
            onPermission = { onPermission(it) }
        )
    }

    override fun accessAny(
        vararg permissions: String,
        options: PermissionSettingOptions?,
        onPermission: () -> Unit
    ): PermissionRequest {
        return PermissionRequestAnyImpl(
            *permissions,
            options = options,
            dispatcher = mDispatcher,
            onPermission = {
                if (it) onPermission()
            }
        )
    }

    override fun forceAccess(
        vararg permissions: String,
        options: PermissionSettingOptions?,
        onPermission: () -> Unit
    ): PermissionRequest {
        return PermissionRequestImpl(
            *permissions,
            options = options,
            dispatcher = mDispatcher,
            onPermission = {
                if (!it) request()
                else onPermission()
            }
        )
    }

    override fun forceAccessAny(
        vararg permissions: String,
        options: PermissionSettingOptions?,
        onPermission: () -> Unit
    ): PermissionRequest {
        return PermissionRequestAnyImpl(
            *permissions,
            options = options,
            dispatcher = mDispatcher,
            onPermission = {
                if (!it) request()
                else onPermission()
            }
        )
    }

    override fun clear() {
        mDispatcher.clear()
    }
}

abstract class PermissionDispatcher {
    abstract val activity: FragmentActivity
    private var mRechecked = hashMapOf<String, Int>()
    private var mOnPermissionResults = hashMapOf<Int, (Array<out String>, IntArray) -> Unit>()
    private var mActivityResults = hashMapOf<Int, (Int, Intent?) -> Unit>()

    val packageName: String get() = activity.packageName

    abstract fun startActivityForResult(intent: Intent, requestCode: Int)
    abstract fun requestPermission(permissions: Array<out String>, requestCode: Int)

    fun onPermissionResult(code: Int, callback: (Array<out String>, IntArray) -> Unit) {
        mOnPermissionResults[code] = callback
    }

    fun onActivityResult(code: Int, callback: (Int, Intent?) -> Unit) {
        mActivityResults[code] = callback
    }

    fun handlePermissionResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        mOnPermissionResults[requestCode]?.also {
            it(permissions, grantResults)
        }
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        mActivityResults[requestCode]?.also {
            it(resultCode, data)
        }
    }

    fun clear() {
        mOnPermissionResults.clear()
        mActivityResults.clear()
    }

    fun increaseRecheck(permissions: Array<out String>) {
        val key = permissions[0]
        mRechecked[key] = mRechecked[key].safe() + 1
    }

    fun clearRechecked(permissions: Array<out String>) {
        mRechecked.remove(permissions[0])
    }

    fun hasRecheck(permissions: Array<out String>): Boolean {
        return mRechecked[permissions[0]].safe() > 1
    }
}

private class ActivityDispatcher(override val activity: FragmentActivity) : PermissionDispatcher() {

    override fun startActivityForResult(intent: Intent, requestCode: Int) {
        activity.startActivityForResult(intent, requestCode)
    }

    override fun requestPermission(permissions: Array<out String>, requestCode: Int) {
        ActivityCompat.requestPermissions(activity, permissions, requestCode)
    }
}

private class FragmentDispatcher(val fragment: Fragment) : PermissionDispatcher() {
    override val activity: FragmentActivity
        get() = fragment.requireActivity()

    override fun startActivityForResult(intent: Intent, requestCode: Int) {
        fragment.startActivityForResult(intent, requestCode)
    }

    override fun requestPermission(permissions: Array<out String>, requestCode: Int) {
        fragment.requestPermissions(permissions, requestCode)
    }
}

abstract class PermissionRequest(
    vararg permission: String,
    private val options: PermissionSettingOptions?,
    protected val dispatcher: PermissionDispatcher,
    protected val onPermission: PermissionRequest.(Boolean) -> Unit
) : View.OnClickListener {
    private var mOpenSettingDialog: AlertDialog? = null
    val permissions = permission
    private val requestCode = requestCodeOf(permissions)
    abstract val checkAll: Boolean

    init {
        dispatcher.onPermissionResult(requestCode) { _, grantResults ->
            if (grantResults.isEmpty()) {
                onPermission(false)
                return@onPermissionResult
            }

            val isAllowed =
                if (checkAll) grantResults.all { it == PackageManager.PERMISSION_GRANTED }
                else grantResults.any { it == PackageManager.PERMISSION_GRANTED }

            onPermission(isAllowed)
            if (isAllowed) dispatcher.clearRechecked(permissions)
        }

        dispatcher.onActivityResult(requestCode) { _, _ ->
            val isAllowed = if (checkAll) isAllAllowed(*permissions) else isAnyAllowed(*permissions)
            onPermission(isAllowed)
            if (isAllowed) dispatcher.clearRechecked(permissions)
        }
    }

    abstract fun request()

    override fun onClick(v: View?) {
        request()
    }

    protected fun checkOrShowSetting() {
        if (shouldShowSettings(permissions)) {
            showSuggestOpenSetting()
        } else {
            dispatcher.requestPermission(permissions, requestCode)
            dispatcher.increaseRecheck(permissions)
        }
    }

    private fun shouldShowSettings(permissions: Array<out String>): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(
            dispatcher.activity,
            permissions.first()
        ) || dispatcher.hasRecheck(permissions)
    }

    private fun requestCodeOf(permissions: Array<out String>): Int {
        val prime = 31
        var result = 1
        for (s in permissions) {
            result = result * prime + s.hashCode()
        }
        return (result and 0xffff)
    }

    protected fun isAnyAllowed(vararg permissions: String): Boolean {
        return permissions.any {
            ContextCompat.checkSelfPermission(
                dispatcher.activity,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    protected fun isAllAllowed(vararg permissions: String): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(
                dispatcher.activity,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun showSuggestOpenSetting() {
        if (mOpenSettingDialog == null) {
            val settingOptions = options ?: PermissionSettingOptions()
            mOpenSettingDialog = AlertDialog.Builder(dispatcher.activity)
                .setTitle(settingOptions.titleDenied)
                .setMessage(settingOptions.messageDenied)
                .setPositiveButton(settingOptions.positive) { _: DialogInterface, _: Int ->
                    openSetting()
                }
                .create()
        }
        mOpenSettingDialog!!.show()
    }

    private fun openSetting() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:" + dispatcher.packageName)
        )
        dispatcher.startActivityForResult(intent, requestCode)
    }
}

class PermissionRequestImpl(
    vararg permission: String,
    options: PermissionSettingOptions?,
    dispatcher: PermissionDispatcher,
    onPermission: PermissionRequest.(Boolean) -> Unit
) : PermissionRequest(
    *permission,
    options = options,
    dispatcher = dispatcher,
    onPermission = onPermission
) {
    override val checkAll: Boolean = false

    override fun request() {
        if (permissions.isEmpty()) throw RuntimeException("No permission to check")

        if (isAllAllowed(*permissions)) {
            onPermission(true)
            return
        }

        checkOrShowSetting()
    }
}

class PermissionRequestAnyImpl(
    vararg permission: String,
    options: PermissionSettingOptions?,
    dispatcher: PermissionDispatcher,
    onPermission: PermissionRequest.(Boolean) -> Unit
) : PermissionRequest(
    *permission,
    options = options,
    dispatcher = dispatcher,
    onPermission = onPermission
) {
    override val checkAll: Boolean = true

    override fun request() {
        if (permissions.isEmpty()) throw RuntimeException("No permission to check")

        if (isAllAllowed(*permissions)) {
            onPermission(true)
            return
        }

        if (isAnyAllowed(*permissions)) {
            if (dispatcher.hasRecheck(permissions)) {
                onPermission(true)
                return
            }

            if (permissions.any {
                    !ActivityCompat.shouldShowRequestPermissionRationale(dispatcher.activity, it)
                }) {
                onPermission(true)
                return
            }

        }

        checkOrShowSetting()
    }
}