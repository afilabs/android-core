package com.support.core.navigation

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.support.core.extension.safe
import kotlin.reflect.KClass

class Destination(
        val kClass: KClass<out Fragment>,
        val tagId: Long,
        val keepInstance: Boolean = false
) {

    var animPopExit: Int = 0
    var animPopEnter: Int = 0
    var animEnter: Int = 0
    var animExit: Int = 0
    val tag: String = DestinationTag.create(this)

    constructor(kClass: KClass<out Fragment>,
                tagId: Long,
                navOptions: NavOptions?
    ) : this(kClass, tagId, navOptions?.reuseInstance.safe()) {
        if (navOptions != null) {
            animEnter = navOptions.animEnter
            animExit = navOptions.animExit
            animPopEnter = navOptions.animPopEnter
            animPopExit = navOptions.animPopExit
        }
    }

    private val mLog = "${kClass.java.simpleName}:$tagId"

    fun createFragment(): Fragment {
        return kClass.java.getConstructor().newInstance()
    }

    override fun toString(): String {
        return mLog
    }

    fun toBundle(): Bundle {
        return Bundle().also {
            it.putString(CLASS, kClass.java.name)
            it.putLong(TAG_ID, tagId)
            it.putBoolean(KEEP_INSTANCE, keepInstance)
        }
    }

    companion object {
        private const val CLASS = "android:destination:class:name"
        private const val TAG_ID = "android:destination:tagId"
        private const val KEEP_INSTANCE = "android:destination:keep:instance"

        fun of(bundle: Bundle): Destination {
            return Destination(
                    Class.forName(
                            bundle.getString(CLASS)
                                    ?: error("Not found class name")
                    ).asSubclass(Fragment::class.java).kotlin,
                    bundle.getLong(TAG_ID),
                    bundle.getBoolean(KEEP_INSTANCE)
            )
        }
    }
}