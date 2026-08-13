package com.example.myapplication.data.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import com.example.myapplication.data.entity.TenantEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TenantDao {
    @Query(value = "SELECT * FROM Tenant")
    fun getAll(): Flow<List<TenantEntity>>
    @Insert
    suspend fun insert(tenant: TenantEntity): Long
}