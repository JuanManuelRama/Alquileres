package com.example.myapplication.model

import androidx.room3.Embedded
import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity
data class Propiedad (
    @PrimaryKey val id: Long,
    @Embedded
    val direccion: Direccion,
    @Embedded
    val propietario: Propietario
)
