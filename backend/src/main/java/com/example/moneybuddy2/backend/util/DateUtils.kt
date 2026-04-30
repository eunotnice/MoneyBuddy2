package com.example.moneybuddy2.backend.util

import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {
    private const val DAY_MS = 24L * 60L * 60L * 1000L

    fun monthKeyFromMillis(
        millis: Long,
        zoneId: ZoneId = ZoneId.of("Asia/Kuala_Lumpur")
    ): YearMonth {
        return YearMonth.from(
            Instant.ofEpochMilli(millis).atZone(zoneId).toLocalDate()
        )
    }

    fun currentMonthRange(nowMillis: Long): Pair<Long, Long> {
        val zone = ZoneId.of("Asia/Kuala_Lumpur")
        val now = Instant.ofEpochMilli(nowMillis).atZone(zone)

        val start = now.withDayOfMonth(1)
            .toLocalDate()
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()

        return start to nowMillis
    }

    fun currentMonthLabel(nowMillis: Long): String {
        val zone = ZoneId.of("Asia/Kuala_Lumpur")
        val now = Instant.ofEpochMilli(nowMillis).atZone(zone)

        return now.format(
            DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)
        )
    }


}