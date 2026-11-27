package ar.controlalq

import android.app.Application
import androidx.room3.Room
import ar.controlalq.data.AppDatabase
import ar.controlalq.data.repository.DefaultSettingsRepository
import ar.controlalq.data.repository.SettingsRepository

class ControlAlq : Application() {
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
