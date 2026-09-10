package com.example.myapplication.data

import androidx.room3.ColumnTypeConverters
import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.example.myapplication.data.dao.BillDao
import com.example.myapplication.data.dao.ContractDao
import com.example.myapplication.data.dao.OwnerDao
import com.example.myapplication.data.dao.PropertyDao
import com.example.myapplication.data.dao.ServiceDao
import com.example.myapplication.data.entity.AdjustmentEntity
import com.example.myapplication.data.entity.BillEntity
import com.example.myapplication.data.entity.BillItemEntity
import com.example.myapplication.data.entity.ContractEntity
import com.example.myapplication.data.entity.ContractServiceCrossRef
import com.example.myapplication.data.entity.OwnerEntity
import com.example.myapplication.data.entity.PropertyEntity
import com.example.myapplication.data.entity.ServiceEntity

@Database(
    entities = [
        PropertyEntity::class,
        OwnerEntity::class,
        ServiceEntity::class,
        ContractEntity::class,
        ContractServiceCrossRef::class,
        AdjustmentEntity::class,
        BillEntity::class,
        BillItemEntity::class
    ],
    version = 15,
    exportSchema = false
)
@ColumnTypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun billDao(): BillDao
    abstract fun propertyDao(): PropertyDao
    abstract fun ownerDao(): OwnerDao
    abstract fun serviceDao(): ServiceDao
    abstract fun contractDao(): ContractDao
}
