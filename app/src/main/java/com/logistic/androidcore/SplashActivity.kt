package com.logistic.androidcore

import android.os.Bundle
import com.support.core.base.BaseActivity
import com.support.core.helpers.BroadcastNetworkMonitor
import com.support.core.open
import kotlinx.android.synthetic.main.activity_main.*


class SplashActivity : BaseActivity(R.layout.activity_main) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        btnHelloWorld.setOnClickListener {
            open<MainActivity> { }
        }
        BroadcastNetworkMonitor(this).subscribe(this) {
            btnHelloWorld.text = it.toString()
        }
    }
}