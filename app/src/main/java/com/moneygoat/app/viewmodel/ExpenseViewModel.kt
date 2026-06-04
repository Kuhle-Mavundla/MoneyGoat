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
 * ExpenseViewModel serves as the communication center between the UI (Fragments)
 * and the Data Layer (Room DAO & Firebase).
 *
 * It maintains the state of expense lists, filtered totals, and handles the
 * business logic for persisting data both locally and in the cloud.
 */
class ExpenseViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "MoneyGoat_ExpenseVM"

    // Database access objects and managers
    private val dao = AppDatabase.getDatabase(application).expenseDao()
    private val firebase = FirebaseManager()
    
    /**
     * Holds the current filtering parameters: (User ID, Start Date, End Date).
     * Using a Triple allows us to trigger updates whenever any of these 3 change.
     */
    private val _dateFilter = MutableLiveData<Triple<Long, String, String>>()

    /**
     * Transformation: filteredExpenses.
     * Automatically executes a new Room query whenever _dateFilter is updated.
     */
    val filteredExpenses: LiveData<List<Expense>> = _dateFilter.switchMap { (u, s, e) ->
        Log.v(TAG, "Fetching filtered expenses for User $u between $s and $e")
        dao.getExpensesByDateRange(u, s, e)
    }
    
    /**
     * Transformation: categoryTotals.
     * Aggregates spending by category for the selected period.
     */
    val categoryTotals: LiveData<List<CategoryTotal>> = _dateFilter.switchMap { (u, s, e) ->
        Log.v(TAG, "Recalculating category totals for period: $s to $e")
        dao.getCategoryTotals(u, s, e)
    }
    
    /**
     * Transformation: totalSpent.
     * Provides the sum of all expenses in the current filter.
     */
    val totalSpent: LiveData<Double?> = _dateFilter.switchMap { (u, s, e) ->
        dao.getTotalSpent(u, s, e)
    }

    /**
     * Updates the active filter. This is called from the UI when a user
     * selects a different date range.
     */
    fun setDateFilter(userId: Long, startDate: String, endDate: String) {
        Log.d(TAG, "Setting date filter: UserID=$userId, Range=[$startDate, $endDate]")
        _dateFilter.value = Triple(userId, startDate, endDate)
    }

    /**
     * Adds a new expense record.
     * This follows a 'Local First' strategy:
     * 1. Save to Room database (returns the generated ID).
     * 2. Copy the expense with the new ID.
     * 3. Sync the updated object to Firebase Realtime Database.
     *
     * @param expense The expense entity to persist.
     * @param username Used as the key in Firebase for data isolation.
     */
    fun addExpense(expense: Expense, username: String) {
        Log.i(TAG, "Adding new expense: ${expense.description} (R${expense.amount})")
        viewModelScope.launch {
            try {
                // Step 1: Local Persistence
                val id = dao.insert(expense)
                Log.d(TAG, "Expense saved to local Room DB with ID: $id")

                // Step 2: Cloud Synchronization
                val syncedExpense = expense.copy(id = id)
                firebase.uploadExpense(username, syncedExpense)
                
                Log.i(TAG, "Expense successfully synced to Firebase for user: $username")
            } catch (e: Exception) {
                Log.e(TAG, "Critical failure during expense save/sync operation", e)
            }
        }
    }

    /**
     * Removes an expense from the local database.
     * Note: In a production app, we would also remove the record from Firebase here.
     */
    fun deleteExpense(expense: Expense) {
        Log.i(TAG, "Requesting deletion of expense ID: ${expense.id}")
        viewModelScope.launch {
            try {
                dao.delete(expense)
                Log.d(TAG, "Expense ID ${expense.id} removed from local database")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete expense ID: ${expense.id}", e)
            }
        }
    }
}
