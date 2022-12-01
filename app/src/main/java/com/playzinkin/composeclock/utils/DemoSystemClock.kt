package com.playzinkin.composeclock.utils

import java.util.Calendar

class DemoSystemClock : SystemClock {
    private val adjustedSpan: Long

    init {
        val curCalendar = Calendar.getInstance()
        curCalendar.set(Calendar.HOUR_OF_DAY, 10)
        curCalendar.set(Calendar.MINUTE, 10)
        curCalendar.set(Calendar.SECOND, 0)
        curCalendar.set(Calendar.MILLISECOND, 0)
        adjustedSpan = curCalendar.time.time - System.currentTimeMillis()
    }

    override fun currentTimeMillis(): Long =
        System.currentTimeMillis() + adjustedSpan

    override fun getHour(): Int =
        Calendar.getInstance().run {
            timeInMillis = currentTimeMillis()
            get(Calendar.HOUR)
        }

}