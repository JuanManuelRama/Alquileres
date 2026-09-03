package com.example.myapplication.data.repository

import com.example.myapplication.data.dao.TenantDao
import com.example.myapplication.data.entity.toEntity
import com.example.myapplication.model.Tenant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TenantRepository (private val dao: TenantDao) {
    fun getAll(): Flow<List<Tenant>> {
        return dao.getAll().map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun insert(tenant: Tenant): Tenant {
        val id = dao.insert(tenant.toEntity())
        return tenant.copy(id = id)
    }
}
