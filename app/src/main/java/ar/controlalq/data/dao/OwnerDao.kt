package ar.controlalq.data.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import ar.controlalq.data.entity.OwnerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OwnerDao {
    @Query(value = "SELECT * FROM Owner")
    fun getAll(): Flow<List<OwnerEntity>>
    @Insert
    suspend fun insert(owner: OwnerEntity): Long

    @Query("DELETE FROM Owner WHERE id NOT IN (SELECT ownerId FROM Property)")
    suspend fun deleteUnreferenced()
}
