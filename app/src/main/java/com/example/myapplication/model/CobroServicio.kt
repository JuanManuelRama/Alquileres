package com.example.myapplication.model

import java.math.BigDecimal

data class CobroServicio (
    val servicio: Servicio,
    val importe: BigDecimal,
)
