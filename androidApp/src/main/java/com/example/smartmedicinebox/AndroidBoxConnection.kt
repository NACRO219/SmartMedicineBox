package com.example.smartmedicinebox

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.smartmedicinebox.domain.BoxConnection
import com.example.smartmedicinebox.domain.BoxState
import kotlinx.coroutines.flow.StateFlow

/**
 * Android implementation of the shared [BoxConnection], on top of
 * [BluetoothService] and the [BluetoothConnectionService] foreground service.
 *
 * One instance for the whole app (see [AppContainer]); the visible activity
 * plugs in [permissionRequester] so missing Bluetooth permissions can be asked.
 */
class AndroidBoxConnection(
    private val context: Context
) : BoxConnection {

    override val state: StateFlow<BoxState> = BluetoothService.state

    /** Set by MainActivity while it is alive; launches the permission dialog. */
    var permissionRequester: ((Array<String>) -> Unit)? = null

    override fun connect() {
        val missing = requiredPermissions().filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isEmpty()) {
            startConnectionService()
        } else {
            val requester = permissionRequester
            if (requester != null) {
                requester(missing.toTypedArray())
            } else {
                BluetoothService.post("Permission Bluetooth requise")
            }
        }
    }

    /** Called by MainActivity with the result of the permission dialog. */
    fun onPermissionsResult(allGranted: Boolean) {
        if (allGranted) {
            startConnectionService()
        } else {
            BluetoothService.post("Permission Bluetooth refusée")
        }
    }

    override fun disconnect() {
        context.stopService(Intent(context, BluetoothConnectionService::class.java))
        BluetoothService.disconnect()
        BluetoothService.post("Bluetooth non connecté")
    }

    override fun sendTestReminder() {
        if (BluetoothService.sendReminder("Test")) {
            BluetoothService.post("Commande envoyée à ESP32", connected = true)
        } else {
            BluetoothService.post("Échec : ESP32 non connecté")
        }
    }

    private fun startConnectionService() {
        val intent = Intent(context, BluetoothConnectionService::class.java).apply {
            action = BluetoothConnectionService.ACTION_CONNECT
        }
        ContextCompat.startForegroundService(context, intent)
    }

    private fun requiredPermissions(): Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
}
