package com.moneygoat.app.viewmodel
import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.moneygoat.app.data.FirebaseManager
import com.moneygoat.app.data.database.AppDatabase
import com.moneygoat.app.data.entity.CategoryTotal
import com.moneygoat.app.data.entity.Expense
import kotlinx.coroutines.launch

/**
 * ViewModel for managing expenses.
 * Handles adding, deleting, and filtering expenses by date range.
 * Now syncs data to Firebase for online persistence using username as key.
 */
class ExpenseViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "MoneyGoat_Expense"
    private val dao = AppDatabase.getDatabase(application).expenseDao()
    private val firebase = FirebaseManager()
    
    private val _dateFilter = MutableLiveData<Triple<Long, String, String>>()

    val filteredExpenses: LiveData<List<Expense>> = _dateFilter.switchMap { (u, s, e) ->
        dao.getExpensesByDateRange(u, s, e)
    }
    
    val categoryTotals: LiveData<List<CategoryTotal>> = _dateFilter.switchMap { (u, s, e) ->
        dao.getCategoryTotals(u, s, e)
    }
    
    val totalSpent: LiveData<Double?> = _dateFilter.switchMap { (u, s, e) ->
        dao.getTotalSpent(u, s, e)
    }

    fun setDateFilter(userId: Long, startDate: String, endDate: String) {
        _dateFilter.value = Triple(userId, startDate, endDate)
    }

    /**
     * Adds a new expense and syncs it to Firebase using the username.
     */
    fun addExpense(expense: Expense, username: String) {
        viewModelScope.launch {
            try {
                val id = dao.insert(expense)
                
                // Sync to online database using username for cross-device consistency
                val syncedExpense = expense.copy(id = id)
                firebase.uploadExpense(username, syncedExpense)
                
                Log.d(TAG, "Expense saved and synced successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving/syncing expense", e)
            }
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            try {
                dao.delete(expense)
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting expense", e)
            }
        }
    }
}
