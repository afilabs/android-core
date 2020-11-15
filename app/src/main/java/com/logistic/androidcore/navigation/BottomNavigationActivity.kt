package com.logistic.androidcore.navigation

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.logistic.androidcore.R
import com.support.core.navigation.NavigationOwner
import com.support.core.navigation.Navigator
import com.support.core.navigation.findNavigator
import kotlinx.android.synthetic.main.activity_navigation_bottom.*

class BottomNavigationActivity : AppCompatActivity(R.layout.activity_navigation_bottom),
    NavigationOwner {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        mNavigator = FragmentNavigator(supportFragmentManager, containerNavigation.id)
        if (savedInstanceState == null) navigator.navigate(AFragment::class)

        btnA.setOnClickListener {
            navigator.navigate(AFragment::class)
        }

        btnB.setOnClickListener {
            navigator.navigate(BFragment::class)
        }

        btnC.setOnClickListener {
            navigator.navigate(CFragment::class)
        }

        navigator.addDestinationChangeListener {
            Log.e("Destination", it.simpleName ?: "Unk")
        }
    }

    override fun onBackPressed() {
        if (navigator.navigateUp()) return
        super.onBackPressed()
    }

    override val navigator: Navigator
        get() = findNavigator(containerNavigation.id)
}