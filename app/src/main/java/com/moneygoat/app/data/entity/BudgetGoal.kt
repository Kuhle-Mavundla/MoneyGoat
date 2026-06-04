package com.moneygoat.app.data.entity
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * BudgetGoal represents a user's financial targets for a specific calendar month.
 * 
 * Each user can define a 'range' of spending:
 * - minimumGoal: A target floor, often representing a savings goal or minimum necessary spending.
 * - maximumGoal: A hard ceiling or budget limit that the user aims not to exceed.
 * 
 * The combination of (userId, month, year) is conceptually unique, though handled 
 * via logic in the DAO and ViewModel (using REPLACE on conflict).
 */
@Entity(
    tableName = "budget_goals",
    foreignKeys = [
        ForeignKey(
            entity = User::class, 
            parentColumns = ["id"], 
            childColumns = ["userId"], 
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class BudgetGoal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    
    // The owner of this budget goal
    val userId: Long = 0,
    
    // Temporal context: 1-12 for month, and YYYY for year
    val month: Int = 0,
    val year: Int = 0,
    
    // Numeric targets
    val minimumGoal: Double = 0.0,
    val maximumGoal: Double = 0.0
)
