package com.moneygoat.app.data.dao
import androidx.lifecycle.LiveData
import androidx.room.*
import com.moneygoat.app.data.entity.BudgetGoal

/**
 * BudgetGoalDao provides the Room database operations for managing monthly financial targets.
 * 
 * Each user can set exactly one budget goal per month/year combination. 
 * This DAO allows for the creation, update, and retrieval of these targets.
 */
@Dao
interface BudgetGoalDao {
    /**
     * Inserts a new budget goal or replaces an existing one if the primary key conflicts.
     * This 'Upsert' behavior is used when users adjust their goals for a month they've already configured.
     * 
     * @param goal The BudgetGoal entity to be saved.
     * @return The row ID of the inserted/updated record.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(goal: BudgetGoal): Long

    /**
     * Retrieves the budget goal for a specific month and year as an observable LiveData object.
     * This is used by UI components to automatically refresh when goals are changed.
     * 
     * @param userId The ID of the logged-in user.
     * @param month The calendar month (1-12).
     * @param year The calendar year (e.g., 2023).
     */
    @Query("SELECT * FROM budget_goals WHERE userId = :userId AND month = :month AND year = :year LIMIT 1")
    fun getGoal(userId: Long, month: Int, year: Int): LiveData<BudgetGoal?>

    /**
     * Performs a one-shot retrieval of a budget goal without observation.
     * Primarily used within ViewModels to check for existing records before performing updates.
     */
    @Query("SELECT * FROM budget_goals WHERE userId = :userId AND month = :month AND year = :year LIMIT 1")
    suspend fun getGoalDirect(userId: Long, month: Int, year: Int): BudgetGoal?
}
