package com.example.myapplication.data.repository

import com.example.myapplication.data.dao.ServiceDao
import com.example.myapplication.data.entity.toEntity
import com.example.myapplication.model.Service
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
