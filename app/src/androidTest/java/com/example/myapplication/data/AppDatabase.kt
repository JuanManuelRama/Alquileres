package com.example.myapplication.data

import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.example.myapplication.data.dao.OwnerDao
import com.example.myapplication.data.dao.PropertyDao
import com.example.myapplication.data.entity.OwnerEntity
import com.example.myapplication.data.entity.PropertyEntity

@Database(
    entities = [PropertyEntity::class, OwnerEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun propertyDao(): PropertyDao
    abstract fun ownerDao(): OwnerDao
}