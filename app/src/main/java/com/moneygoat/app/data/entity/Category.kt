package com.moneygoat.app.data.entity
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Data class representing an expense category.
 * Each category is associated with a specific user.
 */
@Entity(tableName = "categories",
    foreignKeys = [ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["userId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["userId"])])
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = "",
    val userId: Long = 0
)
