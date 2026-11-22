package com.example.smartmedicinebox

import com.example.smartmedicinebox.domain.DateFormats
import com.example.smartmedicinebox.domain.ReminderTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ReminderTimeTest {

    // ---------- validation ----------

    @Test
    fun validTimesAreAccepted() {
        listOf("00:00", "08:00", "09:59", "12:30", "23:59").forEach {
            assertTrue("$it should be valid", ReminderTime.isValid(it))
        }
    }

    @Test
    fun invalidTimesAreRejected() {
        listOf("", "8:00", "24:00", "12:60", "12-30", "ab:cd", " 08:00", "08:00 ", "08:000").forEach {
            assertFalse("'$it' should be invalid", ReminderTime.isValid(it))
        }
    }

    @Test
    fun parseReturnsHourAndMinute() {
        assertEquals(8 to 30, ReminderTime.parse("08:30"))
        assertEquals(23 to 5, ReminderTime.parse("23:05"))
        assertNull(ReminderTime.parse("25:00"))
    }

    // ---------- next occurrence ----------

    /** Epoch millis for today (local time) at hh:mm:ss. */
    private fun todayAt(hour: Int, minute: Int, second: Int = 0): Long =
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, second)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    private fun tomorrowAt(hour: Int, minute: Int): Long =
        Calendar.getInstance().apply {
            timeInMillis = todayAt(hour, minute)
            add(Calendar.DAY_OF_YEAR, 1)
        }.timeInMillis

    @Test
    fun laterTodayFiresToday() {
        val now = todayAt(7, 0)
        assertEquals(todayAt(8, 0), ReminderTime.nextOccurrenceMillis("08:00", now))
    }

    @Test
    fun earlierTodayFiresTomorrow() {
        val now = todayAt(9, 0)
        assertEquals(tomorrowAt(8, 0), ReminderTime.nextOccurrenceMillis("08:00", now))
    }

    @Test
    fun exactlyNowFiresTomorrow() {
        val now = todayAt(8, 0)
        assertEquals(tomorrowAt(8, 0), ReminderTime.nextOccurrenceMillis("08:00", now))
    }

    @Test
    fun matchesTheOldCalendarImplementation() {
        // The previous Android code computed the next reminder with java.util.Calendar.
        val now = todayAt(15, 42, 17)
        for (hour in 0..23) {
            for (minute in listOf(0, 15, 42, 59)) {
                val time = "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
                val expected = Calendar.getInstance().apply {
                    timeInMillis = now
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    if (timeInMillis <= now) add(Calendar.DAY_OF_YEAR, 1)
                }.timeInMillis
                assertEquals(time, expected, ReminderTime.nextOccurrenceMillis(time, now))
            }
        }
    }

    @Test
    fun invalidTimeHasNoOccurrence() {
        assertNull(ReminderTime.nextOccurrenceMillis("99:99", todayAt(8, 0)))
    }

    // ---------- formatting ----------

    @Test
    fun countdownIsFormattedAsHoursMinutesSeconds() {
        assertEquals("00:00:00", DateFormats.countdown(0))
        assertEquals("00:00:59", DateFormats.countdown(59_999))
        assertEquals("01:01:01", DateFormats.countdown(3_661_000))
        assertEquals("23:59:59", DateFormats.countdown(86_399_000))
        assertEquals("00:00:00", DateFormats.countdown(-5_000)) // never negative
    }

    @Test
    fun dateMatchesSimpleDateFormat() {
        val millis = todayAt(9, 5, 7)
        val expectedDate = java.text.SimpleDateFormat("dd/MM/yyyy").format(java.util.Date(millis))
        val expectedDateTime = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(java.util.Date(millis))
        assertEquals(expectedDate, DateFormats.date(millis))
        assertEquals(expectedDateTime, DateFormats.dateTime(millis))
    }
}
