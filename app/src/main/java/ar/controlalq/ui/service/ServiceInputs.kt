package ar.controlalq.ui.service

import kotlinx.serialization.Serializable

@Serializable
data class ServiceInputs(
    val name: String = ""
)
