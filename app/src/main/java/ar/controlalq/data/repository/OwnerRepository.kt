package ar.controlalq.data.repository

import ar.controlalq.data.dao.OwnerDao
import ar.controlalq.data.entity.toEntity
import ar.controlalq.model.Owner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OwnerRepository(private val dao: OwnerDao) {
    fun getAll(): Flow<List<Owner>> {
        return dao.getAll().map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun insert(owner: Owner): Owner {
        val id = dao.insert(owner.toEntity())
        return owner.copy(id = id)
    }
}
