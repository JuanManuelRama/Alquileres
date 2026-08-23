package com.example.myapplication.model

import java.math.BigDecimal

data class ServicePayment(
    val service: Service,
    val amount: BigDecimal,
    val id: Long? = null
)
