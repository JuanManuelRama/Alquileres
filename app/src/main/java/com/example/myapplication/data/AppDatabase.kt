package com.example.myapplication.data

import androidx.room3.ColumnTypeConverters
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
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
    version = 22,
    exportSchema = false
)
@ColumnTypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase() {
    companion object {
        val MIGRATION_21_22 = object : Migration(21, 22) {
            override suspend fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE Adjustment ADD COLUMN frozen INTEGER NOT NULL DEFAULT 0")
                // Older records do not retain whether a rate was fixed at creation.
                connection.execSQL("""
                    UPDATE Adjustment SET frozen = 1
                    WHERE EXISTS (
                        SELECT 1 FROM Bill b WHERE b.contractId = Adjustment.contractId
                            AND b.billingMonth = Adjustment.time AND b.paidAt IS NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_20_21 = object : Migration(20, 21) {
            override suspend fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE Property ADD COLUMN isActive INTEGER NOT NULL DEFAULT 1")
            }
        }

        val MIGRATION_19_20 = object : Migration(19, 20) {
            override suspend fun migrate(connection: SQLiteConnection) {
                connection.execSQL("""
                    CREATE TABLE ContractServiceCrossRef_new (
                        contractId INTEGER NOT NULL,
                        serviceId INTEGER NOT NULL,
                        PRIMARY KEY(contractId, serviceId),
                        FOREIGN KEY(contractId) REFERENCES Contract(id) ON DELETE CASCADE,
                        FOREIGN KEY(serviceId) REFERENCES Service(id) ON DELETE NO ACTION
                    )
                """.trimIndent())
                connection.execSQL("INSERT INTO ContractServiceCrossRef_new SELECT contractId, serviceId FROM ContractServiceCrossRef")
                connection.execSQL("DROP TABLE ContractServiceCrossRef")
                connection.execSQL("ALTER TABLE ContractServiceCrossRef_new RENAME TO ContractServiceCrossRef")
                connection.execSQL("CREATE INDEX index_ContractServiceCrossRef_serviceId ON ContractServiceCrossRef(serviceId)")
            }
        }

        val MIGRATION_18_19 = object : Migration(18, 19) {
            override suspend fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE Contract ADD COLUMN isFinished INTEGER NOT NULL DEFAULT 0")
            }
        }
    }

    abstract fun billDao(): BillDao
    abstract fun propertyDao(): PropertyDao
    abstract fun ownerDao(): OwnerDao
    abstract fun serviceDao(): ServiceDao
    abstract fun contractDao(): ContractDao
}
