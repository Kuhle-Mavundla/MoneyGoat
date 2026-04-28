package com.moneygoat.app.data.database
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.moneygoat.app.data.dao.*
import com.moneygoat.app.data.entity.*

@Database(entities = [User::class, Category::class, Expense::class, BudgetGoal::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetGoalDao(): BudgetGoalDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "moneygoat_database")
                    .fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
        }
    }
}
