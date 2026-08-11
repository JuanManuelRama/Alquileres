package com.example.myapplication.model

import java.time.LocalDate

data class Cobro (
    val alquiler: Alquiler,
    val mes: LocalDate,
    val servicios: List<CobroServicio>,
    val reintegros: List<Reintegro> = emptyList(),
) {
    val huboAjuste: Boolean = alquiler.mesesAjuste().contains(mes)

    val total = alquiler.costoActual +
            servicios.sumOf { it.importe } -
            reintegros.sumOf { it.monto }

    init {
        if (mes.isBefore(alquiler.firma) || mes.isAfter(alquiler.vencimiento)) {
            throw IllegalArgumentException("El mes del cobro debe estar entre la firma y el vencimiento del alquiler")
        }
        if (servicios.map {it.servicio } != alquiler.servicios) {
            throw IllegalArgumentException("Los servicios del cobro deben coincidir con los servicios del alquiler")
        }
    }
}
