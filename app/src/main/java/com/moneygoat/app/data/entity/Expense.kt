package com.moneygoat.app.data.entity
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Expense represents a single financial transaction record in the application.
 * 
 * It is a Room Entity that maps to the 'expenses' table.
 * 
 * Relationships:
 * - Many-to-One with User: Each expense belongs to a specific user.
 * - Many-to-One with Category: Each expense is classified under one category.
 * 
 * Constraints:
 * - Foreign Keys ensure referential integrity; if a User or Category is deleted, 
 *   their associated expenses are also removed (CASCADE).
 * - Indices are provided for userId and categoryId to optimize query performance 
 *   when filtering or aggregating data.
 */
@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = User::class, 
            parentColumns = ["id"], 
            childColumns = ["userId"], 
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Category::class, 
            parentColumns = ["id"], 
            childColumns = ["categoryId"], 
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"]), 
        Index(value = ["categoryId"])
    ]
)
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    
    // Date of the transaction in YYYY-MM-DD format for easy sorting and filtering
    val date: String = "",
    
    // Temporal range of the activity associated with the expense
    val startTime: String = "",
    val endTime: String = "",
    
    // User-provided description of the purchase
    val description: String = "",
    
    // The monetary value of the expense
    val amount: Double = 0.0,
    
    // Linking IDs for relational data
    val categoryId: Long = 0,
    val userId: Long = 0,
    
    // Optional path to an image file containing the digital receipt
    val photoPath: String? = null
)
