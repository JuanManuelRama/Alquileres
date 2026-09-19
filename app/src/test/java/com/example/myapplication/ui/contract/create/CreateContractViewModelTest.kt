package com.example.myapplication.ui.contract.create

import com.example.myapplication.ui.SaveStatus

import androidx.lifecycle.viewModelScope

import com.example.myapplication.data.repository.*
import com.example.myapplication.data.dao.*
import com.example.myapplication.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class CreateContractViewModelTest {
    private val draft = Contract(
        Property(Address("Main", "1", null), Owner("Owner")), Tenant("Tenant", "16502530000"),
        BigDecimal("1000"), YearMonth.of(2026, 1), YearMonth.of(2026, 12), emptyList(), emptyList()
    )

    @Test fun waitsForCompletionRejectsDuplicatesAndAllowsRetryAfterFailure() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        var modelToClear: CreateContractViewModel? = null
        try {
            val first = CompletableDeferred<Contract>()
            val second = CompletableDeferred<Contract>()
            var calls = 0
            val writer = object : ContractWriter {
                override suspend fun insert(contract: Contract, renewingContractId: Long?): Contract {
                    assertEquals(42L, renewingContractId)
                    return if (++calls == 1) first.await() else second.await()
                }
            }
            val model = CreateContractViewModel(
                PropertyRepository(readDao<PropertyDao>()), OwnerRepository(readDao<OwnerDao>()),
                ServiceRepository(readDao<ServiceDao>()), writer
            )
            modelToClear = model
            model.saveContract(draft, 42)
            model.saveContract(draft, 42)
            runCurrent()
            assertEquals(1, calls)
            assertEquals(SaveStatus.Saving, model.saveState.value)
            first.completeExceptionally(IllegalStateException("Database rejected write"))
            runCurrent()
            assertEquals(SaveStatus.Failed, model.saveState.value)
            model.saveContract(draft, 42)
            runCurrent()
            assertEquals(2, calls)
            assertEquals(SaveStatus.Saving, model.saveState.value)
            second.complete(draft.copy(id = 5))
            runCurrent()
            assertEquals(SaveStatus.Saved, model.saveState.value)
            model.saveContract(draft, 42)
            runCurrent()
            assertEquals(2, calls)
        } finally {
            modelToClear?.viewModelScope?.cancel()
            runCurrent()
            Dispatchers.resetMain()
        }
    }

    private inline fun <reified T> readDao(): T = java.lang.reflect.Proxy.newProxyInstance(
        T::class.java.classLoader, arrayOf(T::class.java)
    ) { _, _, _ -> flowOf(emptyList<Any>()) } as T
}
