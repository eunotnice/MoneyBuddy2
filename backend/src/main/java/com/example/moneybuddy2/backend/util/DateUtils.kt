package com.example.moneybuddy2.backend.util

import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId

object DateUtils {
    private const val DAY_MS = 24L * 60L * 60L * 1000L

    fun lastNDaysRange(now: Long, days: Int): Pair<Long, Long> {
        val end = now
        val start = now - (days * DAY_MS)
        return start to end
    }

    fun monthKeyFromMillis(
        millis: Long,
        zoneId: ZoneId = ZoneId.of("Asia/Kuala_Lumpur")
    ): YearMonth {
        return YearMonth.from(
            Instant.ofEpochMilli(millis).atZone(zoneId).toLocalDate()
        )
    }
}