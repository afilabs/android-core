package com.logistic.androidcore

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModel
import com.support.core.base.BaseActivity
import com.support.core.base.viewModel
import com.support.core.functional.SavedStateCreatable
import com.support.core.open
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.random.Random

class TestViewModelSavedStateActivity : BaseActivity(R.layout.activity_main) {
    private val viewModel: SavedStateViewModel by viewModel()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        btnHelloWorld.text = "Test viewModel saved state"

        viewModel.toString()

        btnHelloWorld.setOnClickListener {
            open<MainActivity>()
        }
    }
}

class SavedStateViewModel : ViewModel(), SavedStateCreatable {
    init {
        MemCache.getInstance().test = "Hello ${Random.nextInt()}"
        Log.e("Hash", MemCache.getInstance().test ?: "Unknown")
    }

    override fun onCreate(savedState: Bundle?) {
        val saved = savedState?.getString("Helloworld")
        Log.e("Saved", saved ?: "Initialize")
    }

    override fun onSavedState(): Bundle {
        Log.e("SavedState", "Test")

        return super.onSavedState().apply {
            putString("Helloworld", "Test")
        }
    }
}

class MemCache {
    var test: String? = null

    companion object {
        private var mCache: MemCache? = null
        fun getInstance(): MemCache {
            if (mCache == null) {
                mCache = MemCache()
                Log.e("ReCreate", "MEmCache")
            }
            return mCache!!
        }
    }


}