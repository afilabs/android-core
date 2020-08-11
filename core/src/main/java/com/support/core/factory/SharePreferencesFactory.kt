package com.support.core.factory

import android.content.Context
import android.content.SharedPreferences

interface SharePreferencesFactory {
    fun create(context: Context): SharedPreferences
}

class DefaultSharePreferencesFactory : SharePreferencesFactory {
    override fun create(context: Context): SharedPreferences {
        return context.getSharedPreferences("logistics:cache", Context.MODE_PRIVATE)
    }
}