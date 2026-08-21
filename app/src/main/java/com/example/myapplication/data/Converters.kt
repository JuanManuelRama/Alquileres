package com.example.myapplication.data

import androidx.room3.ColumnTypeConverter
import java.math.BigDecimal
import java.time.LocalDate

class Converters {
    @ColumnTypeConverter
    fun fromTimestamp(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it) }
    }

    @ColumnTypeConverter
    fun dateToTimestamp(date: LocalDate?): String? {
        return date?.toString()
    }

    @ColumnTypeConverter
    fun fromBigDecimal(value: String?): BigDecimal? {
        return value?.toBigDecimalOrNull()
    }

    @ColumnTypeConverter
    fun bigDecimalToString(value: BigDecimal?): String? {
        return value?.toString()
    }
}