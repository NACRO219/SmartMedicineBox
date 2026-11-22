package com.example.smartmedicinebox.domain

import kotlinx.coroutines.flow.StateFlow

/** What the screens show about the ESP32 box. */
data class BoxState(
    val message: String = "Bluetooth non connecté",
    val connected: Boolean = false
)

/**
 * Bluetooth link to the ESP32 SmartMed_Box.
 *  - Android: androidApp/AndroidBoxConnection (BluetoothService)
 *  - iPhone:  CoreBluetooth (phase 4)
 */
interface BoxConnection {
    val state: StateFlow<BoxState>

    fun connect()
    fun disconnect()

    /** Sends a test reminder to the box; the result is reported through [state]. */
    fun sendTestReminder()
}
