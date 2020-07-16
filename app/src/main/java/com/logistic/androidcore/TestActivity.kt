package com.logistic.androidcore

import android.os.Bundle
import com.support.core.base.BaseActivity
import com.support.core.open
import kotlinx.android.synthetic.main.activity_test.*

class TestActivity : BaseActivity(R.layout.activity_test) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MainActivity.event.observeNotNull(this) {
            btnClose.text = it.value
        }
        btnClose.setOnClickListener {
            open<Test1Activity> { }
        }
    }
}
