package com.logistic.androidcore.navigation

import android.os.Bundle
import android.view.View
import com.logistic.androidcore.R
import com.support.core.base.BaseFragment
import com.support.core.navigation.findNavigator
import kotlinx.android.synthetic.main.fragment_navigation.*

abstract class NavigationFragment : BaseFragment(R.layout.fragment_navigation), View.OnClickListener {
    abstract val text: String
    val navigator get() = findNavigator()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        txtFragmentName.text = text
        txtFragmentName.setOnClickListener(this)
    }

    override fun onClick(v: View?) {

    }
}