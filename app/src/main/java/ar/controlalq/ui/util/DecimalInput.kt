package ar.controlalq.ui.util

import java.math.BigDecimal

/**
 * Parses ungrouped form input, accepting either keyboard's decimal separator.
 * Grouping is added only for display by NumberVisualTransformation.
 * Mixed or repeated separators remain invalid; draft values survive language changes.
 */
fun String.toInputBigDecimalOrNull(): BigDecimal? = replace(',', '.').toBigDecimalOrNull()

fun String.toInputBigDecimal(): BigDecimal = replace(',', '.').toBigDecimal()
