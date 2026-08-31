package com.example.util

import android.content.Context
import android.content.SharedPreferences
import kotlin.random.Random

object IndonesianNameGenerator {

    private const val PREFS_NAME = "capsule_floating_prefs"
    private const val KEY_SAVED_PASSWORD = "saved_custom_password"
    private const val DEFAULT_PASSWORD = "Password123!"

    // Kumpulan nama depan Indonesia (Pria & Wanita) yang natural dan populer
    private val firstNames = listOf(
        // Pria
        "Dimas", "Rizky", "Aditya", "Bayu", "Fajar", "Budi", "Andika", "Wahyu",
        "Gilang", "Hendra", "Eko", "Rian", "Ilham", "Farhan", "Bagus", "Arif",
        "Rangga", "Danang", "Galih", "Rafi", "Yusuf", "Satria", "Doni", "Agung",
        "Prasetyo", "Reza", "Yoga", "Fikri", "Aldi", "Fadli", "Surya", "Guntur",
        "Deni", "Fahmi", "Iqbal", "Teguh", "Bambang", "Indra", "Gunawan", "Alif",
        // Wanita
        "Siti", "Dewi", "Putri", "Nur", "Ayu", "Anisa", "Lestari", "Mega",
        "Nabila", "Dini", "Tiara", "Rina", "Maya", "Fitri", "Indah", "Ratna",
        "Winda", "Intan", "Nadia", "Sari", "Tia", "Melati", "Zahra", "Salsa",
        "Aulia", "Citra", "Desi", "Gita", "Kartika", "Lia", "Novita", "Shinta"
    )

    // Kumpulan nama belakang / kata kedua Indonesia yang realistis
    private val secondNames = listOf(
        "Pratama", "Saputra", "Wijaya", "Santoso", "Nugraha", "Hidayat", "Setiawan",
        "Kurniawan", "Wibowo", "Ramadhan", "Kusuma", "Permana", "Firmansyah",
        "Utama", "Prasetya", "Utami", "Maharani", "Wulandari", "Safitri", "Rahmawati",
        "Anggraini", "Purnama", "Siregar", "Nasution", "Lubis", "Simanjuntak",
        "Gunawan", "Maulana", "Irawan", "Subagyo", "Hartono", "Syahputra",
        "Cahyono", "Pamungkas", "Susanto", "Triyono", "Sudirman", "Darwanto",
        "Suharto", "Kencana", "Wardana", "Budiman", "Lesmana", "Hakim", "Aziz"
    )

    /**
     * Menghasilkan nama orang Indonesia yang selalu terdiri dari 2 kata (Nama Depan + Nama Belakang).
     */
    fun generateTwoWordName(): String {
        val first = firstNames[Random.nextInt(firstNames.size)]
        var second = secondNames[Random.nextInt(secondNames.size)]
        // Hindari jika kata kedua persis sama dengan kata pertama
        while (second.equals(first, ignoreCase = true)) {
            second = secondNames[Random.nextInt(secondNames.size)]
        }
        return "$first $second"
    }

    /**
     * Menghasilkan kata sandi acak yang kuat (kombinasi huruf besar, kecil, angka, dan simbol).
     */
    fun generateRandomPassword(length: Int = 10): String {
        val upperChars = "ABCDEFGHJKLMNPQRSTUVWXYZ"
        val lowerChars = "abcdefghijkmnopqrstuvwxyz"
        val numberChars = "23456789"
        val specialChars = "@#$!%*"

        val allChars = upperChars + lowerChars + numberChars + specialChars

        val pass = StringBuilder()
        // Pastikan ada minimal 1 huruf besar, 1 huruf kecil, 1 angka, 1 simbol
        pass.append(upperChars[Random.nextInt(upperChars.length)])
        pass.append(lowerChars[Random.nextInt(lowerChars.length)])
        pass.append(numberChars[Random.nextInt(numberChars.length)])
        pass.append(specialChars[Random.nextInt(specialChars.length)])

        for (i in 4 until length) {
            pass.append(allChars[Random.nextInt(allChars.length)])
        }

        // Acak urutan karakter
        return pass.toList().shuffled().joinToString("")
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Mendapatkan password yang disimpan oleh pengguna.
     */
    fun getSavedPassword(context: Context): String {
        return getPrefs(context).getString(KEY_SAVED_PASSWORD, DEFAULT_PASSWORD) ?: DEFAULT_PASSWORD
    }

    /**
     * Menyimpan atau mengubah password default sesuai keinginan pengguna.
     */
    fun saveCustomPassword(context: Context, newPassword: String) {
        getPrefs(context).edit().putString(KEY_SAVED_PASSWORD, newPassword).apply()
    }
}
