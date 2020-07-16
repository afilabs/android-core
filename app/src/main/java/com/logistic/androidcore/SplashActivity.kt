package com.logistic.androidcore

import android.os.Bundle
import android.util.Log
import com.support.core.base.BaseActivity
import com.support.core.open
import kotlinx.android.synthetic.main.activity_main.*


class SplashActivity : BaseActivity(R.layout.activity_main) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("File", "${MainActivity.event.value}")
        btnHelloWorld.setOnClickListener {
            open<MainActivity> { }
        }
    }
}