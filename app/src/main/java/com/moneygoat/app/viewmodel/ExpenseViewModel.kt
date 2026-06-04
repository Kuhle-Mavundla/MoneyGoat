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
 * Now syncs data to Firebase for online persistence.
 */
class ExpenseViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "MoneyGoat_Expense"
    private val dao = AppDatabase.getDatabase(application).expenseDao()
    private val firebase = FirebaseManager()
    
    private val _dateFilter = MutableLiveData<Triple<Long, String, String>>()

    val filteredExpenses: LiveData<List<Expense>> = _dateFilter.switchMap { (u, s, e) ->
        Log.d(TAG, "Reading expenses for user $u from $s to $e")
        dao.getExpensesByDateRange(u, s, e)
    }
    
    val categoryTotals: LiveData<List<CategoryTotal>> = _dateFilter.switchMap { (u, s, e) ->
        Log.d(TAG, "Reading category totals for user $u from $s to $e")
        dao.getCategoryTotals(u, s, e)
    }
    
    val totalSpent: LiveData<Double?> = _dateFilter.switchMap { (u, s, e) ->
        Log.d(TAG, "Reading total spent for user $u from $s to $e")
        dao.getTotalSpent(u, s, e)
    }

    fun setDateFilter(userId: Long, startDate: String, endDate: String) {
        _dateFilter.value = Triple(userId, startDate, endDate)
    }

    /**
     * Adds a new expense and syncs it to Firebase.
     */
    fun addExpense(expense: Expense) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Saving expense locally: ${expense.description}")
                val id = dao.insert(expense)
                
                // Sync to online database
                val syncedExpense = expense.copy(id = id)
                firebase.uploadExpense(expense.userId, syncedExpense)
                
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
                Log.d(TAG, "Expense deleted successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting expense", e)
            }
        }
    }
}
