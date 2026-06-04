package com.moneygoat.app.data

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.moneygoat.app.data.entity.Expense
import com.moneygoat.app.data.entity.BudgetGoal
import com.moneygoat.app.data.entity.Category
import com.moneygoat.app.data.entity.User
import kotlinx.coroutines.tasks.await

/**
 * Manager class for handling Firebase Realtime Database operations.
 * Provides functionality to sync local data to the cloud.
 */
class FirebaseManager {
    private val TAG = "MoneyGoat_Firebase"
    
    // Explicitly using a reference. If your database is in a specific region, 
    // you might need to provide the URL here: FirebaseDatabase.getInstance("URL").reference
    private val database = FirebaseDatabase.getInstance().reference

    /**
     * Uploads a user profile to Firebase. 
     * Uses username as a key for cross-device consistency.
     */
    suspend fun uploadUser(user: User) {
        try {
            database.child("users_profiles").child(user.username).setValue(user).await()
            Log.d(TAG, "User profile uploaded to Firebase: ${user.username}")
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading user to Firebase. Check rules and google-services.json", e)
        }
    }

    /**
     * Fetches a user profile from Firebase by username.
     */
    suspend fun fetchUser(username: String): User? {
        return try {
            val snapshot = database.child("users_profiles").child(username).get().await()
            snapshot.getValue(User::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user from Firebase", e)
            null
        }
    }

    /**
     * Uploads an expense to Firebase under the user's specific node.
     */
    suspend fun uploadExpense(username: String, expense: Expense) {
        try {
            val key = expense.id.toString()
            database.child("data").child(username)
                .child("expenses").child(key).setValue(expense).await()
            Log.d(TAG, "Expense uploaded to Firebase: ${expense.description}")
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading expense to Firebase", e)
        }
    }

    /**
     * Uploads a budget goal to Firebase.
     */
    suspend fun uploadGoal(username: String, goal: BudgetGoal) {
        try {
            val key = "${goal.year}_${goal.month}"
            database.child("data").child(username)
                .child("goals").child(key).setValue(goal).await()
            Log.d(TAG, "Goal uploaded to Firebase for $key")
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading goal to Firebase", e)
        }
    }

    /**
     * Uploads a category to Firebase.
     */
    suspend fun uploadCategory(username: String, category: Category) {
        try {
            val key = category.id.toString()
            database.child("data").child(username)
                .child("categories").child(key).setValue(category).await()
            Log.d(TAG, "Category uploaded to Firebase: ${category.name}")
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading category to Firebase", e)
        }
    }
}
