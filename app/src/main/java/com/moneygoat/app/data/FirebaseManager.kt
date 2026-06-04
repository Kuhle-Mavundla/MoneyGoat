package com.moneygoat.app.data

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.moneygoat.app.data.entity.Expense
import com.moneygoat.app.data.entity.BudgetGoal
import com.moneygoat.app.data.entity.Category
import kotlinx.coroutines.tasks.await

/**
 * Manager class for handling Firebase Realtime Database operations.
 * Provides functionality to sync local data to the cloud.
 */
class FirebaseManager {
    private val TAG = "MoneyGoat_Firebase"
    private val database = FirebaseDatabase.getInstance().reference

    /**
     * Uploads an expense to Firebase under the user's specific node.
     */
    suspend fun uploadExpense(userId: Long, expense: Expense) {
        try {
            val key = expense.id.toString()
            database.child("users").child(userId.toString())
                .child("expenses").child(key).setValue(expense).await()
            Log.d(TAG, "Expense uploaded to Firebase: ${expense.description}")
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading expense to Firebase", e)
        }
    }

    /**
     * Uploads a budget goal to Firebase.
     */
    suspend fun uploadGoal(userId: Long, goal: BudgetGoal) {
        try {
            val key = "${goal.year}_${goal.month}"
            database.child("users").child(userId.toString())
                .child("goals").child(key).setValue(goal).await()
            Log.d(TAG, "Goal uploaded to Firebase for $key")
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading goal to Firebase", e)
        }
    }

    /**
     * Uploads a category to Firebase.
     */
    suspend fun uploadCategory(userId: Long, category: Category) {
        try {
            val key = category.id.toString()
            database.child("users").child(userId.toString())
                .child("categories").child(key).setValue(category).await()
            Log.d(TAG, "Category uploaded to Firebase: ${category.name}")
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading category to Firebase", e)
        }
    }

    /**
     * Fetches all expenses for a user from Firebase.
     * Note: In a real app, this would be used for restoration.
     */
    suspend fun fetchExpenses(userId: Long): List<Expense> {
        return try {
            val snapshot = database.child("users").child(userId.toString())
                .child("expenses").get().await()
            snapshot.children.mapNotNull { it.getValue(Expense::class.java) }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching from Firebase", e)
            emptyList()
        }
    }
}
