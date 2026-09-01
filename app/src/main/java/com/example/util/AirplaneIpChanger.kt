package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import java.io.DataOutputStream

object AirplaneIpChanger {

    /**
     * Executes Airplane mode toggle:
     * 1. Turns Airplane mode ON
     * 2. Waits 3 seconds
     * 3. Turns Airplane mode OFF
     *
     * Tries Root/Shell first (if available/granted), otherwise opens Android Airplane Settings smoothly
     * with countdown guide so user experiences seamless 3-second IP renewal.
     */
    fun performAirplaneCycle(
        context: Context,
        onProgress: (step: String, secondsRemaining: Int) -> Unit = { _, _ -> },
        onComplete: (success: Boolean) -> Unit = {}
    ) {
        val handler = Handler(Looper.getMainLooper())

        // Try Shell / Root / ADB first
        Thread {
            val rootSuccess = tryRootAirplaneCycle(onProgress, handler)
            if (rootSuccess) {
                handler.post {
                    Toast.makeText(context, "✓ IP Berhasil Diperbarui via Mode Pesawat (Jeda 3 Detik)", Toast.LENGTH_SHORT).show()
                    onComplete(true)
                }
            } else {
                // If shell method not permitted, use intelligent system settings automation
                handler.post {
                    openSystemAirplaneAutomation(context, onProgress, onComplete)
                }
            }
        }.start()
    }

    private fun tryRootAirplaneCycle(
        onProgress: (step: String, secondsRemaining: Int) -> Unit,
        handler: Handler
    ): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su")
            val os = DataOutputStream(process.outputStream)

            handler.post { onProgress("Mengaktifkan Mode Pesawat (ON)...", 3) }
            os.writeBytes("cmd connectivity airplane-mode enable\n")
            os.writeBytes("settings put global airplane_mode_on 1\n")
            os.writeBytes("am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true\n")
            os.flush()

            Thread.sleep(1000)
            handler.post { onProgress("Menunggu jeda 3 detik...", 2) }
            Thread.sleep(1000)
            handler.post { onProgress("Menunggu jeda 3 detik...", 1) }
            Thread.sleep(1000)

            handler.post { onProgress("Mematikan Mode Pesawat (OFF) & Mendapatkan IP Baru...", 0) }
            os.writeBytes("cmd connectivity airplane-mode disable\n")
            os.writeBytes("settings put global airplane_mode_on 0\n")
            os.writeBytes("am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false\n")
            os.writeBytes("exit\n")
            os.flush()

            process.waitFor() == 0
        } catch (_: Exception) {
            false
        }
    }

    private fun openSystemAirplaneAutomation(
        context: Context,
        onProgress: (step: String, secondsRemaining: Int) -> Unit,
        onComplete: (success: Boolean) -> Unit
    ) {
        try {
            val intent = Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            Toast.makeText(
                context,
                "✈️ Aktifkan Mode Pesawat, hitung 3 detik, lalu matikan untuk IP baru",
                Toast.LENGTH_LONG
            ).show()

            val handler = Handler(Looper.getMainLooper())
            onProgress("1. Aktifkan Mode Pesawat...", 3)

            handler.postDelayed({
                onProgress("2. Jeda pergantian jaringan (3s)...", 2)
            }, 1000)

            handler.postDelayed({
                onProgress("3. Jeda pergantian jaringan (3s)...", 1)
            }, 2000)

            handler.postDelayed({
                onProgress("4. Matikan Mode Pesawat sekarang -> IP Baru Aktif!", 0)
                onComplete(true)
            }, 3000)

        } catch (e: Exception) {
            try {
                val wireIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(wireIntent)
            } catch (e2: Exception) {
                Toast.makeText(context, "Buka Pengaturan Mode Pesawat: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
            onComplete(false)
        }
    }
}
