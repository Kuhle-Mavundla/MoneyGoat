package com.moneygoat.app.data

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.moneygoat.app.data.entity.Expense
import com.moneygoat.app.data.entity.BudgetGoal
import com.moneygoat.app.data.entity.Category
import com.moneygoat.app.data.entity.User
import kotlinx.coroutines.tasks.await

/**
 * FirebaseManager is responsible for all interactions with the Firebase Realtime Database.
 * It handles data synchronization by uploading local Room database records to the cloud
 * and fetching user data during login/authentication processes.
 *
 * This class uses Kotlin Coroutines for asynchronous operations, ensuring that network
 * calls do not block the main UI thread.
 */
class FirebaseManager {
    private val TAG = "MoneyGoat_Firebase"
    
    /**
     * Entry point for the Firebase Realtime Database.
     * The database reference points to the root of the JSON tree.
     */
    private val database = FirebaseDatabase.getInstance().reference

    /**
     * Persists a new user profile to the 'users_profiles' node in Firebase.
     *
     * @param user The User entity containing profile details (username, email, etc.)
     * @throws Exception if the network request fails or database rules deny access.
     */
    suspend fun uploadUser(user: User) {
        Log.i(TAG, "Attempting to upload user profile: ${user.username}")
        try {
            // .await() converts the Firebase Task into a suspending function call
            database.child("users_profiles").child(user.username).setValue(user).await()
            Log.d(TAG, "Successfully uploaded user profile for: ${user.username}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload user: ${user.username}. Error: ${e.message}", e)
        }
    }

    /**
     * Retrieves a user's profile information from Firebase using their username as the key.
     *
     * @param username The unique identifier for the user.
     * @return The User object if found, null otherwise or on error.
     */
    suspend fun fetchUser(username: String): User? {
        Log.i(TAG, "Fetching user profile from Firebase: $username")
        return try {
            val snapshot = database.child("users_profiles").child(username).get().await()
            val user = snapshot.getValue(User::class.java)
            if (user != null) {
                Log.d(TAG, "User profile retrieved successfully for: $username")
            } else {
                Log.w(TAG, "No user profile found for: $username")
            }
            user
        } catch (e: Exception) {
            Log.e(TAG, "Error encountered while fetching user: $username", e)
            null
        }
    }

    /**
     * Uploads an expense record to a user-specific 'expenses' sub-node.
     * This ensures data isolation where users only access their own financial records.
     *
     * @param username The owner of the expense.
     * @param expense The expense record to be synchronized.
     */
    suspend fun uploadExpense(username: String, expense: Expense) {
        Log.d(TAG, "Syncing expense to cloud: ID=${expense.id}, Desc=${expense.description}")
        try {
            val key = expense.id.toString()
            database.child("data").child(username)
                .child("expenses").child(key).setValue(expense).await()
            Log.v(TAG, "Cloud sync complete for expense ID: ${expense.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Expense sync failed for ID: ${expense.id}", e)
        }
    }

    /**
     * Uploads or updates a budget goal for a specific month and year.
     * The key is structured as 'YYYY_MM' to facilitate easy lookup and chronological sorting.
     *
     * @param username The owner of the goal.
     * @param goal The BudgetGoal containing the target savings/spending amounts.
     */
    suspend fun uploadGoal(username: String, goal: BudgetGoal) {
        val key = "${goal.year}_${goal.month}"
        Log.i(TAG, "Uploading budget goal for $username - Period: $key")
        try {
            database.child("data").child(username)
                .child("goals").child(key).setValue(goal).await()
            Log.d(TAG, "Goal successfully synced for $key")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync goal for $key", e)
        }
    }

    /**
     * Synchronizes a user-defined category to the cloud.
     *
     * @param username The owner of the category.
     * @param category The Category entity (e.g., 'Groceries', 'Rent').
     */
    suspend fun uploadCategory(username: String, category: Category) {
        Log.d(TAG, "Syncing category: ${category.name} (ID: ${category.id})")
        try {
            val key = category.id.toString()
            database.child("data").child(username)
                .child("categories").child(key).setValue(category).await()
            Log.v(TAG, "Category ${category.name} synced successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing category ${category.name}", e)
        }
    }
}
