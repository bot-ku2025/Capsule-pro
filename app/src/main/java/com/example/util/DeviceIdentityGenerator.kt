package com.example.util

import com.example.data.local.entity.IdentityConfigEntity
import java.util.UUID
import kotlin.random.Random

data class DevicePreset(
    val brand: String,
    val model: String,
    val androidVersion: String,
    val codename: String,
    val boardPlatform: String,
    val manufacturer: String,
    val productDevice: String,
    val fingerprint: String
)

object DeviceIdentityGenerator {

    val PRESETS = listOf(
        DevicePreset(
            brand = "Google",
            model = "Pixel 8 Pro (husky)",
            androidVersion = "Android 14 (API 34)",
            codename = "husky",
            boardPlatform = "zuma / google-tensor-g3",
            manufacturer = "Google",
            productDevice = "husky",
            fingerprint = "google/husky/husky:14/UQ1A.240205.004/11269751:user/release-keys"
        ),
        DevicePreset(
            brand = "Samsung",
            model = "Galaxy S24 Ultra (SM-S928B)",
            androidVersion = "Android 14 (API 34)",
            codename = "e3q",
            boardPlatform = "qcom / kalama-snapdragon8gen3",
            manufacturer = "samsung",
            productDevice = "e3qxxx",
            fingerprint = "samsung/e3qxxx/e3q:14/UP1A.231005.007/S928BXXU1AXB5:user/release-keys"
        ),
        DevicePreset(
            brand = "Xiaomi",
            model = "Xiaomi 14 Pro (23116PN5BC)",
            androidVersion = "Android 14 (API 34)",
            codename = "shennong",
            boardPlatform = "qcom / snapdragon8gen3",
            manufacturer = "Xiaomi",
            productDevice = "shennong",
            fingerprint = "Xiaomi/shennong/shennong:14/UKQ1.230804.001/V816.0.24.0.UNBCNXM:user/release-keys"
        ),
        DevicePreset(
            brand = "Sony",
            model = "Xperia 1 V (XQ-DQ72)",
            androidVersion = "Android 14 (API 34)",
            codename = "pdx234",
            boardPlatform = "qcom / kalama",
            manufacturer = "Sony",
            productDevice = "pdx234",
            fingerprint = "Sony/XQ-DQ72_EEA/XQ-DQ72:14/67.1.A.2.220/067001A002022003883492723:user/release-keys"
        ),
        DevicePreset(
            brand = "ASUS",
            model = "ROG Phone 8 Pro (AI2401)",
            androidVersion = "Android 14 (API 34)",
            codename = "ASUS_AI2401",
            boardPlatform = "qcom / snapdragon-8-gen3",
            manufacturer = "asus",
            productDevice = "ASUS_AI2401",
            fingerprint = "asus/WW_AI2401/ASUS_AI2401:14/UKQ1.230924.001/34.1420.1420.327-0:user/release-keys"
        )
    )

    fun createFreshBlankIdentity(profileId: String): IdentityConfigEntity {
        return generateRandomizedIdentity(profileId, PRESETS.first())
    }

    fun generateRandomizedIdentity(profileId: String, preset: DevicePreset? = null): IdentityConfigEntity {
        val selectedPreset = preset ?: PRESETS.random()
        val randomImei1 = generateValidImei("86")
        val randomImei2 = generateValidImei("86")
        val randomAndroidId = generateRandomHex(16)
        val randomSerial = "R5" + generateRandomAlphanumeric(9).uppercase()
        val randomGsf = generateRandomHex(16).lowercase()
        val randomWifiMac = generateValidMacAddress()
        val randomWifiBssid = generateValidMacAddress().lowercase()
        val randomBtMac = generateValidMacAddress()
        val randomGaid = UUID.randomUUID().toString()
        val randomAppSetId = UUID.randomUUID().toString()
        val randomDrmId = "WV-DRM-" + generateRandomHex(12).uppercase()
        val randomSsid = listOf("Home_Fiber_5G", "Starlink_Guest", "Office_Secure_WLAN", "Private_Mesh_WiFi", "SkyNet_5G").random()
        val randomBtName = listOf("Galaxy Buds Pro", "Pixel Buds Pro", "WH-1000XM5", "Capsule Device", "Soundcore Q30").random()

        val userAgent = "Mozilla/5.0 (Linux; Android 14; ${selectedPreset.model}) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"

        return IdentityConfigEntity(
            profileId = profileId,
            brand = selectedPreset.brand,
            model = selectedPreset.model,
            androidVersion = selectedPreset.androidVersion,
            codename = selectedPreset.codename,
            boardPlatform = selectedPreset.boardPlatform,
            manufacturer = selectedPreset.manufacturer,
            productDevice = selectedPreset.productDevice,
            fingerprint = selectedPreset.fingerprint,
            androidId = randomAndroidId,
            imei1 = randomImei1,
            imei2 = randomImei2,
            serialNumber = randomSerial,
            gsfId = randomGsf,
            wifiMac = randomWifiMac,
            wifiSsid = randomSsid,
            wifiBssid = randomWifiBssid,
            bluetoothMac = randomBtMac,
            bluetoothName = randomBtName,
            advertisingId = randomGaid,
            appSetId = randomAppSetId,
            widevineDrmId = randomDrmId,
            userAgent = userAgent,
            installerPackage = "com.android.vending",
            hiddenKeyboardPackages = "com.google.android.inputmethod.latin,com.touchtype.swiftkey",
            virtualDefaultIme = "com.android.inputmethod.latin/.LatinIME",
            lastSynchronizedTimestamp = System.currentTimeMillis(),
            isFresh = false
        )
    }

    /**
     * Generates a 15-digit valid IMEI using Luhn Check Digit algorithm.
     */
    fun generateValidImei(tacPrefix: String = "86"): String {
        val sb = StringBuilder(tacPrefix)
        while (sb.length < 14) {
            sb.append(Random.nextInt(0, 10))
        }
        val checkDigit = calculateLuhnCheckDigit(sb.toString())
        sb.append(checkDigit)
        return sb.toString()
    }

    private fun calculateLuhnCheckDigit(number: String): Int {
        var sum = 0
        for (i in number.indices) {
            var digit = number[number.length - 1 - i] - '0'
            if (i % 2 == 0) {
                digit *= 2
                if (digit > 9) digit -= 9
            }
            sum += digit
        }
        val remainder = sum % 10
        return if (remainder == 0) 0 else 10 - remainder
    }

    fun generateValidMacAddress(): String {
        val bytes = ByteArray(6)
        Random.nextBytes(bytes)
        // Set bit 1 of byte 0 to 1 (locally administered) and bit 0 to 0 (unicast)
        bytes[0] = ((bytes[0].toInt() and 0xFC) or 0x02).toByte()
        return bytes.joinToString(":") { "%02X".format(it) }
    }

    fun generateRandomHex(length: Int): String {
        val chars = "0123456789abcdef"
        return (1..length)
            .map { chars.random() }
            .joinToString("")
    }

    fun generateRandomAlphanumeric(length: Int): String {
        val chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
        return (1..length)
            .map { chars.random() }
            .joinToString("")
    }
}
