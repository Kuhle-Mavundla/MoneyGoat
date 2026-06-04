package com.moneygoat.app.data.dao
import androidx.lifecycle.LiveData
import androidx.room.*
import com.moneygoat.app.data.entity.Category

/**
 * Data Access Object for the Category entity.
 * Provides methods for managing user-defined expense categories.
 */
@Dao
interface CategoryDao {
    /**
     * Inserts a new category into the database.
     */
    @Insert
    suspend fun insert(category: Category): Long

    /**
     * Retrieves all categories for a specific user as LiveData for UI observation.
     */
    @Query("SELECT * FROM categories WHERE userId = :userId ORDER BY name ASC")
    fun getCategoriesByUser(userId: Long): LiveData<List<Category>>

    /**
     * Retrieves all categories for a specific user as a standard List (one-shot).
     */
    @Query("SELECT * FROM categories WHERE userId = :userId ORDER BY name ASC")
    suspend fun getCategoriesByUserList(userId: Long): List<Category>

    /**
     * Deletes a category from the database.
     */
    @Delete
    suspend fun delete(category: Category)
}
