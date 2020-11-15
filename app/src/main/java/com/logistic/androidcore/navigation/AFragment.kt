package com.logistic.androidcore.navigation

import android.os.Bundle
import android.view.View
import com.logistic.androidcore.R
import com.support.core.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_navigation.*

class AFragment : BaseFragment(R.layout.fragment_navigation) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        txtFragmentName.text = "A Fragment"
    }
}

class BFragment : BaseFragment(R.layout.fragment_navigation) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        txtFragmentName.text = "B Fragment"
    }
}

class CFragment : BaseFragment(R.layout.fragment_navigation) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        txtFragmentName.text = "C Fragment"
    }
}