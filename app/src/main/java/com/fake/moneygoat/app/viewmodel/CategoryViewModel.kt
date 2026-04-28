package com.moneygoat.app.viewmodel
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.moneygoat.app.data.database.AppDatabase
import com.moneygoat.app.data.entity.Category
import kotlinx.coroutines.launch

class CategoryViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).categoryDao()
    fun getCategories(userId: Long): LiveData<List<Category>> = dao.getCategoriesByUser(userId)
    suspend fun getCategoriesList(userId: Long): List<Category> = dao.getCategoriesByUserList(userId)
    fun addCategory(name: String, userId: Long) { viewModelScope.launch { dao.insert(Category(name = name, userId = userId)) } }
    fun deleteCategory(category: Category) { viewModelScope.launch { dao.delete(category) } }
}

