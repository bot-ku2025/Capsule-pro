package com.example.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppLanguage(val code: String, val displayName: String) {
    INDONESIAN("in", "Bahasa Indonesia"),
    ENGLISH("en", "English")
}

object LanguageManager {
    private const val PREFS_NAME = "capsule_language_prefs"
    private const val KEY_LANGUAGE = "app_language"

    private val _currentLanguage = MutableStateFlow(AppLanguage.INDONESIAN)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    fun init(context: Context) {
        val prefs = getPrefs(context)
        val savedCode = prefs.getString(KEY_LANGUAGE, AppLanguage.INDONESIAN.code) ?: AppLanguage.INDONESIAN.code
        val lang = if (savedCode == AppLanguage.ENGLISH.code) AppLanguage.ENGLISH else AppLanguage.INDONESIAN
        _currentLanguage.value = lang
    }

    fun setLanguage(context: Context, language: AppLanguage) {
        _currentLanguage.value = language
        getPrefs(context).edit().putString(KEY_LANGUAGE, language.code).apply()
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getString(id: String): String {
        val isEn = _currentLanguage.value == AppLanguage.ENGLISH
        return if (isEn) (englishStrings[id] ?: id) else (indonesianStrings[id] ?: id)
    }

    // Comprehensive 100% dictionary for clean language switching
    private val indonesianStrings = mapOf(
        // Tabs
        "tab_mainland" to "Mainland",
        "tab_capsule" to "Capsule",
        "tab_identity" to "Identity",
        "tab_glacier" to "Glacier",
        "tab_bridge" to "Bridge",
        "tab_settings" to "Pengaturan",

        // Header & Search
        "search_placeholder" to "Cari aplikasi atau paket...",
        "stat_capsule" to "Capsule",
        "stat_frozen" to "Beku",
        "stat_saved_ram" to "Hemat RAM",
        "quick_id" to "Quick ID",
        "app_subtitle" to "Android Dual-Space & Sandbox Engine",

        // Mainland Screen
        "filter_all" to "Semua",
        "filter_not_cloned" to "Belum Dikloning",
        "filter_user_apps" to "Aplikasi Pengguna",
        "filter_system" to "Sistem",
        "mainland_banner" to "Mainland (Ruang Utama): Pilih aplikasi untuk dikloning ke dalam Capsule Sandbox agar terisolasi dari data pribadi.",
        "empty_apps_title" to "Tidak Ada Aplikasi Ditemukan",
        "empty_apps_desc" to "Coba ubah kata kunci pencarian atau filter di atas.",
        "btn_clone" to "Kloning",
        "btn_open" to "Buka",

        // Capsule Screen
        "btn_freeze_all" to "Bekukan Semua",
        "btn_defrost_all" to "Cairkan Semua",
        "filter_active" to "Aktif ⚡",
        "filter_frozen" to "Dibekukan ❄️",
        "filter_auto_freeze" to "Auto-Freeze ⏱️",
        "capsule_empty_title" to "Belum Ada Aplikasi di Ruang Kapsul",
        "capsule_empty_desc" to "Kloning aplikasi dari tab 'Mainland' untuk mengisolasi data, mengaktifkan akun ganda, atau membekukan proses latar belakang.",
        "btn_clone_now" to "Kloning Aplikasi Sekarang",

        // Glacier Screen
        "glacier_hero_title" to "Glacier Freezer Engine",
        "glacier_hero_sub" to "Hibernasi Mendalam & Penghemat Baterai",
        "glacier_stat_frozen" to "Aplikasi Dibekukan",
        "glacier_stat_ram" to "RAM Latar Dihemat",
        "section_auto_freeze" to "OTOMASI AUTO-FREEZE",
        "screen_off_freeze_title" to "Auto-Freeze saat Layar Mati",
        "screen_off_freeze_desc" to "Otomatis bekukan aplikasi saat layar HP dimatikan atau dikunci",
        "auto_defrost_notice" to "Auto-Defrost Aktif: Aplikasi dibekukan akan otomatis dicairkan ketika dibuka.",
        "section_queue" to "ANTREAN HIBERNASI AUTO-FREEZE",
        "queue_empty_title" to "Belum Ada Aplikasi di Antrean Auto-Freeze",
        "queue_empty_desc" to "Buka detail aplikasi di tab Capsule untuk mengaktifkan Auto-Freeze.",
        "btn_defrost" to "Cairkan",
        "btn_freeze" to "Bekukan",

        // Settings Screen
        "section_language" to "PENGATURAN BAHASA / LANGUAGE",
        "language_title" to "Bahasa Aplikasi",
        "language_desc" to "Pilih Bahasa Indonesia atau Full English",
        "section_space_mgmt" to "MANAJEMEN RUANG ISOLASI",
        "reset_space_title" to "Reset & Hancurkan Ruang Kapsul",
        "reset_space_desc" to "Hapus seluruh aplikasi yang telah dikloning dan pulihkan memori",
        "btn_reset_space" to "Reset Seluruh Ruang Kapsul",
        "section_audit_logs" to "LOG AUDIT AKTIVITAS PRIVASI",
        "btn_clear_logs" to "Bersihkan",
        "empty_logs" to "Belum ada aktivitas audit tercatat.",
        "dialog_destroy_title" to "Hancurkan Ruang Isolasi?",
        "dialog_destroy_desc" to "Tindakan ini akan menghapus seluruh data kloning di dalam Capsule Sandbox dan mencairkan semua proses. Anda yakin ingin melanjutkan?",
        "btn_confirm_destroy" to "Ya, Hancurkan",
        "btn_cancel" to "Batal",
        "about_desc" to "CapsulePro adalah utilitas sandboxing, kloning aplikasi akun ganda, isolasi profil kerja, dan pembekuan proses latar belakang (Glacier Freezer) canggih untuk Android.",

        // Floating Dialog
        "floating_title" to "Floating Assistant & Utilitas",
        "floating_sub" to "Generator Nama, Password Cepat & Kontrol IP Pesawat",
        "sec_name_indo" to "NAMA INDONESIA (2 KATA)",
        "btn_random_name" to "🎲 Acak Nama",
        "btn_copy_name" to "📋 Salin Nama",
        "sec_password" to "PASSWORD (BISA DIUBAH / SIMPAN)",
        "btn_random_pass" to "🔑 Acak Kuat",
        "btn_copy_pass" to "📋 Salin Password",
        "sec_airplane_ip" to "GANTI IP OTOMATIS (MODE PESAWAT)",
        "airplane_ip_desc" to "Mode pesawat ON selama 3 detik lalu OFF untuk memperbarui IP seluler.",
        "btn_trigger_ip" to "✈️ Mulai Ganti IP (Jeda 3 Detik)",
        "sec_floating_bubble" to "Bubble Melayang (Floating)",
        "bubble_active_desc" to "Layanan bubble aktif di atas layar HP",
        "bubble_inactive_desc" to "Tampilkan bubble di atas aplikasi lain",
        "copied" to "Tersalin!",

        // Floating Overlay
        "overlay_id_title" to "⚡ Capsule Quick ID",
        "overlay_ip_title" to "✈️ Auto Ganti IP",
        "ip_status_idle" to "Mode Pesawat: Siap",
        "ip_status_running" to "Sedang Mengganti IP...",
        "ip_status_step1" to "1. Mengaktifkan Mode Pesawat (ON)...",
        "ip_status_step2" to "2. Menunggu jeda 3 detik...",
        "ip_status_step3" to "3. Mematikan Mode Pesawat (OFF) & IP Baru Aktif!",

        // Root Engine & Setup
        "sec_root_engine" to "MODE ROOT ENGINE (SUPERUSER / MAGISK / KSU)",
        "root_card_title" to "Root Access & Privileged Sandbox Engine",
        "root_card_desc" to "Eksekusi langsung pembekuan, kloning aplikasi, dan pembuatan Work Profile tanpa perlu kabel PC/ADB.",
        "root_status_granted" to "Root Terhubung & Izin Superuser Aktif (uid=0) ✓",
        "root_status_detected" to "Binary SU Ditemukan (Klik Minta Izin)",
        "root_status_none" to "Mode Non-Root (Gunakan Sandboxing / Shizuku / DPM)",
        "btn_request_root" to "Minta Izin Root (su)",
        "btn_setup_root_profile" to "1-Klik Setup Capsule via Root",
        "btn_test_root" to "Tes Root",
        "sec_engine_mode" to "PILIH WORKING ENGINE UTAMA",
        "engine_mode_title" to "Engine Eksekusi Aktif",
        "engine_mode_desc" to "Metode pembekuan dan isolasi yang digunakan CapsulePro",
        "engine_root" to "⚡ Mode Root (Superuser)",
        "engine_shizuku" to "🛠️ Mode Shizuku (ADB API)",
        "engine_dpm" to "🛡️ Mode Device Admin (DPM)",
        "engine_sandbox" to "📦 Mode Sandboxing Mandiri",
        "root_profile_success" to "✓ Work Profile Capsule berhasil dibuat dan diatur via Root!",
        "root_profile_failed" to "Gagal setup via Root: ",
        "root_granted_toast" to "✓ Akses Superuser Root Diberikan!",
        "root_denied_toast" to "Akses root ditolak atau belum diizinkan.",
        "root_freeze_cmd_title" to "Perintah Root Freeze & Force-Stop",
        "root_create_cmd_title" to "Perintah Root Buat Capsule Profile",

        // Capsule Tools Bar
        "btn_tools" to "Alat",
        "tools_title" to "Alat & Asisten Mengambang",
        "tools_desc" to "Pusat utilitas cepat & asisten kontrol",

        // Multi-Profile & Backup System
        "profile_title" to "Profil Sandbox",
        "profile_switch" to "Ganti Profil",
        "profile_new" to "Buat Profil Baru",
        "profile_delete" to "Hapus Profil",
        "profile_name_hint" to "Nama Profil (contoh: Kerja, Game, Vault)",
        "profile_active_badge" to "AKTIF",
        "btn_backup_restore" to "Cadangan & Snapshot",
        "snapshot_title" to "Snapshot & Riwayat Real-Time",
        "snapshot_desc" to "Setiap kloning dan pergantian profil tersimpan otomatis secara real-time.",
        "snapshot_create_btn" to "Ambil Snapshot Baru",
        "snapshot_restore_btn" to "Pulihkan",
        "snapshot_last_btn" to "Pulihkan Terakhir",
        "snapshot_auto_tag" to "Otomatis",
        "snapshot_manual_tag" to "Manual",
        "backup_export" to "Ekspor File Cadangan (.json)",
        "backup_import" to "Impor File Cadangan",
        "backup_export_desc" to "Simpan seluruh konfigurasi klon, profil, dan aturan isolasi ke format JSON.",
        "backup_import_desc" to "Pulihkan aplikasi dan profil dari file JSON cadangan."
    )

    private val englishStrings = mapOf(
        // Tabs
        "tab_mainland" to "Mainland",
        "tab_capsule" to "Capsule",
        "tab_identity" to "Identity",
        "tab_glacier" to "Glacier",
        "tab_bridge" to "Bridge",
        "tab_settings" to "Settings",

        // Header & Search
        "search_placeholder" to "Search apps or packages...",
        "stat_capsule" to "Capsule",
        "stat_frozen" to "Frozen",
        "stat_saved_ram" to "Saved RAM",
        "quick_id" to "Quick ID",
        "app_subtitle" to "Android Dual-Space & Sandbox Engine",

        // Mainland Screen
        "filter_all" to "All",
        "filter_not_cloned" to "Not Cloned",
        "filter_user_apps" to "User Apps",
        "filter_system" to "System",
        "mainland_banner" to "Mainland (Primary Profile): Select applications to clone into Capsule Sandbox for complete data isolation.",
        "empty_apps_title" to "No Applications Found",
        "empty_apps_desc" to "Try changing the search query or filters above.",
        "btn_clone" to "Clone",
        "btn_open" to "Open",

        // Capsule Screen
        "btn_freeze_all" to "Freeze All",
        "btn_defrost_all" to "Defrost All",
        "filter_active" to "Active ⚡",
        "filter_frozen" to "Frozen ❄️",
        "filter_auto_freeze" to "Auto-Freeze ⏱️",
        "capsule_empty_title" to "No Applications in Capsule Space",
        "capsule_empty_desc" to "Clone applications from Mainland to isolate data, enable dual accounts, or freeze background processes.",
        "btn_clone_now" to "Clone Application Now",

        // Glacier Screen
        "glacier_hero_title" to "Glacier Freezer Engine",
        "glacier_hero_sub" to "Deep Hibernation & Battery Guard",
        "glacier_stat_frozen" to "Frozen Applications",
        "glacier_stat_ram" to "Saved Background RAM",
        "section_auto_freeze" to "AUTO-FREEZE AUTOMATION",
        "screen_off_freeze_title" to "Auto-Freeze on Screen Off",
        "screen_off_freeze_desc" to "Automatically freeze background applications when screen turns off or locks",
        "auto_defrost_notice" to "Auto-Defrost Enabled: Frozen apps automatically wake up and defrost upon launch.",
        "section_queue" to "AUTO-FREEZE HIBERNATION QUEUE",
        "queue_empty_title" to "No Applications in Auto-Freeze Queue",
        "queue_empty_desc" to "Open app details in Capsule tab to enable Auto-Freeze.",
        "btn_defrost" to "Defrost",
        "btn_freeze" to "Freeze",

        // Settings Screen
        "section_language" to "LANGUAGE SETTINGS",
        "language_title" to "App Language",
        "language_desc" to "Choose Indonesian or Full English",
        "section_space_mgmt" to "ISOLATION SPACE MANAGEMENT",
        "reset_space_title" to "Reset & Destroy Capsule Space",
        "reset_space_desc" to "Delete all cloned applications and reclaim memory",
        "btn_reset_space" to "Reset Entire Capsule Space",
        "section_audit_logs" to "PRIVACY AUDIT LOGS",
        "btn_clear_logs" to "Clear",
        "empty_logs" to "No privacy audit logs recorded yet.",
        "dialog_destroy_title" to "Destroy Isolation Space?",
        "dialog_destroy_desc" to "This will permanently remove all cloned data inside Capsule Sandbox and defrost all processes. Are you sure you want to proceed?",
        "btn_confirm_destroy" to "Yes, Destroy",
        "btn_cancel" to "Cancel",
        "about_desc" to "CapsulePro is a comprehensive sandboxing, dual-account app cloning, work profile isolation, and background process freezer utility for Android.",

        // Floating Dialog
        "floating_title" to "Floating Assistant & Utilities",
        "floating_sub" to "Name Generator, Quick Password & Airplane IP Reset",
        "sec_name_indo" to "INDONESIAN NAME (2 WORDS)",
        "btn_random_name" to "🎲 Randomize Name",
        "btn_copy_name" to "📋 Copy Name",
        "sec_password" to "PASSWORD (EDITABLE / AUTO-SAVE)",
        "btn_random_pass" to "🔑 Random Strong",
        "btn_copy_pass" to "📋 Copy Password",
        "sec_airplane_ip" to "AUTO IP CHANGER (AIRPLANE MODE)",
        "airplane_ip_desc" to "Turn Airplane mode ON for 3 seconds, then OFF to refresh cellular IP.",
        "btn_trigger_ip" to "✈️ Trigger IP Change (3s Delay)",
        "sec_floating_bubble" to "Floating Screen Bubble",
        "bubble_active_desc" to "Bubble service is active over other apps",
        "bubble_inactive_desc" to "Display floating bubble over other apps",
        "copied" to "Copied!",

        // Floating Overlay
        "overlay_id_title" to "⚡ Capsule Quick ID",
        "overlay_ip_title" to "✈️ Auto IP Changer",
        "ip_status_idle" to "Airplane Mode: Ready",
        "ip_status_running" to "Refreshing IP Address...",
        "ip_status_step1" to "1. Turning Airplane Mode ON...",
        "ip_status_step2" to "2. Waiting 3-second cooldown...",
        "ip_status_step3" to "3. Turning Airplane Mode OFF & New IP Ready!",

        // Root Engine & Setup
        "sec_root_engine" to "ROOT ENGINE MODE (SUPERUSER / MAGISK / KSU)",
        "root_card_title" to "Root Access & Privileged Sandbox Engine",
        "root_card_desc" to "Execute direct freezing, app cloning, and Work Profile creation without needing PC/ADB cables.",
        "root_status_granted" to "Root Connected & Superuser Granted (uid=0) ✓",
        "root_status_detected" to "SU Binary Found (Click Request Access)",
        "root_status_none" to "Non-Root Mode (Use Sandboxing / Shizuku / DPM)",
        "btn_request_root" to "Request Root Access (su)",
        "btn_setup_root_profile" to "1-Click Setup Capsule via Root",
        "btn_test_root" to "Test Root",
        "sec_engine_mode" to "CHOOSE PRIMARY WORKING ENGINE",
        "engine_mode_title" to "Active Execution Engine",
        "engine_mode_desc" to "Freezing and isolation engine method used by CapsulePro",
        "engine_root" to "⚡ Root Mode (Superuser)",
        "engine_shizuku" to "🛠️ Shizuku Mode (ADB API)",
        "engine_dpm" to "🛡️ Device Admin Mode (DPM)",
        "engine_sandbox" to "📦 Native Sandboxing Mode",
        "root_profile_success" to "✓ Capsule Work Profile successfully created & set up via Root!",
        "root_profile_failed" to "Root setup failed: ",
        "root_granted_toast" to "✓ Root Superuser Access Granted!",
        "root_denied_toast" to "Root access denied or not yet granted.",
        "root_freeze_cmd_title" to "Root Freeze & Force-Stop Command",
        "root_create_cmd_title" to "Root Capsule Profile Command",

        // Capsule Tools Bar
        "btn_tools" to "Tools",
        "tools_title" to "Floating Tools & Assistant",
        "tools_desc" to "Quick utility center & control assistant",

        // Multi-Profile & Backup System
        "profile_title" to "Sandbox Profiles",
        "profile_switch" to "Switch Profile",
        "profile_new" to "Create New Profile",
        "profile_delete" to "Delete Profile",
        "profile_name_hint" to "Profile Name (e.g. Work, Gaming, Vault)",
        "profile_active_badge" to "ACTIVE",
        "btn_backup_restore" to "Backup & Snapshots",
        "snapshot_title" to "Real-Time Snapshots & History",
        "snapshot_desc" to "Every cloning action and profile switch is automatically snapshotted in real-time.",
        "snapshot_create_btn" to "Take New Snapshot",
        "snapshot_restore_btn" to "Restore",
        "snapshot_last_btn" to "Restore Latest",
        "snapshot_auto_tag" to "Auto",
        "snapshot_manual_tag" to "Manual",
        "backup_export" to "Export Backup File (.json)",
        "backup_import" to "Import Backup File",
        "backup_export_desc" to "Save all cloned app configs, profiles, and isolation rules to JSON.",
        "backup_import_desc" to "Restore app configuration and profiles from a JSON backup file."
    )
}
