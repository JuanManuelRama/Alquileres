package com.example.myapplication.data.repository

import com.example.myapplication.data.dao.PropertyDao
import com.example.myapplication.model.Property
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PropertyRepository(private val propertyDao: PropertyDao) {

    fun getAllProperties(): Flow<List<Property>> {
        return propertyDao.getAllPropertiesWithOwners().map { list ->
            list.map { it.toDomain() }
        }
    }
}