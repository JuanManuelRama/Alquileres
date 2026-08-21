package com.example.myapplication.model

data class Payment(
    val contract: Contract,
    val services: List<Service>
)
