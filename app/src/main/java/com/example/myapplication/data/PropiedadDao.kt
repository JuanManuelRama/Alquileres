package com.example.myapplication.data

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import com.example.myapplication.model.Propiedad
import kotlinx.coroutines.flow.Flow

@Dao
interface PropiedadDao {
    @Query("SELECT * FROM Propiedad")
    fun getAll(): Flow<List<Propiedad>>

    @Insert
    suspend fun insert(propiedad: Propiedad)
}