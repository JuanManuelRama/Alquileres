package ar.controlalq.data.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import ar.controlalq.data.entity.ServiceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceDao {
    @Query("SELECT * FROM Service")
    fun getAll(): Flow<List<ServiceEntity>>

    @Insert
    suspend fun insert(service: ServiceEntity): Long

    @Query("""
      DELETE FROM Service
      WHERE id NOT IN (
          SELECT serviceId FROM ContractServiceCrossRef
      )
    """)
    suspend fun deleteUnreferenced()
}
