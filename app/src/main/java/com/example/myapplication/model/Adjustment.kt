package com.example.myapplication.model

import java.math.BigDecimal
import java.time.LocalDate

data class Adjustment(
    val time: LocalDate,
    val amount: BigDecimal? = null,
)
