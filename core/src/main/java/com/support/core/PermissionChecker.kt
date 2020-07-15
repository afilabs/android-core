@file:Suppress("UNUSED")

package com.support.core

import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.support.core.base.BaseActivity
import com.support.core.extension.safe

class PermissionOptions(
    val title: String = "Permission denied",
    val message: String = "You need to allow permission to use this feature",
    val positive: String = "Ok",
    val cancelable: Boolean = true
)

class PermissionChecker(private val activity: BaseActivity) {

    private var mRechecked = hashMapOf<Int, Int>()

    fun access(
        vararg permissions: String,
        options: PermissionOptions = PermissionOptions(),
        onAccess: () -> Unit
    ) {
        check(*permissions, options = options) { if (it) onAccess() }
    }

    fun forceAccess(
        vararg permissions: String,
        options: PermissionOptions = PermissionOptions(),
        onAccess: () -> Unit
    ) {
        check(*permissions, options = options) {
            if (!it) forceAccess(*permissions, options = options, onAccess = onAccess)
            else onAccess()
        }
    }

    fun check(
        vararg permissions: String,
        options: PermissionOptions = PermissionOptions(),
        onPermission: (Boolean) -> Unit
    ) {
        if (permissions.isEmpty()) throw RuntimeException("No permission to check")

        if (isAllAllowed(*permissions)) {
            onPermission(true)
            return
        }

        checkOrShowSetting(permissions, true, options = options, onPermission = onPermission)
    }

    fun checkAny(
        vararg permissions: String,
        options: PermissionOptions = PermissionOptions(),
        onPermission: (Boolean) -> Unit
    ) {
        if (permissions.isEmpty()) throw RuntimeException("No permission to check")

        if (isAllAllowed(*permissions)) {
            onPermission(true)
            return
        }

        if (isAnyAllowed(*permissions)) {
            if (hasRecheck(permissions)) {
                onPermission(true)
                return
            }

            if (permissions.any {
                    !ActivityCompat.shouldShowRequestPermissionRationale(activity, it)
                }) {
                onPermission(true)
                return
            }

        }

        checkOrShowSetting(permissions, false, options = options, onPermission = onPermission)
    }

    private fun checkOrShowSetting(
        permissions: Array<out String>,
        checkAll: Boolean,
        options: PermissionOptions,
        onPermission: (Boolean) -> Unit
    ) {
        if (shouldShowSettings(permissions)) {
            showSuggestOpenSetting(
                permissions,
                checkAll,
                options = options,
                onPermission = onPermission
            )
        } else {
            request(permissions, checkAll, onPermission)
            val key = requestCodeOf(permissions)
            mRechecked[key] = mRechecked[key].safe() + 1
        }
    }

    private fun shouldShowSettings(permissions: Array<out String>): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permissions.first())
                || hasRecheck(permissions)
    }

    private fun clearRechecked(permissions: Array<out String>) {
        mRechecked.remove(requestCodeOf(permissions))
    }

    private fun hasRecheck(permissions: Array<out String>): Boolean {
        return mRechecked[requestCodeOf(permissions)].safe() > 1
    }

    private fun request(
        permissions: Array<out String>,
        checkAll: Boolean,
        onPermission: (Boolean) -> Unit
    ) {
        val requestCode = requestCodeOf(permissions)
        activity.resultLife.onPermissionsResult(requestCode) { _, grantResults ->
            if (grantResults.isEmpty()) {
                onPermission(false)
                return@onPermissionsResult
            }

            val isAllowed =
                if (checkAll) grantResults.all { it == PackageManager.PERMISSION_GRANTED }
                else grantResults.any { it == PackageManager.PERMISSION_GRANTED }

            onPermission(isAllowed)
            if (isAllowed) clearRechecked(permissions)
        }
        ActivityCompat.requestPermissions(activity, permissions, requestCode)
    }

    private fun requestCodeOf(permissions: Array<out String>): Int {
        val prime = 31
        var result = 1
        for (s in permissions) {
            result = result * prime + s.hashCode()
        }
        return (result and 0xffff)
    }

    private fun isAnyAllowed(vararg permissions: String): Boolean {
        return permissions.any {
            ContextCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun isAllAllowed(vararg permissions: String): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun showSuggestOpenSetting(
        permissions: Array<out String>,
        checkAll: Boolean,
        options: PermissionOptions,
        onPermission: (Boolean) -> Unit
    ) {
        AlertDialog.Builder(activity)
            .setTitle(options.title)
            .setMessage(options.message)
            .setCancelable(options.cancelable)
            .setPositiveButton(options.positive) { _: DialogInterface, _: Int ->
                openSetting(permissions, checkAll, onPermission)
            }
            .create().show()
    }

    private fun openSetting(
        permissions: Array<out String>,
        checkAll: Boolean,
        onPermission: (Boolean) -> Unit
    ) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:" + activity.packageName)
        )
        val requestCode = requestCodeOf(permissions)
        activity.resultLife.onInstantResult(requestCode) { _, _ ->
            val isAllowed = if (checkAll) isAllAllowed(*permissions) else isAnyAllowed(*permissions)
            onPermission(isAllowed)
            if (isAllowed) clearRechecked(permissions)
        }
        activity.startActivityForResult(intent, requestCode)
    }
}
