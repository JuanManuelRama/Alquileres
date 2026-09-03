package com.example.myapplication.model

import java.math.BigDecimal
import java.time.LocalDate

data class Payment(
    val contract: Contract,
    val services: List<ServicePayment>,
    val rent: BigDecimal,
    val date: LocalDate,
    val id: Long = 0L
) {
   val total: BigDecimal = rent + services.sumOf { it.amount }
}
