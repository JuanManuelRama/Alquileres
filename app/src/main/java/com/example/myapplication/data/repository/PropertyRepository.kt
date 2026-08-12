package com.example.myapplication.data.repository

import com.example.myapplication.data.dao.PropertyDao
import com.example.myapplication.data.entity.PropertyEntity
import com.example.myapplication.data.entity.toEntity
import com.example.myapplication.model.Property
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PropertyRepository(private val propertyDao: PropertyDao) {
    fun getAll(): Flow<List<Property>> {
        return propertyDao.getAllPropertiesWithOwners().map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun insert(property: Property) {
        propertyDao.insertProperty(property.toEntity())
    }
}