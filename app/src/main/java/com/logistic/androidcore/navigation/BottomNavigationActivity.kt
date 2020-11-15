package com.logistic.androidcore.navigation

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.logistic.androidcore.R
import com.support.core.navigation.NavOptions
import com.support.core.navigation.NavigationOwner
import com.support.core.navigation.Navigator
import com.support.core.navigation.findNavigator
import kotlinx.android.synthetic.main.activity_navigation_bottom.*

class BottomNavigationActivity : AppCompatActivity(R.layout.activity_navigation_bottom),
        NavigationOwner {
    private lateinit var mOptionDialog: OptionDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) navigator.navigate(AFragment::class)

        mOptionDialog = OptionDialog(this)

        btnOption.setOnClickListener {
            mOptionDialog.show()
        }

        btnA.setOnClickListener {
            navigator.navigate(AFragment::class, navOptions = createNavOptions())
        }

        btnB.setOnClickListener {
            navigator.navigate(BFragment::class, navOptions = createNavOptions())
        }

        btnC.setOnClickListener {
            navigator.navigate(CFragment::class, navOptions = createNavOptions())
        }

        navigator.addDestinationChangeListener { it, log ->
            Log.e("Destination", it.simpleName ?: "Unk")
            txtInfo.text = log
        }
    }

    private fun createNavOptions(): NavOptions? {
        return mOptionDialog.createNavOption()
    }

    override fun onBackPressed() {
        if (navigator.navigateUp()) return
        super.onBackPressed()
    }

    override val navigator: Navigator
        get() = findNavigator(containerNavigation.id)
}