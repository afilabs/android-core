package com.support.core

import com.support.core.date.ZoneDate
import org.junit.Test

import org.junit.Assert.*
import java.util.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testZoneDate() {
        val date = ZoneDate.fromDevice(Date(), "Asia/Singapore")
        println(date.toString())
        println(date.deviceString)
    }
}
