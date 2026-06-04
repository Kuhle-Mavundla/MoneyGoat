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
 * GoalViewModel manages the user's financial targets (Budget Goals).
 * 
 * Each goal is bound to a specific month and year. This allows users to set 
 * varying financial targets as their life circumstances change (e.g., higher 
 * budget for December holidays vs. January).
 */
class GoalViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "MoneyGoat_GoalVM"
    
    // Database access for local targets
    private val dao = AppDatabase.getDatabase(application).budgetGoalDao()
    
    // Cloud manager for syncing goals across devices
    private val firebase = FirebaseManager()

    /**
     * Retrieves the budget goal for a specific user and month.
     * Returns a LiveData object that the UI (like HomeFragment) can observe 
     * to show real-time progress.
     */
    fun getGoal(userId: Long, month: Int, year: Int): LiveData<BudgetGoal?> {
        Log.v(TAG, "Observing goal for User $userId - Period: $month/$year")
        return dao.getGoal(userId, month, year)
    }

    /**
     * Persists a goal to the local database and synchronizes it with Firebase.
     * 
     * It handles both new goal creation and updating existing goals by:
     * 1. Checking if a goal already exists for the given period.
     * 2. Constructing a BudgetGoal object with the correct primary key (ID).
     * 3. Performing an 'Upsert' operation.
     */
    fun saveGoal(userId: Long, username: String, month: Int, year: Int, minGoal: Double, maxGoal: Double) {
        Log.i(TAG, "Saving goal for $username: Month=$month, Year=$year, Min=$minGoal, Max=$maxGoal")
        
        viewModelScope.launch {
            try {
                // Step 1: Check for existing goal to avoid duplicate entries for the same month
                val existing = dao.getGoalDirect(userId, month, year)
                
                val goal = BudgetGoal(
                    id = existing?.id ?: 0, // Use existing ID if updating, 0 triggers auto-increment if new
                    userId = userId, 
                    month = month, 
                    year = year, 
                    minimumGoal = minGoal, 
                    maximumGoal = maxGoal
                )
                
                // Step 2: Save locally
                val id = dao.insertOrUpdate(goal)
                Log.d(TAG, "Goal successfully saved to Room DB (ID: $id)")
                
                // Step 3: Cloud Sync
                // Construct the object with the finalized ID for Firebase consistency
                val syncedGoal = if (goal.id == 0L) goal.copy(id = id) else goal
                firebase.uploadGoal(username, syncedGoal)
                
                Log.i(TAG, "Goal successfully synchronized with cloud for $username")
            } catch (e: Exception) {
                Log.e(TAG, "Critical failure during goal save/sync operation", e)
            }
        }
    }
}
