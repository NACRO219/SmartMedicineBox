
package com.example.smartmedicinebox

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MedicineTakenReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val medicineId =
            intent.getLongExtra(MedicineReminder.EXTRA_ID, -1L)
                .takeIf { it != -1L }

        val medicineName =
            intent.getStringExtra(MedicineReminder.EXTRA_NAME) ?: "Médicament"

        val time =
            intent.getStringExtra(MedicineReminder.EXTRA_TIME)

        // Fermer la notification une fois la prise confirmée
        val notificationManager = context.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        notificationManager.cancel(
            medicineId?.let { MedicineReminder.requestCode(it) }
                ?: medicineName.hashCode()
        )

        Toast.makeText(
            context,
            "$medicineName : prise confirmée !",
            Toast.LENGTH_LONG
        ).show()

        // Database writes must not run on the main thread
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                AppContainer.repository(context).recordTaken(
                    medicineId,
                    medicineName,
                    time
                )
            } finally {
                pendingResult.finish()
            }
        }
    }
}
