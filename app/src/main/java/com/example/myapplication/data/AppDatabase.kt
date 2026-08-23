package com.example.myapplication.data

import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.ColumnTypeConverters
import com.example.myapplication.data.dao.ContractDao
import com.example.myapplication.data.dao.OwnerDao
import com.example.myapplication.data.dao.PropertyDao
import com.example.myapplication.data.dao.ServiceDao
import com.example.myapplication.data.dao.TenantDao
import com.example.myapplication.data.entity.AdjustmentEntity
import com.example.myapplication.data.entity.ContractEntity
import com.example.myapplication.data.entity.ContractServiceCrossRef
import com.example.myapplication.data.entity.OwnerEntity
import com.example.myapplication.data.entity.PropertyEntity
import com.example.myapplication.data.entity.ServiceEntity
import com.example.myapplication.data.entity.TenantEntity

@Database(
    entities = [
        PropertyEntity::class,
        OwnerEntity::class,
        TenantEntity::class,
        ServiceEntity::class,
        ContractEntity::class,
        ContractServiceCrossRef::class,
        AdjustmentEntity::class
    ],
    version = 6,
    exportSchema = false
)
@ColumnTypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun propertyDao(): PropertyDao
    abstract fun ownerDao(): OwnerDao
    abstract fun tenantDao(): TenantDao
    abstract fun serviceDao(): ServiceDao
    abstract fun contractDao(): ContractDao

}