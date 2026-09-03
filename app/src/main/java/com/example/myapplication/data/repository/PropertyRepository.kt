package com.example.myapplication.data.repository

import com.example.myapplication.data.dao.PropertyDao
import com.example.myapplication.data.entity.toEntity
import com.example.myapplication.model.Property
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PropertyRepository(private val dao: PropertyDao) {
    fun getAll(): Flow<List<Property>> {
        return dao.getAllPropertiesWithOwners().map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun insert(property: Property): Property {
        val id = dao.insertProperty(property.toEntity())
        return property.copy(id = id)
    }
}
