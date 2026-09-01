package com.example.admin

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import android.os.UserManager

object CapsulePolicyManager {

    fun getAdminComponent(context: Context): ComponentName {
        return ComponentName(context, CapsuleDeviceAdminReceiver::class.java)
    }

    fun isDeviceAdminActive(context: Context): Boolean {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager ?: return false
        return dpm.isAdminActive(getAdminComponent(context))
    }

    fun isProfileOwner(context: Context): Boolean {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager ?: return false
        return dpm.isProfileOwnerApp(context.packageName)
    }

    fun isDeviceOwner(context: Context): Boolean {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager ?: return false
        return dpm.isDeviceOwnerApp(context.packageName)
    }

    fun isManagedProfile(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val userManager = context.getSystemService(Context.USER_SERVICE) as? UserManager
            return userManager?.isManagedProfile == true
        }
        return false
    }

    fun createAddDeviceAdminIntent(context: Context): Intent {
        return Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, getAdminComponent(context))
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Aktifkan Device Administrator untuk mengizinkan CapsulePro mengelola isolasi aplikasi dan sandboxing."
            )
        }
    }

    fun createProvisioningIntent(context: Context): Intent {
        val intent = Intent(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE)
        intent.putExtra(
            DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME,
            getAdminComponent(context)
        )
        intent.putExtra(
            DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_NAME,
            context.packageName
        )
        // Compatibility extras for modern Android
        intent.putExtra("android.app.extra.PROVISIONING_LEAVE_ALL_SYSTEM_APPS_ENABLED", true)
        intent.putExtra("android.app.extra.PROVISIONING_SKIP_ENCRYPTION", true)
        return intent
    }

    fun getAdbCommandForProfileOwner(packageName: String): String {
        return "adb shell dpm set-profile-owner --user 10 $packageName/.admin.CapsuleDeviceAdminReceiver"
    }

    fun getAdbCommandForDeviceOwner(packageName: String): String {
        return "adb shell dpm set-device-owner $packageName/.admin.CapsuleDeviceAdminReceiver"
    }

    fun getAdbFreezeCommand(pkg: String): String {
        return "adb shell pm suspend $pkg"
    }

    fun getAdbUnfreezeCommand(pkg: String): String {
        return "adb shell pm unsuspend $pkg"
    }

    fun getShizukuFreezeCommand(pkg: String): String {
        return "pm disable-user --user 0 $pkg"
    }

    fun getShizukuUnfreezeCommand(pkg: String): String {
        return "pm enable --user 0 $pkg"
    }

    fun getRootFreezeCommand(pkg: String): String {
        return "su -c pm disable-user --user 0 $pkg && su -c am force-stop $pkg"
    }

    fun getRootUnfreezeCommand(pkg: String): String {
        return "su -c pm enable --user 0 $pkg"
    }

    fun getRootCreateProfileCommand(packageName: String): String {
        return "su -c pm create-user --profileOf 0 --managed \"Capsule Space\" && su -c dpm set-profile-owner --user 10 $packageName/.admin.CapsuleDeviceAdminReceiver"
    }
}
