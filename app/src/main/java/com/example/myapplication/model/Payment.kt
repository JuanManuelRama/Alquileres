package com.example.myapplication.model

import java.math.BigDecimal

data class Payment(
    val contract: Contract,
    val services: List<ServicePayment>,
    val rent: BigDecimal,
    val id: Long? = null
) {
   val total: BigDecimal = rent + services.sumOf { it.amount }
}
