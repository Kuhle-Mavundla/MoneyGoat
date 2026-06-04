package com.moneygoat.app.data.entity
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Category represents a user-defined classification for expenses.
 * 
 * Users can create custom categories (e.g., 'Coffee', 'Rent', 'Travel') to better
 * understand their spending habits. Each category is unique to a specific user.
 */
@Entity(
    tableName = "categories",
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
data class Category(
    // Unique identifier for the category record
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    
    // The display name of the category
    val name: String = "",
    
    // The ID of the user who owns this category
    val userId: Long = 0
)
