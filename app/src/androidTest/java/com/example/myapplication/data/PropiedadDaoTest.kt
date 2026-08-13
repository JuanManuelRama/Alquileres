package com.example.myapplication.data

import androidx.room3.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.myapplication.data.entity.OwnerEntity
import com.example.myapplication.data.entity.PropertyEntity
import com.example.myapplication.data.repository.PropertyRepository
import com.example.myapplication.model.Address
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PropertyRepositoryTest {

    private lateinit var database: AppDatabase
    private lateinit var propertyRepository: PropertyRepository

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        )
            // Allow running database operations synchronously on the main thread for tests
            .allowMainThreadQueries()
            .build()

        propertyRepository = PropertyRepository(database.propertyDao())
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetAllPropertiesWithOwners() = runTest {
        val ownerId = database.ownerDao().insert(OwnerEntity(name = "pepe"))
        val address = Address(
            street = "Oak",
            number = "430",
            unit = null
        )
        database.propertyDao().insertProperty(
            PropertyEntity(
                address = address,
                ownerId = ownerId
            )
        )

        // 3. Retrieve from Repository
        val properties = propertyRepository.getAll().first()

        // 4. Assert against the converted domain model
        assertEquals("pepe", properties.first().owner.name)
        assertEquals(address, properties.first().address)
    }
}