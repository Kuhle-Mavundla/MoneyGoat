package com.moneygoat.app.data.entity
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "budget_goals",
    foreignKeys = [ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["userId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["userId"])])
data class BudgetGoal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val month: Int,
    val year: Int,
    val minimumGoal: Double,
    val maximumGoal: Double
)
