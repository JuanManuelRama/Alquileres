package com.example.myapplication.model

import kotlinx.serialization.Serializable

@Serializable
data class Property(
    val address: Address,
    val owner: Owner,
    val isActive: Boolean = true,
    val id: Long = 0L

) {
    override fun toString(): String {
        return address.toString()
    }
}
