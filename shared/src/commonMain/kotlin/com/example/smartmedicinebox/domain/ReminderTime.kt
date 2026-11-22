package com.example.smartmedicinebox.domain

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

/** Time helpers shared by Android and iPhone (no java.util.Calendar). */
object ReminderTime {

    private val format = Regex("^([01]\\d|2[0-3]):[0-5]\\d$")

    /** True for a valid "HH:mm" time, e.g. "08:00". */
    fun isValid(time: String): Boolean = format.matches(time)

    /** "08:30" -> (8, 30), or null if invalid. */
    fun parse(time: String): Pair<Int, Int>? {
        if (!isValid(time)) return null
        val (hour, minute) = time.split(":").map { it.toInt() }
        return hour to minute
    }

    /** Next moment (epoch millis) the daily reminder "HH:mm" fires after [nowMillis]. */
    fun nextOccurrenceMillis(time: String, nowMillis: Long): Long? {
        val (hour, minute) = parse(time) ?: return null
        val zone = TimeZone.currentSystemDefault()
        val today = Instant.fromEpochMilliseconds(nowMillis).toLocalDateTime(zone).date
        val at = LocalTime(hour, minute)

        val todayMillis = LocalDateTime(today, at).toInstant(zone).toEpochMilliseconds()
        if (todayMillis > nowMillis) return todayMillis

        val tomorrow = today.plus(1, DateTimeUnit.DAY)
        return LocalDateTime(tomorrow, at).toInstant(zone).toEpochMilliseconds()
    }
}

/** Display formats used by the screens. */
object DateFormats {

    private fun Int.pad2() = toString().padStart(2, '0')
    private fun Long.pad2() = toString().padStart(2, '0')

    private fun local(millis: Long) =
        Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.currentSystemDefault())

    /** dd/MM/yyyy */
    fun date(millis: Long): String {
        val d = local(millis).date
        return "${d.day.pad2()}/${d.month.number.pad2()}/${d.year}"
    }

    /** dd/MM/yyyy HH:mm:ss */
    fun dateTime(millis: Long): String {
        val t = local(millis)
        return "${date(millis)} ${t.hour.pad2()}:${t.minute.pad2()}:${t.second.pad2()}"
    }

    /** Duration as HH:mm:ss. */
    fun countdown(durationMillis: Long): String {
        val totalSeconds = durationMillis.coerceAtLeast(0L) / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return "${hours.pad2()}:${minutes.pad2()}:${seconds.pad2()}"
    }
}
