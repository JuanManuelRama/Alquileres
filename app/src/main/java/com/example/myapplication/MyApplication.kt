package com.example.myapplication

import android.app.Application
import androidx.room3.Room
import com.example.myapplication.data.AppDatabase

class MyApplication : Application() {
    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "my-app.db"
        )
            .addMigrations(AppDatabase.MIGRATION_18_19, AppDatabase.MIGRATION_19_20)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }
}
