package com.example.util

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.File
import java.io.InputStreamReader

data class RootStatus(
    val hasSuBinary: Boolean,
    val isGranted: Boolean,
    val uid: String = "",
    val suVersion: String = "",
    val message: String = ""
)

data class RootCommandResult(
    val success: Boolean,
    val exitCode: Int,
    val output: String,
    val error: String
)

object RootEngine {

    private val SU_PATHS = arrayOf(
        "/system/bin/su",
        "/system/xbin/su",
        "/sbin/su",
        "/data/local/xbin/su",
        "/data/local/bin/su",
        "/system/sd/xbin/su",
        "/system/bin/failsafe/su",
        "/data/local/su",
        "/su/bin/su",
        "/magisk/.core/bin/su",
        "/debug_ramdisk/su"
    )

    fun isSuBinaryPresent(): Boolean {
        for (path in SU_PATHS) {
            try {
                if (File(path).exists()) return true
            } catch (_: Exception) {}
        }
        // Check PATH env
        val pathEnv = System.getenv("PATH") ?: ""
        for (dir in pathEnv.split(":")) {
            try {
                if (File(dir, "su").exists()) return true
            } catch (_: Exception) {}
        }
        return false
    }

    suspend fun checkRootStatus(): RootStatus = withContext(Dispatchers.IO) {
        val suPresent = isSuBinaryPresent()
        val result = execute("id")

        if (result.success && result.output.contains("uid=0")) {
            val suVerResult = execute("su -v || su --version || which su")
            val version = if (suVerResult.success) suVerResult.output.trim().lines().firstOrNull() ?: "Root (Magisk/KernelSU)" else "Root Granted (uid=0)"
            RootStatus(
                hasSuBinary = true,
                isGranted = true,
                uid = "uid=0 (root)",
                suVersion = version,
                message = "Akses Superuser Root Aktif & Terhubung"
            )
        } else if (suPresent) {
            RootStatus(
                hasSuBinary = true,
                isGranted = false,
                uid = "",
                suVersion = "",
                message = "Binary SU terdeteksi. Silakan berikan izin Superuser (Grant Root)."
            )
        } else {
            RootStatus(
                hasSuBinary = false,
                isGranted = false,
                uid = "",
                suVersion = "",
                message = "Perangkat Non-Root (Gunakan Mode Sandboxing Mandiri / Shizuku / DPM)."
            )
        }
    }

    suspend fun requestRootAccess(): RootCommandResult = withContext(Dispatchers.IO) {
        execute("id")
    }

    suspend fun executeRoot(command: String): RootCommandResult = execute(command)

    suspend fun execute(command: String): RootCommandResult = withContext(Dispatchers.IO) {
        var process: Process? = null
        try {
            process = Runtime.getRuntime().exec("su")
            val os = DataOutputStream(process.outputStream)
            val isReader = BufferedReader(InputStreamReader(process.inputStream))
            val errReader = BufferedReader(InputStreamReader(process.errorStream))

            os.writeBytes("$command\n")
            os.writeBytes("exit\n")
            os.flush()

            val outputBuilder = StringBuilder()
            val errorBuilder = StringBuilder()

            var line: String?
            while (isReader.readLine().also { line = it } != null) {
                outputBuilder.appendLine(line)
            }
            while (errReader.readLine().also { line = it } != null) {
                errorBuilder.appendLine(line)
            }

            val exitCode = process.waitFor()
            val out = outputBuilder.toString().trim()
            val err = errorBuilder.toString().trim()

            RootCommandResult(
                success = exitCode == 0,
                exitCode = exitCode,
                output = out,
                error = err
            )
        } catch (e: Exception) {
            RootCommandResult(
                success = false,
                exitCode = -1,
                output = "",
                error = e.localizedMessage ?: "Gagal mengeksekusi perintah su"
            )
        } finally {
            process?.destroy()
        }
    }

    suspend fun freezeAppRoot(packageName: String): Boolean = withContext(Dispatchers.IO) {
        // Try multiple methods supported across Android versions
        val cmd = "pm disable-user --user 0 $packageName || pm hide $packageName || pm suspend $packageName; am force-stop $packageName"
        val res = execute(cmd)
        res.success || res.exitCode == 0
    }

    suspend fun defrostAppRoot(packageName: String): Boolean = withContext(Dispatchers.IO) {
        val cmd = "pm enable --user 0 $packageName; pm unhide $packageName; pm unsuspend $packageName"
        val res = execute(cmd)
        res.success || res.exitCode == 0
    }

    suspend fun cloneAppRoot(packageName: String, targetUserId: Int = 10): Boolean = withContext(Dispatchers.IO) {
        val cmd = "pm install-existing --user $targetUserId $packageName; pm set-installer $packageName com.android.vending || cmd package set-installer-package $packageName com.android.vending"
        val res = execute(cmd)
        res.success
    }

    suspend fun spoofPlayStoreInstaller(packageName: String, installer: String = "com.android.vending"): Boolean = withContext(Dispatchers.IO) {
        val cmd = "pm set-installer $packageName $installer || cmd package set-installer-package $packageName $installer"
        val res = execute(cmd)
        res.success
    }

    /**
     * 1-Click Capsule Work Profile setup via Root without needing a PC or ADB connection!
     */
    suspend fun setupWorkProfileWithRoot(context: Context): RootCommandResult = withContext(Dispatchers.IO) {
        val adminReceiver = "${context.packageName}/.admin.CapsuleDeviceAdminReceiver"
        val script = "USER_ID=$(pm list users | grep 'Capsule Space' | sed -n 's/.*UserInfo{\\([0-9]*\\):.*/\\1/p')\n" +
                "if [ -z \"\$USER_ID\" ]; then\n" +
                "  CREATE_OUT=$(pm create-user --profileOf 0 --managed 'Capsule Space' 2>&1)\n" +
                "  USER_ID=$(echo \"\$CREATE_OUT\" | sed -n 's/.*id \\([0-9]*\\).*/\\1/p')\n" +
                "fi\n" +
                "if [ -z \"\$USER_ID\" ]; then\n" +
                "  USER_ID=10\n" +
                "fi\n" +
                "dpm set-profile-owner --user \$USER_ID $adminReceiver\n" +
                "am start-user \$USER_ID\n" +
                "pm enable --user 0 $adminReceiver\n" +
                "pm enable --user \$USER_ID $adminReceiver\n" +
                "echo \"SUCCESS_USER_ID=\$USER_ID\""

        execute(script)
    }

    suspend fun toggleAirplaneModeRoot(enable: Boolean): Boolean = withContext(Dispatchers.IO) {
        val stateInt = if (enable) 1 else 0
        val stateBool = if (enable) "true" else "false"
        val action = if (enable) "enable" else "disable"

        val cmd = """
            cmd connectivity airplane-mode $action || (
                settings put global airplane_mode_on $stateInt &&
                am broadcast -a android.intent.action.AIRPLANE_MODE --ez state $stateBool
            )
        """.trimIndent()

        val res = execute(cmd)
        res.success
    }
}
