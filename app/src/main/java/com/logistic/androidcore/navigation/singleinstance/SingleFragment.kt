package com.logistic.androidcore.navigation.singleinstance

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import com.logistic.androidcore.navigation.NavigationFragment
import com.support.core.extension.safe
import com.support.core.navigation.ArgumentChangeable
import com.support.core.navigation.NavOptions
import kotlinx.android.synthetic.main.fragment_navigation.*


class ASingleFragment : NavigationFragment() {
    override val text: String
        get() = "This is A, click to go to B"

    override fun onClick(v: View?) {
        navigator.navigate(BSingleFragment::class)
    }
}

class BSingleFragment : NavigationFragment() {
    override val text: String
        get() = "This is B, click to go to C ${System.currentTimeMillis()}"

    override fun onClick(v: View?) {
        navigator.navigate(CSingleFragment::class)
    }
}

class CSingleFragment : NavigationFragment() {
    override val text: String
        get() = "This is C, click to go to D"

    override fun onClick(v: View?) {
        navigator.navigate(DSingleFragment::class)
    }
}

class DSingleFragment : NavigationFragment(), ArgumentChangeable {
    override val text: String
        get() = "This is D, click to Re-up"

    override fun onClick(v: View?) {
        navigator.navigate(ASingleFragment::class, navOptions = NavOptions(
                popupTo = ASingleFragment::class,
                singleTask = true,
                inclusive = false
        ),args = bundleOf("key" to "Hello world ${System.currentTimeMillis()}"))
    }

    @SuppressLint("SetTextI18n")
    override fun onNewArguments(arguments: Bundle) {
        txtFragmentName.text = "$text ${arguments.getString("key", "").safe()}"
    }
}

class ESingleFragment : NavigationFragment(), ArgumentChangeable {
    override val text: String
        get() = "This is E, click to Re-up"

    override fun onClick(v: View?) {
        navigator.navigate(ESingleFragment::class, navOptions = NavOptions(
                popupTo = DSingleFragment::class,
                singleTask = true,
                inclusive = true
        ),args = bundleOf("key" to "Hello world ${System.currentTimeMillis()}"))
    }

    @SuppressLint("SetTextI18n")
    override fun onNewArguments(arguments: Bundle) {
        txtFragmentName.text = "$text ${arguments.getString("key", "").safe()}"
    }
}