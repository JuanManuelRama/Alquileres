package com.example.myapplication.model

data class Address(
    val street: String,
    val number: String,
    val unit: String?,
) {
    override fun toString(): String {
        return listOfNotNull(street, number, unit).joinToString(" ")
    }
}
