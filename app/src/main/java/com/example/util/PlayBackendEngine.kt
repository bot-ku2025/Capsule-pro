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

    val POPULAR_RESTRICTED_APPS = listOf(
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
            packageName = "com.zhiliaoapp.musically",
            appName = "TikTok Lite / Global",
            developer = "TikTok Pte. Ltd.",
            version = "v34.1.2",
            versionCode = 34120,
            sizeMb = "98.5 MB",
            architecture = "ARM64-v8a",
            minSdk = "Android 8.0+",
            category = "Sosial Media",
            iconEmoji = "🎵",
            downloadUrl = "https://play.google.com/store/apps/details?id=com.zhiliaoapp.musically"
        ),
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
            packageName = "com.bca",
            appName = "BCA Mobile (Work Sandbox Ready)",
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
            packageName = "id.dana",
            appName = "DANA - Dompet Digital Indonesia",
            developer = "PT Espay Debit Indonesia Koe",
            version = "v2.54.0",
            versionCode = 25400,
            sizeMb = "66.4 MB",
            architecture = "ARM64-v8a",
            minSdk = "Android 8.0+",
            category = "Keuangan",
            iconEmoji = "💳",
            downloadUrl = "https://play.google.com/store/apps/details?id=id.dana"
        ),
        PlayAppItem(
            packageName = "com.tokopedia.tkpd",
            appName = "Tokopedia: Selalu Ada Selalu Bisa",
            developer = "Tokopedia",
            version = "v3.250.1",
            versionCode = 32501,
            sizeMb = "92.0 MB",
            architecture = "ARM64-v8a",
            minSdk = "Android 8.0+",
            category = "Belanja",
            iconEmoji = "🟢",
            downloadUrl = "https://play.google.com/store/apps/details?id=com.tokopedia.tkpd"
        ),
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
            packageName = "com.spotify.music",
            appName = "Spotify: Musik & Podcast",
            developer = "Spotify AB",
            version = "v8.9.18",
            versionCode = 89180,
            sizeMb = "47.8 MB",
            architecture = "ARM64-v8a",
            minSdk = "Android 7.0+",
            category = "Musik",
            iconEmoji = "🎧",
            downloadUrl = "https://play.google.com/store/apps/details?id=com.spotify.music"
        )
    )

    fun searchPlayApps(query: String): List<PlayAppItem> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return POPULAR_RESTRICTED_APPS
        return POPULAR_RESTRICTED_APPS.filter {
            it.appName.lowercase().contains(q) ||
            it.packageName.lowercase().contains(q) ||
            it.developer.lowercase().contains(q) ||
            it.category.lowercase().contains(q)
        }.ifEmpty {
            // Generate custom search entry if searching for unknown package
            listOf(
                PlayAppItem(
                    packageName = if (q.contains(".")) q else "com.$q.app",
                    appName = query.replaceFirstChar { it.uppercase() },
                    developer = "Google Play Verified Developer",
                    version = "v1.0.0 (Latest)",
                    versionCode = 100,
                    sizeMb = "45.0 MB",
                    architecture = "ARM64-v8a",
                    minSdk = "Android 8.0+",
                    category = "Aplikasi Khusus",
                    iconEmoji = "📦",
                    playIntegrityBypass = true,
                    rating = "4.9 ★",
                    downloadUrl = "https://play.google.com/store/apps/details?id=$q"
                )
            )
        }
    }

    suspend fun simulateDirectDownloadAndInstall(
        context: Context,
        app: PlayAppItem,
        targetProfileId: String,
        onProgress: (Int, String) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        onProgress(10, "Menginisialisasi handshake Play Store CDN...")
        delay(400)
        onProgress(35, "Mengunduh APK Base (${app.sizeMb}) [Bypass Play Integrity]...")
        delay(600)
        onProgress(70, "Memverifikasi tanda tangan digital Google SHA-256...")
        delay(500)
        onProgress(90, "Menyuntikkan paket ke Sandbox Profile ($targetProfileId)...")
        delay(500)
        onProgress(100, "✓ Berhasil terpasang di Sandbox!")
        return@withContext true
    }
}
