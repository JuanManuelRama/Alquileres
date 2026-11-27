package ar.controlalq.data

import androidx.room3.ColumnTypeConverters
import androidx.room3.Database
import androidx.room3.RoomDatabase
import ar.controlalq.data.dao.BillDao
import ar.controlalq.data.dao.ContractDao
import ar.controlalq.data.dao.OwnerDao
import ar.controlalq.data.dao.PropertyDao
import ar.controlalq.data.dao.ServiceDao
import ar.controlalq.data.entity.AdjustmentEntity
import ar.controlalq.data.entity.BillEntity
import ar.controlalq.data.entity.BillItemEntity
import ar.controlalq.data.entity.ContractEntity
import ar.controlalq.data.entity.ContractServiceCrossRef
import ar.controlalq.data.entity.OwnerEntity
import ar.controlalq.data.entity.PropertyEntity
import ar.controlalq.data.entity.ServiceEntity

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
    version = 22,
    exportSchema = true
)
@ColumnTypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun billDao(): BillDao
    abstract fun propertyDao(): PropertyDao
    abstract fun ownerDao(): OwnerDao
    abstract fun serviceDao(): ServiceDao
    abstract fun contractDao(): ContractDao
}
