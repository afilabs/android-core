package com.logistic.androidcore

import android.os.Bundle
import android.view.View
import com.support.core.base.BaseActivity
import com.support.core.base.BaseViewModel
import com.support.core.open
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.random.Random


class SplashActivity : BaseActivity(R.layout.activity_main) {
    val viewModel = SplashViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        btnHelloWorld.setOnClickListener {
            open<MainActivity> { }
        }
//        BroadcastNetworkMonitor(this).subscribe(this) {
//            btnHelloWorld.text = it.toString()
//        }
//        btnHelloWorld.setOnClickListener(TestAsyncTimeoutAction())
    }

    inner class TestAsyncTimeoutAction : View.OnClickListener {
        override fun onClick(v: View?) {
            viewModel.async {
                val data = task {
                    Thread.sleep(3000)
                    "Hello world Test ${Random.nextInt()}"
                }.await(2000)
                runOnUiThread {
                    btnHelloWorld2.text = data
                }
            }
        }
    }

}

class SplashViewModel : BaseViewModel()