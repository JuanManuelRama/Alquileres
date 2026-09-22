package com.example.myapplication.data.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Transaction
import com.example.myapplication.data.entity.PropertyEntity
import com.example.myapplication.data.entity.PropertyWithOwner
import com.example.myapplication.data.entity.ManagedProperty
import com.example.myapplication.data.entity.OwnerEntity
import com.example.myapplication.model.Address
import kotlinx.coroutines.flow.Flow

@Dao
interface PropertyDao {
    @Insert
    suspend fun insertProperty(property: PropertyEntity): Long

    @Transaction
    @Query("SELECT * FROM Property")
    fun getAllPropertiesWithOwners(): Flow<List<PropertyWithOwner>>

    @Transaction
    @Query("SELECT * FROM Property WHERE isActive = 1")
    fun observeActive(): Flow<List<PropertyWithOwner>>

    @Transaction
    @Query("SELECT * FROM Property WHERE id = :id")
    suspend fun getById(id: Long): PropertyWithOwner?

    @Transaction
    @Query("""
        SELECT p.*,
            (SELECT COUNT(*) FROM Contract c WHERE c.propertyId = p.id) AS contractCount,
            (SELECT COUNT(*) FROM Contract c WHERE c.propertyId = p.id AND c.isFinished = 0) AS activeContractCount
        FROM Property p
        ORDER BY p.addr_street COLLATE NOCASE, p.addr_number COLLATE NOCASE, p.addr_unit COLLATE NOCASE, p.id
    """)
    fun observeManagement(): Flow<List<ManagedProperty>>

    @Query("UPDATE Property SET addr_street = :street, addr_number = :number, addr_unit = :unit, ownerId = :ownerId WHERE id = :id")
    suspend fun updateDetails(id: Long, street: String, number: String, unit: String?, ownerId: Long): Int

    @Insert
    suspend fun insertOwner(owner: OwnerEntity): Long

    @Transaction
    suspend fun saveDetails(id: Long, address: Address, owner: OwnerEntity) {
        val ownerId = if (owner.id == 0L) insertOwner(owner) else owner.id
        check(updateDetails(id, address.street, address.number, address.unit, ownerId) == 1) {
            "Property no longer exists"
        }
    }

    @Query("""
        UPDATE Property SET isActive = :active WHERE id = :id
        AND (:active = 1 OR NOT EXISTS (
            SELECT 1 FROM Contract WHERE propertyId = :id AND isFinished = 0
        ))
    """)
    suspend fun setActive(id: Long, active: Boolean): Int

    @Query("DELETE FROM Contract WHERE propertyId = :id")
    suspend fun deleteContracts(id: Long)

    @Query("DELETE FROM Property WHERE id = :id")
    suspend fun deleteProperty(id: Long): Int

    @Query("DELETE FROM Service WHERE id NOT IN (SELECT serviceId FROM ContractServiceCrossRef)")
    suspend fun deleteUnreferencedServices()

    @Transaction
    suspend fun deleteWithContracts(id: Long) {
        deleteContracts(id)
        check(deleteProperty(id) == 1) { "Property no longer exists" }
        deleteUnreferencedServices()
    }
}
