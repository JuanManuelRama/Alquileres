package com.example.myapplication.model

data class Property(
    val address: Address,
    val owner: Owner,
    val id: Long? = null

) {
    override fun toString(): String {
        return address.toString()
    }
}