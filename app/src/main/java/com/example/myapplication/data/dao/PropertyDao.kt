package com.example.myapplication.data.dao

import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Transaction
import com.example.myapplication.data.entity.PropertyEntity
import com.example.myapplication.data.entity.PropertyWithOwner
import kotlinx.coroutines.flow.Flow

interface PropertyDao {
    @Insert
    suspend fun insertProperty(property: PropertyEntity): Long

    @Transaction
    @Query("SELECT * FROM Property")
    fun getAllPropertiesWithOwners(): Flow<List<PropertyWithOwner>>
}