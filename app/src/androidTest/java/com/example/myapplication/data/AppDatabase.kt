package com.example.myapplication.data

import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.example.myapplication.model.Propiedad

@Database(
    entities = [Propiedad::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun propiedadDao(): PropiedadDao
}