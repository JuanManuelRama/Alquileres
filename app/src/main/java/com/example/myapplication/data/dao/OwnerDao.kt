package com.example.myapplication.data.dao

import androidx.room3.Dao
import androidx.room3.Insert
import com.example.myapplication.data.entity.OwnerEntity

@Dao
interface OwnerDao {
    @Insert
    suspend fun insertOwner(owner: OwnerEntity): Long
}