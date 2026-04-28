package com.moneygoat.app.data.dao
import androidx.lifecycle.LiveData
import androidx.room.*
import com.moneygoat.app.data.entity.CategoryTotal
import com.moneygoat.app.data.entity.Expense

/**
 * Data Access Object for the Expense entity.
 * Handles all database operations related to tracking expenses.
 */
@Dao
interface ExpenseDao {
    /**
     * Inserts a new expense into the database.
     */
    @Insert
    suspend fun insert(expense: Expense): Long

    /**
     * Deletes an existing expense from the database.
     */
    @Delete
    suspend fun delete(expense: Expense)

    /**
     * Retrieves all expenses for a specific user, ordered by date and time.
     */
    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY date DESC, startTime DESC")
    fun getAllByUser(userId: Long): LiveData<List<Expense>>

    /**
     * Retrieves expenses for a specific user within a given date range.
     */
    @Query("SELECT * FROM expenses WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC, startTime DESC")
    fun getExpensesByDateRange(userId: Long, startDate: String, endDate: String): LiveData<List<Expense>>

    /**
     * Calculates the total amount spent per category for a user within a date range.
     * Returns a list of CategoryTotal objects.
     */
    @Query("SELECT c.name AS categoryName, SUM(e.amount) AS totalAmount FROM expenses e INNER JOIN categories c ON e.categoryId = c.id WHERE e.userId = :userId AND e.date BETWEEN :startDate AND :endDate GROUP BY e.categoryId ORDER BY totalAmount DESC")
    fun getCategoryTotals(userId: Long, startDate: String, endDate: String): LiveData<List<CategoryTotal>>

    /**
     * Calculates the grand total of all expenses for a user within a date range.
     */
    @Query("SELECT SUM(amount) FROM expenses WHERE userId = :userId AND date BETWEEN :startDate AND :endDate")
    fun getTotalSpent(userId: Long, startDate: String, endDate: String): LiveData<Double?>
}
