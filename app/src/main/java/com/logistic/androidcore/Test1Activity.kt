package com.logistic.androidcore

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_test.*
import kotlin.random.Random

class Test1Activity : AppCompatActivity(R.layout.activity_test) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        btnClose.setOnClickListener {
            MainActivity.event.post(Payload("Hello world ${Random.nextInt()}"))
        }
    }
}

class Payload(val value: String)
