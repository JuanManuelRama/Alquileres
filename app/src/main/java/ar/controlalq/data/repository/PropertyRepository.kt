package ar.controlalq.data.repository

import androidx.room3.withWriteTransaction
import ar.controlalq.data.AppDatabase
import ar.controlalq.data.dao.OwnerDao
import ar.controlalq.data.dao.PropertyDao
import ar.controlalq.data.entity.toEntity
import ar.controlalq.model.Owner
import ar.controlalq.model.Property
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface PropertyReader {
    fun getActive(): Flow<List<Property>>
}

class PropertyRepository(private val db: AppDatabase) : PropertyReader {
    private val dao: PropertyDao = db.propertyDao()
    private val ownerDao: OwnerDao = db.ownerDao()
    fun observeManagement() = dao.observeManagement()

    override fun getActive(): Flow<List<Property>> = dao.observeActive().map { list -> list.map { it.toDomain() } }

    suspend fun update(property: Property) {
        db.withWriteTransaction {
            val owner = saveOwner(property.owner)
            val address = property.address
            check(dao.updateDetails(property.id, address.street, address.number, address.unit, owner.id) == 1) {
                "Property no longer exists"
            }
        }
    }

    suspend fun setActive(id: Long, active: Boolean) {
        check(dao.setActive(id, active) == 1) { "Property is missing or has active contracts" }
    }

    suspend fun delete(id: Long) {
        db.withWriteTransaction {
            db.contractDao().deleteByProperty(id)
            check(dao.deleteProperty(id) == 1)
            db.serviceDao().deleteUnreferenced()
            ownerDao.deleteUnreferenced()
        }
    }

    fun getAll(): Flow<List<Property>> {
        return dao.getAllPropertiesWithOwners().map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun insert(property: Property): Property = db.withWriteTransaction {
        val saved = property.copy(owner = saveOwner(property.owner))
        val id = dao.insertProperty(saved.toEntity())
        saved.copy(id = id)
    }

    private suspend fun saveOwner(owner: Owner): Owner =
        if (owner.id == 0L) owner.copy(id = ownerDao.insert(owner.toEntity())) else owner
}
