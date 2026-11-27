package ar.controlalq.data.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Transaction
import ar.controlalq.data.entity.ManagedProperty
import ar.controlalq.data.entity.PropertyEntity
import ar.controlalq.data.entity.PropertyWithOwner
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

    @Query("SELECT EXISTS(SELECT 1 FROM Property WHERE id = :id AND isActive = 1)")
    suspend fun isActive(id: Long): Boolean

    @Transaction
    @Query("""
        SELECT p.*, p.isActive AS propertyActive,
            EXISTS (SELECT 1 FROM Contract c WHERE c.propertyId = p.id AND c.isFinished = 0) AS hasActiveContracts
        FROM Property p
        ORDER BY p.addr_street COLLATE NOCASE, p.addr_number COLLATE NOCASE, p.addr_unit COLLATE NOCASE, p.id
    """)
    fun observeManagement(): Flow<List<ManagedProperty>>

    @Query("UPDATE Property SET addr_street = :street, addr_number = :number, addr_unit = :unit, ownerId = :ownerId WHERE id = :id")
    suspend fun updateDetails(id: Long, street: String, number: String, unit: String?, ownerId: Long): Int

    @Query("""
        UPDATE Property SET isActive = :active WHERE id = :id
        AND (:active = 1 OR NOT EXISTS (
            SELECT 1 FROM Contract WHERE propertyId = :id AND isFinished = 0
        ))
    """)
    suspend fun setActive(id: Long, active: Boolean): Int

    @Query("DELETE FROM Property WHERE id = :id")
    suspend fun deleteProperty(id: Long): Int
}
