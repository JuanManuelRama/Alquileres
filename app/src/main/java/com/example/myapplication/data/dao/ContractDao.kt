package com.example.myapplication.data.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Transaction
import com.example.myapplication.data.entity.AdjustmentEntity
import com.example.myapplication.data.entity.BasicContract
import com.example.myapplication.data.entity.ContractEntity
import com.example.myapplication.data.entity.ContractServiceCrossRef
import com.example.myapplication.data.entity.ContractWithRelations
import com.example.myapplication.data.entity.ContractWithStatus
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal
import java.time.YearMonth

@Dao
interface ContractDao {
    @Query("""
        SELECT c.*,
            p.id AS property_id, p.ownerId AS property_ownerId,
            p.addr_street AS property_addr_street,
            p.addr_number AS property_addr_number,
            p.addr_unit AS property_addr_unit,
            o.id AS owner_id, o.name AS owner_name,
            (c.startingMonth <= :month AND c.finishingMonth >= :month
                AND NOT EXISTS (
                    SELECT 1 FROM Bill b
                    WHERE b.contractId = c.id AND b.paidAt IS NOT NULL
                        AND b.billingMonth = :month
                )) AS paymentDue,
            EXISTS (
                SELECT 1 FROM Adjustment a
                WHERE a.contractId = c.id
                    AND a.time = :month
            ) AS hikeThisMonth
        FROM Contract c
        INNER JOIN Property p ON p.id = c.propertyId
        INNER JOIN Owner o ON o.id = p.ownerId
        WHERE c.isFinished = 0
    """)
    fun observeWithStatus(month: YearMonth): Flow<List<ContractWithStatus>>

    @Query("""
        SELECT c.*,
            p.id AS property_id, p.ownerId AS property_ownerId,
            p.addr_street AS property_addr_street,
            p.addr_number AS property_addr_number,
            p.addr_unit AS property_addr_unit,
            o.id AS owner_id, o.name AS owner_name
        FROM Contract c
        INNER JOIN Property p ON p.id = c.propertyId
        INNER JOIN Owner o ON o.id = p.ownerId
        WHERE c.isFinished = 1
    """)
    fun observeInactive(): Flow<List<BasicContract>>

    @Transaction
    @Query("SELECT * FROM Contract")
    fun getAllContracts(): Flow<List<ContractWithRelations>>

    @Transaction
    @Query("SELECT * FROM Contract WHERE id = :contractId")
    fun observeById(contractId: Long): Flow<ContractWithRelations?>

    @Insert
    suspend fun insertContract(contract: ContractEntity): Long

    @Insert
    suspend fun insertContractService(contractRefService: List<ContractServiceCrossRef>)

    @Insert
    suspend fun insertAdjustments(adjustments: List<AdjustmentEntity>)

    @Query("UPDATE Contract SET isFinished = 1 WHERE id = :contractId")
    suspend fun finish(contractId: Long): Int

    @Query("UPDATE Contract SET finishingMonth = :month WHERE id = :contractId AND finishingMonth > :month")
    suspend fun shortenTerm(contractId: Long, month: YearMonth): Int

    @Query("DELETE FROM Adjustment WHERE contractId = :contractId AND time > :cancellationMonth")
    suspend fun deleteFutureAdjustments(contractId: Long, cancellationMonth: YearMonth): Int

    @Query("DELETE FROM Contract WHERE id = :contractId")
    suspend fun deleteContract(contractId: Long): Int

    @Query("""
        UPDATE Adjustment
        SET amount = :amount
        WHERE contractId = :contractId
            AND time = :time
            AND EXISTS (
                SELECT 1 FROM Contract WHERE id = :contractId AND isFinished = 0)
            AND NOT EXISTS (
                SELECT 1 FROM Bill WHERE contractId = :contractId AND billingMonth >= :time AND paidAt IS NOT NULL
            )
            """)
    suspend fun updateAdjustmentAmount(contractId: Long, time: YearMonth, amount: BigDecimal?): Int

    @Transaction
    suspend fun saveContract(
        contract: ContractEntity,
        serviceIds: List<Long>,
        adjustments: List<AdjustmentEntity>
    ): Long {
        val contractId = insertContract(contract)
        insertContractService(serviceIds.map { ContractServiceCrossRef(contractId, it) } )
        insertAdjustments(adjustments.map { it.copy(contractId = contractId) })
        return contractId
    }

}
