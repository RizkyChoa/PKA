package com.example

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.repository.FinanceRepository
import com.example.data.sample.InitialDataSeeder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PayableTransactionTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: FinanceRepository

    @Before
    fun setUp() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Application>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        InitialDataSeeder.seedIfEmpty(db)
        repository = FinanceRepository(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testPayableCreationCreatesTransactionAndAdjustsAccount() = runBlocking {
        val accountsBefore = repository.allAccounts.first()
        val kasUtama = accountsBefore.first { it.id == 1L }
        assertEquals(0.0, kasUtama.currentBalance, 0.01)

        val payableId = repository.insertPayableWithTransaction(
            creditorName = "PT Semen Ready Mix",
            type = "SUPPLIER",
            description = "Utang bahan semen",
            totalAmount = 15000000.0,
            dueDate = "2026-10-15",
            destinationAccountId = 1L,
            transactionDate = "2026-10-01"
        )

        assertTrue(payableId > 0)

        // Check account balance updated
        val accountsAfter = repository.allAccounts.first()
        val kasUtamaAfter = accountsAfter.first { it.id == 1L }
        assertEquals(15000000.0, kasUtamaAfter.currentBalance, 0.01)

        // Check transaction created
        val trxs = repository.allTransactions.first()
        assertEquals(1, trxs.size)
        val trx = trxs.first()
        assertEquals("MONEY_IN", trx.type)
        assertEquals(15000000.0, trx.amount, 0.01)
        assertEquals("DEBT", trx.classification)
        assertEquals(payableId, trx.payableId)
    }
}
