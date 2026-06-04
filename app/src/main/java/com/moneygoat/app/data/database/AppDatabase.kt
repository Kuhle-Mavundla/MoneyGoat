package com.moneygoat.app.data.database
import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.moneygoat.app.data.dao.*
import com.moneygoat.app.data.entity.*

/**
 * AppDatabase is the main entry point for the local Room persistence layer.
 * 
 * It defines the schema version, the set of data entities (tables), 
 * and provides access to the Data Access Objects (DAOs) which contain the SQL queries.
 */
@Database(
    entities = [User::class, Category::class, Expense::class, BudgetGoal::class], 
    version = 1, 
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    // Abstract methods to expose the DAOs to the rest of the application
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetGoalDao(): BudgetGoalDao

    companion object {
        private const val TAG = "MoneyGoat_Database"
        
        /**
         * Singleton pattern ensures only one instance of the database exists.
         * Creating multiple instances is expensive and can lead to data synchronization issues.
         */
        @Volatile 
        private var INSTANCE: AppDatabase? = null

        /**
         * Returns the database singleton.
         * Uses synchronized double-check locking for thread safety during initialization.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Log.d(TAG, "Initializing Room database instance...")
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "moneygoat_database"
                )
                /**
                 * fallbackToDestructiveMigration:
                 * During development, if we change the schema (e.g., add a column), 
                 * Room will wipe the existing database and rebuild it from scratch 
                 * instead of requiring complex migration scripts.
                 */
                .fallbackToDestructiveMigration() 
                .build()
                
                INSTANCE = instance
                Log.i(TAG, "Database instance created successfully.")
                instance
            }
        }
    }
}
