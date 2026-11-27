package ar.controlalq.model

import kotlinx.serialization.Serializable

@Serializable
data class Service(
    val name: String,
    val id: Long = 0L
)
