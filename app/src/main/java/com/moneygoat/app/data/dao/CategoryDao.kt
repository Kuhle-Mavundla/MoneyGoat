package com.moneygoat.app.data.dao
import androidx.lifecycle.LiveData
import androidx.room.*
import com.moneygoat.app.data.entity.Category

/**
 * CategoryDao handles the database operations for user-defined spending categories.
 * 
 * Each user can create their own unique set of categories to classify their expenses.
 * This DAO provides the necessary methods to create, list, and remove these categories.
 */
@Dao
interface CategoryDao {
    /**
     * Inserts a new category into the 'categories' table.
     * 
     * @param category The category object containing the name and owner's userId.
     * @return The auto-generated row ID of the new category.
     */
    @Insert
    suspend fun insert(category: Category): Long

    /**
     * Retrieves an observable list of all categories created by a specific user.
     * The results are sorted alphabetically by name to ensure a consistent UI layout.
     * 
     * @param userId The ID of the user whose categories are being requested.
     */
    @Query("SELECT * FROM categories WHERE userId = :userId ORDER BY name ASC")
    fun getCategoriesByUser(userId: Long): LiveData<List<Category>>

    /**
     * Performs a one-shot retrieval of categories for a specific user.
     * Unlike the LiveData version, this is a suspending function that returns 
     * a static list immediately.
     */
    @Query("SELECT * FROM categories WHERE userId = :userId ORDER BY name ASC")
    suspend fun getCategoriesByUserList(userId: Long): List<Category>

    /**
     * Removes a category from the database.
     * Note: In a production environment, this might trigger a CASCADE delete or 
     * require checking if expenses are currently linked to this category.
     */
    @Delete
    suspend fun delete(category: Category)
}
