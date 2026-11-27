package ar.controlalq.data.repository

import ar.controlalq.data.dao.ServiceDao
import ar.controlalq.data.entity.toEntity
import ar.controlalq.model.Service
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ServiceRepository(private val dao: ServiceDao) {
    fun getAll(): Flow<List<Service>> {
        return dao.getAll().map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun insert(service: Service): Service {
        val id = dao.insert(service.toEntity())
        return service.copy(id = id)
    }
}
