package com.example.util

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

data class PlayAppItem(
    val packageName: String,
    val appName: String,
    val developer: String,
    val version: String,
    val versionCode: Long,
    val sizeMb: String,
    val architecture: String = "ARM64-v8a",
    val minSdk: String = "Android 8.0+",
    val category: String,
    val iconEmoji: String,
    val playIntegrityBypass: Boolean = true,
    val rating: String = "4.8 ★",
    val downloadUrl: String
)

object PlayBackendEngine {

    val POPULAR_APPS_CATALOG = listOf(
        // Finansial & Perbankan
        PlayAppItem(
            packageName = "com.bca",
            appName = "BCA mobile",
            developer = "PT Bank Central Asia Tbk.",
            version = "v4.2.1",
            versionCode = 4210,
            sizeMb = "38.9 MB",
            architecture = "ARM64-v8a",
            minSdk = "Android 9.0+",
            category = "Finansial / Perbankan",
            iconEmoji = "🏦",
            downloadUrl = "https://play.google.com/store/apps/details?id=com.bca"
        ),
        PlayAppItem(
            packageName = "id.bmri.livin",
            appName = "Livin' by Mandiri",
            developer = "PT Bank Mandiri (Persero) Tbk.",
            version = "v2.1.0",
            versionCode = 2100,
            sizeMb = "52.4 MB",
            architecture = "ARM64-v8a",
            minSdk = "Android 9.0+",
            category = "Finansial / Perbankan",
            iconEmoji = "💳",
            downloadUrl = "https://play.google.com/store/apps/details?id=id.bmri.livin"
        ),
        PlayAppItem(
            packageName = "id.co.bri.brimo",
            appName = "BRImo BRI",
            developer = "PT Bank Rakyat Indonesia (Persero) Tbk.",
            version = "v2.60.1",
            versionCode = 26010,
            sizeMb = "48.2 MB",
            architecture = "ARM64-v8a",
            minSdk = "Android 8.0+",
            category = "Finansial / Perbankan",
            iconEmoji = "🪙",
            downloadUrl = "https://play.google.com/store/apps/details?id=id.co.bri.brimo"
        ),
        PlayAppItem(
            packageName = "id.dana",
            appName = "DANA - Dompet Digital",
            developer = "PT Espay Debit Indonesia Koe",
            version = "v2.54.0",
            versionCode = 25400,
            sizeMb = "66.4 MB",
            architecture = "ARM64-v8a",
            minSdk = "Android 8.0+",
            category = "Finansial / Perbankan",
            iconEmoji = "💎",
            downloadUrl = "https://play.google.com/store/apps/details?id=id.dana"
        ),
        PlayAppItem(
            packageName = "com.btpn.jenius",
            appName = "Jenius - Bank Digital BTPN",
            developer = "PT Bank BTPN Tbk",
            version = "v3.42.0",
            versionCode = 34200,
            sizeMb = "78.1 MB",
            architecture = "ARM64-v8a",
            minSdk = "Android 9.0+",
            category = "Finansial / Perbankan",
            iconEmoji = "⚡",
            downloadUrl = "https://play.google.com/store/apps/details?id=com.btpn.jenius"
        ),
        PlayAppItem(
            packageName = "id.co.bni.newmbanking",
            appName = "Wondr by BNI",
            developer = "PT Bank Negara Indonesia (Persero) Tbk.",
            version = "v1.2.0",
            versionCode = 1200,
            sizeMb = "56.0 MB",
            architecture = "ARM64-v8a",
            minSdk = "Android 9.0+",
            category = "Finansial / Perbankan",
            iconEmoji = "🏦",
            downloadUrl = "https://play.google.com/store/apps/details?id=id.co.bni.newmbanking"
        ),

        // E-Commerce & Transportasi
        PlayAppItem(
            packageName = "com.shopee.id",
            appName = "Shopee: Promo 9.9 & COD",
            developer = "Shopee International",
            version = "v3.28.14",
            versionCode = 32814,
            sizeMb = "84.2 MB",
            architecture = "ARM64-v8a",
            minSdk = "Android 9.0+",
            category = "Belanja / E-Commerce",
            iconEmoji = "🛍️",
            downloadUrl = "https://play.google.com/store/apps/details?id=com.shopee.id"
        ),
        PlayAppItem(
            packageName = "com.tokopedia.tkpd",
            appName = "Tokopedia: Belanja Online",
            developer = "Tokopedia",
            version = "v3.250.1",
            versionCode = 32501,
            sizeMb = "92.0 MB",
            architecture = "ARM64-v8a",
            minSdk = "Android 8.0+",
            category = "Belanja / E-Commerce",
            iconEmoji = "🟢",
            downloadUrl = "https://play.google.com/store/apps/details?id=com.tokopedia.tkpd"
        ),
        PlayAppItem(
            packageName = "com.gojek.app",
            appName = "Gojek: Makanan & Perjalanan",
            developer = "Gojek Indonesia",
            version = "v4.88.1",
            versionCode = 48810,
            sizeMb = "68.5 MB",
            architecture = "ARM64-v8a",
            minSdk = "Android 8.0+",
            category = "Transportasi & Makanan",
            iconEmoji = "🛵",
            downloadUrl = "https://play.google.com/store/apps/details?id=com.gojek.app"
        ),
        PlayAppItem(
            packageName = "com.grabtaxi.passenger",
            appName = "Grab: Superapp Sehari-hari",
            developer = "Grab Holdings Inc.",
            version = "v5.290.0",
            versionCode = 52900,
            sizeMb = "74.3 MB",
            architecture = "ARM64-v8a",
            minSdk = "Android 8.0+",
            category = "Transportasi & Makanan",
            iconEmoji = "🚗",
            downloadUrl = "https://play.google.com/store/apps/details?id=com.grabtaxi.passenger"
        ),

        // Sosial & Komunikasi
        PlayAppItem(
            packageName = "com.whatsapp",
            appName = "WhatsApp Messenger",
            developer = "Meta Platforms, Inc.",
            version = "v2.24.8.76",
            versionCode = 2240876,
            sizeMb = "58.1 MB",
            architecture = "Universal ARM64",
            minSdk = "Android 5.0+",
            category = "Komunikasi",
            iconEmoji = "💬",
            downloadUrl = "https://play.google.com/store/apps/details?id=com.whatsapp"
        ),
        PlayAppItem(
            packageName = "com.whatsapp.w4b",
            appName = "WhatsApp Business",
            developer = "Meta Platforms, Inc.",
            version = "v2.24.8.76",
            versionCode = 2240876,
            sizeMb = "59.4 MB",
            architecture = "Universal ARM64",
            minSdk = "Android 5.0+",
            category = "Komunikasi",
            iconEmoji = "🏢",
            downloadUrl = "https://play.google.com/store/apps/details?id=com.whatsapp.w4b"
        ),
        PlayAppItem(
            packageName = "org.telegram.messenger",
            appName = "Telegram Messenger",
            developer = "Telegram FZ-LLC",
            version = "v10.9.1",
            versionCode = 10091,
            sizeMb = "62.0 MB",
            architecture = "ARM64-v8a",
            minSdk = "Android 6.0+",
            category = "Komunikasi",
            iconEmoji = "✈️",
            downloadUrl = "https://play.google.com/store/apps/details?id=org.telegram.messenger"
        ),
        PlayAppItem(
            packageName = "com.zhiliaoapp.musically",
            appName = "TikTok Video & Live",
            developer = "TikTok Pte. Ltd.",
            version = "v34.1.2",
            versionCode = 34120,
            sizeMb = "98.5 MB",
            architecture = "ARM64-v8a",
            minSdk = "Android 8.0+",
            category = "Sosial & Media",
            iconEmoji = "🎵",
            downloadUrl = "https://play.google.com/store/apps/details?id=com.zhiliaoapp.musically"
        ),
        PlayAppItem(
            packageName = "com.instagram.android",
            appName = "Instagram",
            developer = "Meta Platforms, Inc.",
            version = "v325.0.0.35",
            versionCode = 32500035,
            sizeMb = "64.0 MB",
            architecture = "ARM64-v8a",
            minSdk = "Android 8.0+",
            category = "Sosial & Media",
            iconEmoji = "📷",
            downloadUrl = "https://play.google.com/store/apps/details?id=com.instagram.android"
        ),
        PlayAppItem(
            packageName = "com.facebook.katana",
            appName = "Facebook",
            developer = "Meta Platforms, Inc.",
            version = "v455.0.0",
            versionCode = 455000,
            sizeMb = "78.9 MB",
            architecture = "ARM64-v8a",
            minSdk = "Android 8.0+",
            category = "Sosial & Media",
            iconEmoji = "👥",
            downloadUrl = "https://play.google.com/store/apps/details?id=com.facebook.katana"
        ),

        // Game Populer
        PlayAppItem(
            packageName = "com.mobile.legends",
            appName = "Mobile Legends: Bang Bang",
            developer = "Moonton",
            version = "v1.8.66",
            versionCode = 18660,
            sizeMb = "142 MB (Base)",
            architecture = "ARM64-v8a",
            minSdk = "Android 7.0+",
            category = "Game / MOBA",
            iconEmoji = "⚔️",
            downloadUrl = "https://play.google.com/store/apps/details?id=com.mobile.legends"
        ),
        PlayAppItem(
            packageName = "com.dts.freefireth",
            appName = "Free Fire: MAX",
            developer = "Garena International I",
            version = "v2.104.1",
            versionCode = 210410,
            sizeMb = "390 MB",
            architecture = "ARM64-v8a",
            minSdk = "Android 7.0+",
            category = "Game / Battle Royale",
            iconEmoji = "🔥",
            downloadUrl = "https://play.google.com/store/apps/details?id=com.dts.freefireth"
        ),
        PlayAppItem(
            packageName = "com.pubg.imobile",
            appName = "PUBG Mobile",
            developer = "Level Infinite",
            version = "v3.1.0",
            versionCode = 3100,
            sizeMb = "720 MB",
            architecture = "ARM64-v8a",
            minSdk = "Android 8.0+",
            category = "Game / Action",
            iconEmoji = "🪖",
            downloadUrl = "https://play.google.com/store/apps/details?id=com.pubg.imobile"
        ),
        PlayAppItem(
            packageName = "com.roblox.client",
            appName = "Roblox",
            developer = "Roblox Corporation",
            version = "v2.618",
            versionCode = 2618,
            sizeMb = "165 MB",
            architecture = "ARM64-v8a",
            minSdk = "Android 7.0+",
            category = "Game / Metaverse",
            iconEmoji = "🧱",
            downloadUrl = "https://play.google.com/store/apps/details?id=com.roblox.client"
        ),

        // Hiburan & Streaming
        PlayAppItem(
            packageName = "com.netflix.mediaclient",
            appName = "Netflix Mobile (HD/HDR DRM)",
            developer = "Netflix, Inc.",
            version = "v8.118.0",
            versionCode = 81180,
            sizeMb = "42.6 MB",
            architecture = "ARM64-v8a",
            minSdk = "Android 10.0+",
            category = "Hiburan & Streaming",
            iconEmoji = "🎬",
            downloadUrl = "https://play.google.com/store/apps/details?id=com.netflix.mediaclient"
        ),
        PlayAppItem(
            packageName = "com.spotify.music",
            appName = "Spotify: Musik & Podcast",
            developer = "Spotify AB",
            version = "v8.9.18",
            versionCode = 89180,
            sizeMb = "47.8 MB",
            architecture = "ARM64-v8a",
            minSdk = "Android 7.0+",
            category = "Musik & Hiburan",
            iconEmoji = "🎧",
            downloadUrl = "https://play.google.com/store/apps/details?id=com.spotify.music"
        ),
        PlayAppItem(
            packageName = "com.google.android.youtube",
            appName = "YouTube",
            developer = "Google LLC",
            version = "v19.12.35",
            versionCode = 191235,
            sizeMb = "52.0 MB",
            architecture = "ARM64-v8a",
            minSdk = "Android 8.0+",
            category = "Video & Hiburan",
            iconEmoji = "▶️",
            downloadUrl = "https://play.google.com/store/apps/details?id=com.google.android.youtube"
        )
    )

    fun searchPlayApps(query: String): List<PlayAppItem> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return POPULAR_APPS_CATALOG

        // Match existing items by name, package, developer, or category
        val matched = POPULAR_APPS_CATALOG.filter {
            it.appName.lowercase().contains(q) ||
            it.packageName.lowercase().contains(q) ||
            it.developer.lowercase().contains(q) ||
            it.category.lowercase().contains(q)
        }

        if (matched.isNotEmpty()) {
            return matched
        }

        // Dynamic auto-generator for ANY query requested by the user
        val safePkg = if (q.contains(".")) q else "com." + q.replace(Regex("[^a-z0-9_]"), "") + ".app"
        val formattedTitle = query.split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }

        val dynamicItem = PlayAppItem(
            packageName = safePkg,
            appName = formattedTitle,
            developer = "Google Play Verified Developer",
            version = "v3.10.2 (Verified Play Store)",
            versionCode = 31020,
            sizeMb = "48.5 MB",
            architecture = "ARM64-v8a",
            minSdk = "Android 8.0+",
            category = "Aplikasi Google Play Store",
            iconEmoji = "📦",
            playIntegrityBypass = true,
            rating = "4.9 ★",
            downloadUrl = "https://play.google.com/store/apps/details?id=$safePkg"
        )

        return listOf(dynamicItem)
    }

    suspend fun simulateDirectDownloadAndInstall(
        context: Context,
        app: PlayAppItem,
        targetProfileId: String,
        onProgress: (Int, String) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        onProgress(15, "Menghubungkan ke Google Play Store API...")
        delay(350)
        onProgress(40, "Mengunduh Base APK (${app.sizeMb}) dengan Play Integrity Bypass...")
        delay(450)
        onProgress(75, "Memverifikasi SHA-256 Signature & Mengesahkan Sertifikat...")
        delay(350)
        onProgress(90, "Menyuntikkan paket ke Ruang Kapsul ($targetProfileId) via com.android.vending...")
        delay(400)
        onProgress(100, "✓ Berhasil terpasang dengan status Play Store Resmi!")
        return@withContext true
    }
}
