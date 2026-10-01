package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AccountEntity
import com.example.data.model.AuditLogEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.PayableEntity
import com.example.data.model.ProfitPartnerEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.ReceivableEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.WorkerInvoiceEntity
import com.example.data.model.WorkerJobItemEntity
import com.example.data.model.WorkerLoanItemEntity

@Database(
    entities = [
        AccountEntity::class,
        ProjectEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        ReceivableEntity::class,
        PayableEntity::class,
        AuditLogEntity::class,
        WorkerInvoiceEntity::class,
        WorkerJobItemEntity::class,
        WorkerLoanItemEntity::class,
        ProfitPartnerEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun projectDao(): ProjectDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun receivableDao(): ReceivableDao
    abstract fun payableDao(): PayableDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun workerInvoiceDao(): WorkerInvoiceDao
    abstract fun profitPartnerDao(): ProfitPartnerDao

    companion object {
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Schema is unchanged in v7; this migration preserves existing data.
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bore_pile_finance.db"
                )
                    .addMigrations(MIGRATION_6_7)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
