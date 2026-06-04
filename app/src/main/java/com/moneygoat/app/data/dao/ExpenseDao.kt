package com.moneygoat.app.data.dao
import androidx.lifecycle.LiveData
import androidx.room.*
import com.moneygoat.app.data.entity.CategoryTotal
import com.moneygoat.app.data.entity.Expense

/**
 * ExpenseDao provides the interface for Room to interact with the 'expenses' table.
 * It contains SQL queries for recording transactions and generating analytical reports.
 */
@Dao
interface ExpenseDao {
    /**
     * Persists a new transaction to the database.
     * @return The auto-generated primary key ID of the new record.
     */
    @Insert
    suspend fun insert(expense: Expense): Long

    /**
     * Removes a specific transaction from the database.
     */
    @Delete
    suspend fun delete(expense: Expense)

    /**
     * Retrieves the entire transaction history for a user, sorted chronologically (newest first).
     * Returns a LiveData stream for real-time UI updates.
     */
    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY date DESC, startTime DESC")
    fun getAllByUser(userId: Long): LiveData<List<Expense>>

    /**
     * Filters expenses for a specific audit period. 
     * Used by the Expense List and Analytics screens.
     */
    @Query("SELECT * FROM expenses WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC, startTime DESC")
    fun getExpensesByDateRange(userId: Long, startDate: String, endDate: String): LiveData<List<Expense>>

    /**
     * Performs a relational JOIN between expenses and categories to calculate spending per category.
     * This aggregation is the primary data source for the PieChart visualization.
     * 
     * @return A list of CategoryTotal projections containing category names and their summed amounts.
     */
    @Query("""
        SELECT c.name AS categoryName, SUM(e.amount) AS totalAmount 
        FROM expenses e 
        INNER JOIN categories c ON e.categoryId = c.id 
        WHERE e.userId = :userId AND e.date BETWEEN :startDate AND :endDate 
        GROUP BY e.categoryId 
        ORDER BY totalAmount DESC
    """)
    fun getCategoryTotals(userId: Long, startDate: String, endDate: String): LiveData<List<CategoryTotal>>

    /**
     * Aggregates the total spending for a specific user and period.
     * Used on the Dashboard and Analytics screens to compare against budget goals.
     */
    @Query("SELECT SUM(amount) FROM expenses WHERE userId = :userId AND date BETWEEN :startDate AND :endDate")
    fun getTotalSpent(userId: Long, startDate: String, endDate: String): LiveData<Double?>
}
