package com.support.core.navigation

import android.os.Bundle
import android.os.Parcelable
import java.io.Serializable

interface FragmentArgs : Serializable {
    fun toBundle(): Bundle = Bundle().also {
        it.putSerializable(KEY, this)
    }

    companion object {
        const val KEY = "com:support:core:navigation:arguments"

        @Suppress("UNCHECKED_CAST")
        inline operator fun <reified T : Serializable> get(bundle: Bundle?): T {
            return (bundle?.getSerializable(KEY) as? T)
                    ?: error("Can not to cast to ${T::class.java.simpleName}")
        }

        @Suppress("UNCHECKED_CAST")
        inline operator fun <reified T : Parcelable> invoke(bundle: Bundle?): T {
            return (bundle?.getSerializable(KEY) as? T)
                    ?: error("Can not to cast to ${T::class.java.simpleName}")
        }
    }
}