package com.moneygoat.app.data.dao
import androidx.lifecycle.LiveData
import androidx.room.*
import com.moneygoat.app.data.entity.BudgetGoal

@Dao
interface BudgetGoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(goal: BudgetGoal): Long

    @Query("SELECT * FROM budget_goals WHERE userId = :userId AND month = :month AND year = :year LIMIT 1")
    fun getGoal(userId: Long, month: Int, year: Int): LiveData<BudgetGoal?>

    @Query("SELECT * FROM budget_goals WHERE userId = :userId AND month = :month AND year = :year LIMIT 1")
    suspend fun getGoalDirect(userId: Long, month: Int, year: Int): BudgetGoal?
}
