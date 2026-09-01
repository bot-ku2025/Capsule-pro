package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.CapsuleDao
import com.example.data.local.entity.CapsuleAppEntity
import com.example.data.local.entity.CapsuleLogEntity
import com.example.data.local.entity.CapsuleProfileEntity
import com.example.data.local.entity.CapsuleSnapshotEntity
import com.example.data.local.entity.IdentityConfigEntity

@Database(
    entities = [
        CapsuleAppEntity::class,
        CapsuleLogEntity::class,
        CapsuleProfileEntity::class,
        CapsuleSnapshotEntity::class,
        IdentityConfigEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun capsuleDao(): CapsuleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "capsule_pro_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
