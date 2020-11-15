package com.logistic.androidcore.navigation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.logistic.androidcore.R
import com.logistic.androidcore.navigation.singleinstance.ASingleFragment
import com.support.core.navigation.findNavigator

class NavigationSingleActivity : AppCompatActivity(R.layout.activity_navigation_bottom) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) findNavigator().navigate(ASingleFragment::class)
    }

    override fun onBackPressed() {
        if (findNavigator().navigateUp()) return
        super.onBackPressed()
    }
}