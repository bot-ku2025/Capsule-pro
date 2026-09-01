package com.example.admin

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

/**
 * DeviceAdminReceiver for CapsulePro to manage Android Work Profile / Managed Profile
 * and Device Owner policies.
 */
class CapsuleDeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Toast.makeText(context, "CapsulePro Device Admin Diberikan", Toast.LENGTH_SHORT).show()
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Toast.makeText(context, "CapsulePro Device Admin Dinonaktifkan", Toast.LENGTH_SHORT).show()
    }

    override fun onProfileProvisioningComplete(context: Context, intent: Intent) {
        super.onProfileProvisioningComplete(context, intent)
        Toast.makeText(context, "Capsule Sandbox Work Profile Berhasil Dikonfigurasi!", Toast.LENGTH_LONG).show()
    }
}
