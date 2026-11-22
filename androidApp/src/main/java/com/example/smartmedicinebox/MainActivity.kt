
package com.example.smartmedicinebox

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

/**
 * Android entry point. The screens themselves live in the shared module
 * (shared/src/commonMain/.../ui) and are also used by the iPhone app.
 */
class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { }

    private val bluetoothPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { results ->
            AppContainer.box(this).onPermissionsResult(
                results.isNotEmpty() && results.values.all { it }
            )
        }

    private val requestBluetoothPermissions: (Array<String>) -> Unit = { permissions ->
        bluetoothPermissionLauncher.launch(permissions)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= 33) {
            if (
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }
        }

        val repository = AppContainer.repository(this)
        val authRepository = AppContainer.authRepository(this)
        val box = AppContainer.box(this)

        // Alarms are lost after a reinstall / backup restore (and an app update on
        // some phones); the saved medicines are not. Re-create them from the database.
        lifecycleScope.launch { repository.rescheduleAll() }
        box.permissionRequester = requestBluetoothPermissions

        setContent {
            App(
                repository = repository,
                authRepository = authRepository,
                box = box
            )
        }
    }

    override fun onDestroy() {
        val box = AppContainer.box(this)
        if (box.permissionRequester === requestBluetoothPermissions) {
            box.permissionRequester = null
        }
        super.onDestroy()
    }
}
