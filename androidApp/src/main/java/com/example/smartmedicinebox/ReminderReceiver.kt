
package com.example.smartmedicinebox

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(
        context: Context,
        intent: Intent
    ) {

        val medicineId =
            intent.getLongExtra(MedicineReminder.EXTRA_ID, -1L)

        val medicineName =
            intent.getStringExtra(MedicineReminder.EXTRA_NAME) ?: "Médicament"

        val time =
            intent.getStringExtra(MedicineReminder.EXTRA_TIME) ?: ""

        val notificationId =
            if (medicineId != -1L) MedicineReminder.requestCode(medicineId)
            else medicineName.hashCode()

        // Programmer le prochain rappel quotidien
        if (medicineId != -1L && time.isNotEmpty()) {
            MedicineReminder.schedule(
                context,
                medicineId,
                medicineName,
                time
            )
        }

        // Essayer d'envoyer la commande à ESP32
        val sent =
            BluetoothService.sendReminder(medicineName)

        val channelId = "medicine_reminders"

        val manager = context.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        // Créer le canal
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                channelId,
                "Rappels médicaments",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications de rappel"
                enableVibration(true)
            }

            manager.createNotificationChannel(channel)
        }

        // Action de confirmation
        val confirmIntent = Intent(
            context,
            MedicineTakenReceiver::class.java
        ).apply {
            putExtra(MedicineReminder.EXTRA_ID, medicineId)
            putExtra(MedicineReminder.EXTRA_NAME, medicineName)
            putExtra(MedicineReminder.EXTRA_TIME, time)
        }

        val confirmPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            confirmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        // Ouvrir l'application
        val openIntent = Intent(
            context,
            MainActivity::class.java
        )

        val openPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        // Notification
        val notification = NotificationCompat.Builder(
            context,
            channelId
        )
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Smart Medicine Box")
            .setContentText(
                "C'est l'heure du rappel : $medicineName"
            )
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        "Médicament : $medicineName\n" +
                                "Heure prévue : $time\n\n" +
                                "Vérifiez votre rappel dans l'application."
                    )
            )
            .setColor(android.graphics.Color.rgb(36, 100, 178))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openPendingIntent)
            .addAction(
                android.R.drawable.ic_menu_save,
                "Confirmer",
                confirmPendingIntent
            )
            .setAutoCancel(true)
            .setOngoing(false)
            .build()

        manager.notify(
            notificationId,
            notification
        )
    }
}
