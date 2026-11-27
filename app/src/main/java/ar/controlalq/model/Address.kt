package ar.controlalq.model

import kotlinx.serialization.Serializable

@Serializable
data class Address(
    val street: String,
    val number: String,
    val unit: String?,
) {
    override fun toString(): String {
        return listOfNotNull(street, number, unit).joinToString(" ")
    }
}
