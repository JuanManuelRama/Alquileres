package com.example.myapplication.data.repository

import com.example.myapplication.data.dao.OwnerDao
import com.example.myapplication.data.entity.toEntity
import com.example.myapplication.model.Owner
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
