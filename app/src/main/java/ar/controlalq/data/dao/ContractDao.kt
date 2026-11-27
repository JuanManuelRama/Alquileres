package ar.controlalq.data.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Transaction
import ar.controlalq.data.entity.AdjustmentEntity
import ar.controlalq.data.entity.BasicContract
import ar.controlalq.data.entity.ContractEntity
import ar.controlalq.data.entity.ContractServiceCrossRef
import ar.controlalq.data.entity.ContractWithRelations
import ar.controlalq.data.entity.ContractWithStatus
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal
import java.time.YearMonth

@Dao
interface ContractDao {
    @Query("""
        SELECT c.*,
            p.id AS property_id, p.ownerId AS property_ownerId, p.isActive AS property_isActive,
            p.addr_street AS property_addr_street,
            p.addr_number AS property_addr_number,
            p.addr_unit AS property_addr_unit,
            o.id AS owner_id, o.name AS owner_name,
            (c.startingMonth <= :month AND c.finishingMonth >= :month
                AND NOT EXISTS (
                    SELECT 1 FROM Bill b
                    WHERE b.contractId = c.id AND b.paidAt IS NOT NULL
                        AND b.billingMonth = :month
                ) AND EXISTS (
                    SELECT 1 FROM Bill b
                    WHERE b.contractId = c.id AND b.billingMonth = :month
                        AND NOT EXISTS (
                            SELECT 1 FROM BillItem i WHERE i.billId = b.id
                                AND (i.amount = '' OR trim(i.description, char(9,10,11,12,13,32,133,160,5760,8192,8193,8194,8195,8196,8197,8198,8199,8200,8201,8202,8232,8233,8239,8287,12288)) = '')
                        )
                )) AS paymentDue,
            (c.startingMonth <= :month AND c.finishingMonth >= :month
                AND NOT EXISTS (
                    SELECT 1 FROM Bill b
                    WHERE b.contractId = c.id AND b.billingMonth = :month
                        AND (b.paidAt IS NOT NULL OR NOT EXISTS (
                            SELECT 1 FROM BillItem i WHERE i.billId = b.id
                                AND (i.amount = '' OR trim(i.description, char(9,10,11,12,13,32,133,160,5760,8192,8193,8194,8195,8196,8197,8198,8199,8200,8201,8202,8232,8233,8239,8287,12288)) = '')
                        ))
                )) AS billIncomplete,
            EXISTS (
                SELECT 1 FROM Adjustment a
                WHERE a.contractId = c.id
                    AND a.time = :month
            ) AND NOT EXISTS (
                SELECT 1 FROM Bill b
                WHERE b.contractId = c.id AND b.billingMonth = :month AND b.paidAt IS NOT NULL
            ) AS hikeThisMonth
        FROM Contract c
        INNER JOIN Property p ON p.id = c.propertyId
        INNER JOIN Owner o ON o.id = p.ownerId
        WHERE c.isFinished = 0
    """)
    fun observeWithStatus(month: YearMonth): Flow<List<ContractWithStatus>>

    @Query("""
        SELECT c.*,
            p.id AS property_id, p.ownerId AS property_ownerId, p.isActive AS property_isActive,
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

    @Query("DELETE FROM Contract WHERE propertyId = :propertyId")
    suspend fun deleteByProperty(propertyId: Long): Int

    @Query("""
        UPDATE Adjustment
        SET amount = :amount
        WHERE contractId = :contractId
            AND time = :time
            AND frozen = 0
            AND EXISTS (
                SELECT 1 FROM Contract WHERE id = :contractId AND isFinished = 0)
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
