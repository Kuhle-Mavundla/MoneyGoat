package com.moneygoat.app.data.dao
import androidx.lifecycle.LiveData
import androidx.room.*
import com.moneygoat.app.data.entity.CategoryTotal
import com.moneygoat.app.data.entity.Expense

@Dao
interface ExpenseDao {
    @Insert
    suspend fun insert(expense: Expense): Long

    @Delete
    suspend fun delete(expense: Expense)

    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY date DESC, startTime DESC")
    fun getAllByUser(userId: Long): LiveData<List<Expense>>

    @Query("SELECT * FROM expenses WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC, startTime DESC")
    fun getExpensesByDateRange(userId: Long, startDate: String, endDate: String): LiveData<List<Expense>>

    @Query("SELECT c.name AS categoryName, SUM(e.amount) AS totalAmount FROM expenses e INNER JOIN categories c ON e.categoryId = c.id WHERE e.userId = :userId AND e.date BETWEEN :startDate AND :endDate GROUP BY e.categoryId ORDER BY totalAmount DESC")
    fun getCategoryTotals(userId: Long, startDate: String, endDate: String): LiveData<List<CategoryTotal>>

    @Query("SELECT SUM(amount) FROM expenses WHERE userId = :userId AND date BETWEEN :startDate AND :endDate")
    fun getTotalSpent(userId: Long, startDate: String, endDate: String): LiveData<Double?>
}
