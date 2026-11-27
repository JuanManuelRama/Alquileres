package ar.controlalq.model

import java.math.BigDecimal
import java.time.YearMonth

data class Adjustment(
    val time: YearMonth,
    val amount: BigDecimal? = null,
    val frozen: Boolean = false,
)
