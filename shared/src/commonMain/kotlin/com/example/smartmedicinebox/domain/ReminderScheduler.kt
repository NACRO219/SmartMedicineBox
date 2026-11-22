package com.example.smartmedicinebox.domain

import com.example.smartmedicinebox.data.Medicine

/**
 * Daily reminders for a medicine.
 *  - Android: AlarmManager + notification (androidApp/AndroidReminderScheduler)
 *  - iPhone:  repeating local notification (phase 4)
 */
interface ReminderScheduler {
    fun schedule(medicine: Medicine)
    fun cancel(medicineId: Long)
}
