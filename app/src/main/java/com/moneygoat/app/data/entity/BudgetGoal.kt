package com.moneygoat.app.data.entity
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Data class representing a monthly budget goal for a user.
 * Defines the minimum and maximum spending targets for a specific month and year.
 */
@Entity(tableName = "budget_goals",
    foreignKeys = [ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["userId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["userId"])])
data class BudgetGoal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long = 0,
    val month: Int = 0,
    val year: Int = 0,
    val minimumGoal: Double = 0.0,
    val maximumGoal: Double = 0.0
)
