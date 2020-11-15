package com.logistic.androidcore.navigation

import android.app.Dialog
import android.content.Context
import androidx.fragment.app.Fragment
import com.logistic.androidcore.R
import com.support.core.navigation.NavOptions
import kotlinx.android.synthetic.main.dialog_option_navigation.*
import kotlin.reflect.KClass

class OptionDialog(context: Context) : Dialog(context) {

    private var mDefaultNavOptions: NavOptions = NavOptions()

    init {
        setContentView(R.layout.dialog_option_navigation)

        btnSetAsDefault.setOnClickListener {
            mDefaultNavOptions = doCreateNavOptions()
        }
    }

    private fun doCreateNavOptions() = NavOptions(
            popupTo = getPopupTo(edtPopupTo.text.toString()),
            inclusive = cbInclusive.isChecked,
            reuseInstance = cbReuseInstance.isChecked,
            singleTask = cbSingleTask.isChecked
    )

    fun createNavOption(): NavOptions? {
        return doCreateNavOptions().also {
            edtPopupTo.setText(when (mDefaultNavOptions.popupTo) {
                AFragment::class -> "A"
                BFragment::class -> "B"
                CFragment::class -> "C"
                else -> ""
            })
            cbInclusive.isChecked = mDefaultNavOptions.inclusive
            cbReuseInstance.isChecked = mDefaultNavOptions.reuseInstance
            cbSingleTask.isChecked = mDefaultNavOptions.singleTask
        }
    }

    private fun getPopupTo(toString: String): KClass<out Fragment>? {
        return when {
            toString.equals("A", true) -> AFragment::class
            toString.equals("B", true) -> BFragment::class
            toString.equals("C", true) -> CFragment::class
            else -> null
        }
    }
}