package com.example.myapplication.model

data class Property(
    val address: Address,
    val owner: Owner

) {
    override fun toString(): String {
        return address.toString()
    }
}