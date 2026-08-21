package com.example.myapplication.ui.components.form

object Validations {
    val isBigDecimal: (String) -> Boolean = { it.toBigDecimalOrNull() != null }
    val isLong: (String) -> Boolean = { it.toLongOrNull() != null }

}