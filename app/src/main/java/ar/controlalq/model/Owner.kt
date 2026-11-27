package ar.controlalq.model

import kotlinx.serialization.Serializable

@Serializable
data class Owner(
    val name: String,
    val id: Long = 0L
)
