package com.example.myapplication.data

import androidx.room3.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.myapplication.model.Direccion
import com.example.myapplication.model.Propiedad
import com.example.myapplication.model.Propietario
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PropiedadDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: PropiedadDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()

        dao = database.propiedadDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetAll() = runTest {
        val propiedad = Propiedad(
            id = 1,
            direccion = Direccion(
                calle = "Vieytes",
                altura = "1897",
                piso = "4",
                departamento = null
            ),
            propietario = Propietario(
                nombre = "pepe"
            )
        )
        dao.insert(propiedad)

        val propiedades = dao.getAll()

        assertEquals(listOf(propiedad), propiedades.first())
    }
}