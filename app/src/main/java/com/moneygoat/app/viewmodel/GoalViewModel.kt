package com.moneygoat.app.viewmodel
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.moneygoat.app.data.FirebaseManager
import com.moneygoat.app.data.database.AppDatabase
import com.moneygoat.app.data.entity.BudgetGoal
import kotlinx.coroutines.launch

/**
 * ViewModel for managing budget goals.
 * Now includes Firebase sync for online data storage.
 */
class GoalViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "MoneyGoat_Goal"
    private val dao = AppDatabase.getDatabase(application).budgetGoalDao()
    private val firebase = FirebaseManager()

    fun getGoal(userId: Long, month: Int, year: Int): LiveData<BudgetGoal?> {
        return dao.getGoal(userId, month, year)
    }

    /**
     * Saves or updates a budget goal and syncs it to Firebase.
     */
    fun saveGoal(userId: Long, month: Int, year: Int, minGoal: Double, maxGoal: Double) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Saving goal locally for $month/$year")
                val existing = dao.getGoalDirect(userId, month, year)
                val goal = BudgetGoal(
                    id = existing?.id ?: 0, 
                    userId = userId, 
                    month = month, 
                    year = year, 
                    minimumGoal = minGoal, 
                    maximumGoal = maxGoal
                )
                val id = dao.insertOrUpdate(goal)
                
                // Sync to online database
                val syncedGoal = if (goal.id == 0L) goal.copy(id = id) else goal
                firebase.uploadGoal(userId, syncedGoal)
                
                Log.d(TAG, "Goal saved and synced successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving/syncing goal", e)
            }
        }
    }
}
