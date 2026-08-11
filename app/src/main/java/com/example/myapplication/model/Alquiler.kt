package com.example.myapplication.model

import java.math.BigDecimal
import java.time.LocalDate

data class Alquiler (
    val id: Int,
    val propiedad: Propiedad,
    val costoInicial: BigDecimal,
    val costoActual: BigDecimal = costoInicial,
    val servicios: List<Servicio>,
    val firma: LocalDate,
    val vencimiento: LocalDate,
    val ajuste: Long,
) {
    fun mesesAjuste(): Sequence<LocalDate> =
        generateSequence(firma.plusMonths(ajuste)) { it.plusMonths(ajuste) }
            .takeWhile { it.isBefore(vencimiento) }

}
