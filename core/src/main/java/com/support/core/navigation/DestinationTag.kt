package com.support.core.navigation

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import kotlin.reflect.KClass

object DestinationTag {
    private val String.tagId: Long?
        get() = split(":").lastOrNull()?.toLong()

    private val String.isSingle: Boolean
        get() = split(":").let { it[it.size - 2] == "single" }

    fun create(destination: Destination): String {
        return with(destination) {
            val keep = if (keepInstance) "single" else "new"
            "navigator:destination:${kClass.java.simpleName}:tag:$keep:$tagId"
        }
    }

    fun findTagId(fragmentManager: FragmentManager, kClass: KClass<out Fragment>): Long? {
        return fragmentManager.fragments.findLast { (it.javaClass == kClass.java) && it.tag?.isSingle == true }?.tag?.tagId
    }
}