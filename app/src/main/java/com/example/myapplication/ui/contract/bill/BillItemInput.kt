package com.example.myapplication.ui.contract.bill

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import java.util.UUID

internal class BillItemInput(
    description: String,
    amount: String,
    val isExtra: Boolean,
    val key: String = UUID.randomUUID().toString()
) {
    var description by mutableStateOf(description)
    var amount by mutableStateOf(amount)
}

internal val BillItemsSaver = listSaver<SnapshotStateList<BillItemInput>, String>(
    save = { rows -> rows.flatMap { listOf(it.key, it.description, it.amount, it.isExtra.toString()) } },
    restore = { saved ->
        saved.chunked(4).map { (key, description, amount, isExtra) ->
            BillItemInput(description, amount, isExtra.toBoolean(), key)
        }.toMutableStateList()
    }
)
