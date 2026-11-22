
package com.example.smartmedicinebox

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.smartmedicinebox.data.Medicine
import com.example.smartmedicinebox.domain.ReminderScheduler
import com.example.smartmedicinebox.domain.ReminderTime

/** Android implementation of the shared [ReminderScheduler]. */
class AndroidReminderScheduler(
    private val context: Context
) : ReminderScheduler {

    override fun schedule(medicine: Medicine) =
        MedicineReminder.schedule(context, medicine.id, medicine.name, medicine.time)

    override fun cancel(medicineId: Long) =
        MedicineReminder.cancel(context, medicineId)
}

object MedicineReminder {

    const val EXTRA_ID = "medicineId"
    const val EXTRA_NAME = "medicineName"
    const val EXTRA_TIME = "time"

    /** Alarm / notification request code for a medicine. */
    fun requestCode(medicineId: Long): Int = medicineId.toInt()

    fun schedule(
        context: Context,
        medicineId: Long,
        medicineName: String,
        time: String
    ) {

        val triggerAt = ReminderTime.nextOccurrenceMillis(
            time,
            System.currentTimeMillis()
        ) ?: return

        val intent = Intent(
            context,
            ReminderReceiver::class.java
        ).apply {
            putExtra(EXTRA_ID, medicineId)
            putExtra(EXTRA_NAME, medicineName)
            putExtra(EXTRA_TIME, time)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode(medicineId),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(
            Context.ALARM_SERVICE
        ) as AlarmManager

        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            pendingIntent
        )
    }

    // Annuler le rappel sans toucher à l'historique
    fun cancel(
        context: Context,
        medicineId: Long
    ) {
        cancelByRequestCode(context, requestCode(medicineId))
    }

    /** Cancels an alarm created by the old version of the app (keyed by name). */
    fun cancelLegacy(
        context: Context,
        medicineName: String
    ) {
        cancelByRequestCode(context, medicineName.hashCode())
    }

    private fun cancelByRequestCode(
        context: Context,
        requestCode: Int
    ) {

        val intent = Intent(
            context,
            ReminderReceiver::class.java
        )

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(
            Context.ALARM_SERVICE
        ) as AlarmManager

        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()

        val notificationManager = context.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        notificationManager.cancel(requestCode)
    }
}
