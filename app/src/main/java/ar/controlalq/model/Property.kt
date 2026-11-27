package ar.controlalq.model

import kotlinx.serialization.Serializable

@Serializable
data class Property(
    val address: Address,
    val owner: Owner,
    val id: Long = 0L

) {
    override fun toString(): String {
        return address.toString()
    }
}
