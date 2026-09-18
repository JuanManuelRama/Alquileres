package com.example.myapplication.ui.contract.bill

import com.example.myapplication.ui.util.toMoney
import java.math.BigDecimal


internal fun BigDecimal.toCurrencyString(): String = toMoney()
