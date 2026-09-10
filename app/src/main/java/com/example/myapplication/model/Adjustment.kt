package com.example.myapplication.model

import java.math.BigDecimal
import java.time.YearMonth

data class Adjustment(
    val time: YearMonth,
    val amount: BigDecimal? = null,
)
