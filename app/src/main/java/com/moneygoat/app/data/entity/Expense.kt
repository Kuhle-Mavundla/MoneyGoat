package com.moneygoat.app.data.entity
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Data class representing an expense entry.
 * It has foreign key relationships with User and Category entities.
 */
@Entity(tableName = "expenses",
    foreignKeys = [
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["userId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Category::class, parentColumns = ["id"], childColumns = ["categoryId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["userId"]), Index(value = ["categoryId"])])
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val description: String = "",
    val amount: Double = 0.0,
    val categoryId: Long = 0,
    val userId: Long = 0,
    val photoPath: String? = null
)
