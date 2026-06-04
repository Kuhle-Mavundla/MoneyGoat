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
 * ViewModel for managing expense categories.
 * Now includes Firebase sync.
 */
class CategoryViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "MoneyGoat_Category"
    private val dao = AppDatabase.getDatabase(application).categoryDao()
    private val firebase = FirebaseManager()

    fun getCategories(userId: Long): LiveData<List<Category>> {
        return dao.getCategoriesByUser(userId)
    }

    suspend fun getCategoriesList(userId: Long): List<Category> {
        return dao.getCategoriesByUserList(userId)
    }

    /**
     * Adds a new category and syncs it to Firebase.
     */
    fun addCategory(name: String, userId: Long) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Saving category locally: $name")
                val category = Category(name = name, userId = userId)
                val id = dao.insert(category)
                
                // Sync to online database
                val syncedCategory = category.copy(id = id)
                firebase.uploadCategory(userId, syncedCategory)
                
                Log.d(TAG, "Category saved and synced successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving/syncing category", e)
            }
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            try {
                dao.delete(category)
                Log.d(TAG, "Category deleted successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting category", e)
            }
        }
    }
}
