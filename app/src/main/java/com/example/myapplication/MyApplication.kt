package com.example.myapplication

import android.app.Application
import androidx.room3.Room
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.repository.DefaultSettingsRepository
import com.example.myapplication.data.repository.SettingsRepository

class MyApplication : Application() {
    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "my-app.db",
        )
            .addMigrations(AppDatabase.MIGRATION_18_19, AppDatabase.MIGRATION_19_20, AppDatabase.MIGRATION_20_21, AppDatabase.MIGRATION_21_22)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    val settingsRepository: SettingsRepository by lazy {
        DefaultSettingsRepository(applicationContext)
    }
}
