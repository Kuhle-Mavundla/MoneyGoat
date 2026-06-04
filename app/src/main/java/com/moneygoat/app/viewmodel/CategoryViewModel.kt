package com.moneygoat.app.viewmodel
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.moneygoat.app.data.FirebaseManager
import com.moneygoat.app.data.database.AppDatabase
import com.moneygoat.app.data.entity.Category
import kotlinx.coroutines.launch

/**
 * CategoryViewModel manages the lifecycle of user-defined spending categories.
 *
 * It provides methods to retrieve categories specifically for the logged-in user,
 * as well as logic to add new categories and remove existing ones.
 * Like the ExpenseViewModel, it implements a dual-persistence strategy (Room + Firebase).
 */
class CategoryViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "MoneyGoat_CategoryVM"

    // Data Access Object for local SQLite operations
    private val dao = AppDatabase.getDatabase(application).categoryDao()

    // Cloud manager for Firebase synchronization
    private val firebase = FirebaseManager()

    /**
     * Retrieves an observable list of categories for a specific user.
     * The LiveData will emit updates whenever the underlying database table changes.
     */
    fun getCategories(userId: Long): LiveData<List<Category>> {
        Log.v(TAG, "Requesting LiveData for categories belonging to user $userId")
        return dao.getCategoriesByUser(userId)
    }

    /**
     * Synchronous (blocking/suspend) fetch of categories.
     * Useful for one-time operations that don't require continuous observation.
     */
    suspend fun getCategoriesList(userId: Long): List<Category> {
        Log.v(TAG, "Fetching static category list for user $userId")
        return dao.getCategoriesByUserList(userId)
    }

    /**
     * Creates a new category and initiates synchronization.
     *
     * @param name The display name for the category (e.g., 'Groceries').
     * @param userId Owner ID for local database constraints.
     * @param username Used as the unique identifier in Firebase.
     */
    fun addCategory(name: String, userId: Long, username: String) {
        Log.i(TAG, "Adding new category: '$name' for user $username")
        viewModelScope.launch {
            try {
                // Step 1: Insert into local Room database
                val category = Category(name = name, userId = userId)
                val id = dao.insert(category)
                Log.d(TAG, "Category '$name' assigned local ID: $id")

                // Step 2: Push to Firebase for cross-device availability
                val syncedCategory = category.copy(id = id)
                firebase.uploadCategory(username, syncedCategory)
                
                Log.i(TAG, "Category sync completed successfully for '$name'")
            } catch (e: Exception) {
                Log.e(TAG, "Critical failure while adding/syncing category '$name'", e)
            }
        }
    }

    /**
     * Deletes a category from the local database.
     */
    fun deleteCategory(category: Category) {
        Log.i(TAG, "Requesting deletion of category: ${category.name} (ID: ${category.id})")
        viewModelScope.launch {
            try {
                dao.delete(category)
                Log.d(TAG, "Category '${category.name}' removed from local storage")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete category: ${category.name}", e)
            }
        }
    }
}
