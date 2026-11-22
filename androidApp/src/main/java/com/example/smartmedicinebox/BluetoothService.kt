
package com.example.smartmedicinebox

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.example.smartmedicinebox.domain.BoxState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

object BluetoothService {

    private const val DEVICE_NAME = "SmartMed_Box"

    private val SERVICE_UUID =
        UUID.fromString("7d2a0001-7e3d-4f2a-9c5b-1a2b3c4d5e6f")

    private val COMMAND_UUID =
        UUID.fromString("7d2a0002-7e3d-4f2a-9c5b-1a2b3c4d5e6f")

    private val STATUS_UUID =
        UUID.fromString("7d2a0003-7e3d-4f2a-9c5b-1a2b3c4d5e6f")

    private val CCCD_UUID =
        UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    private var gatt: BluetoothGatt? = null
    private var commandCharacteristic: BluetoothGattCharacteristic? = null
    private var scanner: BluetoothLeScanner? = null
    private var appContext: Context? = null

    private val _state = MutableStateFlow(BoxState())

    /** Connection status shown by the shared UI. */
    val state: StateFlow<BoxState> = _state.asStateFlow()

    fun post(message: String, connected: Boolean = false) {
        _state.value = BoxState(message, connected)
    }

    private fun hasBluetoothPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) ==
                    PackageManager.PERMISSION_GRANTED &&
                    context.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) ==
                    PackageManager.PERMISSION_GRANTED
        } else {
            context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED
        }
    }

    @SuppressLint("MissingPermission")
    fun connect(context: Context) {

        if (!hasBluetoothPermission(context)) {
            post("Bluetooth permission missing")
            return
        }

        appContext = context.applicationContext

        val manager = context.getSystemService(
            Context.BLUETOOTH_SERVICE
        ) as BluetoothManager

        val adapter = manager.adapter

        if (adapter == null) {
            post("Bluetooth unavailable")
            return
        }

        if (!adapter.isEnabled) {
            post("Please enable Bluetooth")
            return
        }

        val bleScanner = adapter.bluetoothLeScanner

        if (bleScanner == null) {
            post("Bluetooth scanner unavailable")
            return
        }

        scanner = bleScanner

        post("Searching for ESP32...")
        bleScanner.startScan(scanCallback)
    }

    private val scanCallback = object : ScanCallback() {

        @SuppressLint("MissingPermission")
        override fun onScanResult(
            callbackType: Int,
            result: ScanResult
        ) {
            val device = result.device
            val deviceName =
                result.scanRecord?.deviceName ?: device.name

            if (deviceName == DEVICE_NAME) {

                scanner?.stopScan(this)

                post("ESP32 found. Connecting...")

                val context = appContext ?: return

                gatt?.close()

                gatt = device.connectGatt(
                    context,
                    false,
                    gattCallback,
                    BluetoothDevice.TRANSPORT_LE
                )
            }
        }

        override fun onScanFailed(errorCode: Int) {
            post("BLE scan failed: $errorCode")
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {

        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(
            bluetoothGatt: BluetoothGatt,
            status: Int,
            newState: Int
        ) {
            if (
                status == BluetoothGatt.GATT_SUCCESS &&
                newState == BluetoothProfile.STATE_CONNECTED
            ) {
                gatt = bluetoothGatt
                post("Connected. Discovering services...")
                bluetoothGatt.discoverServices()
            } else {
                gatt = null
                commandCharacteristic = null
                bluetoothGatt.close()
                post("ESP32 disconnected")
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(
            bluetoothGatt: BluetoothGatt,
            status: Int
        ) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                post("Service discovery failed")
                return
            }

            val service = bluetoothGatt.getService(SERVICE_UUID)

            if (service == null) {
                post("ESP32 service not found")
                return
            }

            commandCharacteristic =
                service.getCharacteristic(COMMAND_UUID)

            val statusCharacteristic =
                service.getCharacteristic(STATUS_UUID)

            if (
                commandCharacteristic == null ||
                statusCharacteristic == null
            ) {
                post("BLE characteristics missing")
                return
            }

            bluetoothGatt.setCharacteristicNotification(
                statusCharacteristic,
                true
            )

            val descriptor =
                statusCharacteristic.getDescriptor(CCCD_UUID)

            if (descriptor == null) {
                post("Notification descriptor missing")
                return
            }

            if (Build.VERSION.SDK_INT >= 33) {
                bluetoothGatt.writeDescriptor(
                    descriptor,
                    BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                )
            } else {
                @Suppress("DEPRECATION")
                descriptor.value =
                    BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE

                @Suppress("DEPRECATION")
                bluetoothGatt.writeDescriptor(descriptor)
            }
        }

        override fun onDescriptorWrite(
            bluetoothGatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int
        ) {
            if (
                descriptor.uuid == CCCD_UUID &&
                status == BluetoothGatt.GATT_SUCCESS
            ) {
                post("ESP32 connected", connected = true)
            } else {
                post("Notification setup failed")
            }
        }

        override fun onCharacteristicChanged(
            bluetoothGatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            if (characteristic.uuid == STATUS_UUID) {
                post(value.toString(Charsets.UTF_8), connected = true)
            }
        }

        @Deprecated("Deprecated on Android 13")
        override fun onCharacteristicChanged(
            bluetoothGatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            if (
                Build.VERSION.SDK_INT < 33 &&
                characteristic.uuid == STATUS_UUID
            ) {
                post(characteristic.value.toString(Charsets.UTF_8), connected = true)
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun sendReminder(medicineName: String): Boolean {
        val bluetoothGatt = gatt ?: return false
        val characteristic = commandCharacteristic ?: return false
        val data = "REMIND".toByteArray(Charsets.UTF_8)

        return if (Build.VERSION.SDK_INT >= 33) {
            bluetoothGatt.writeCharacteristic(
                characteristic,
                data,
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            ) == BluetoothGatt.GATT_SUCCESS
        } else {
            @Suppress("DEPRECATION")
            characteristic.value = data

            @Suppress("DEPRECATION")
            characteristic.writeType =
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT

            @Suppress("DEPRECATION")
            bluetoothGatt.writeCharacteristic(characteristic)
        }
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        scanner?.stopScan(scanCallback)
        scanner = null

        gatt?.disconnect()
        gatt?.close()

        gatt = null
        commandCharacteristic = null
        appContext = null

        post("Disconnected")
    }
}