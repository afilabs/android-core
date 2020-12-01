package com.logistic.androidcore

import android.os.Bundle
import com.logistic.androidcore.navigation.BottomNavigationActivity
import com.logistic.androidcore.navigation.NavigationSingleActivity
import com.support.core.base.BaseActivity
import com.support.core.open
import kotlinx.android.synthetic.main.activity_test_navigation.*

class TestNavigationActivity : BaseActivity(R.layout.activity_test_navigation) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        btnNavBottom.setOnClickListener {
            open<BottomNavigationActivity>()
        }
        btnNavSingleInstance.setOnClickListener {
            open<NavigationSingleActivity>()
        }
    }
}
