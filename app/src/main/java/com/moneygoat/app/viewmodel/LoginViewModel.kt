package com.moneygoat.app.viewmodel
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.moneygoat.app.data.FirebaseManager
import com.moneygoat.app.data.database.AppDatabase
import com.moneygoat.app.data.entity.User
import kotlinx.coroutines.launch

/**
 * LoginViewModel manages the authentication state and business logic for 
 * user registration and login.
 * 
 * It interacts with the UserDao for local validation and persistence,
 * and uses FirebaseManager to ensure user profiles are backed up to the cloud.
 */
class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "MoneyGoat_LoginVM"
    
    // Access to local SQLite user table
    private val userDao = AppDatabase.getDatabase(application).userDao()
    
    // LiveData to notify the UI when a user has successfully logged in
    val loginResult = MutableLiveData<User?>()
    
    // LiveData to notify the UI when a new account has been created
    val registerResult = MutableLiveData<Boolean>()
    
    // LiveData to pass user-friendly error messages back to the View
    val errorMessage = MutableLiveData<String>()

    /**
     * Verifies user credentials against the local database.
     * 
     * @param username The unique identifier provided by the user.
     * @param password The secret key associated with the account.
     */
    fun login(username: String, password: String) {
        Log.i(TAG, "Initiating login sequence for user: $username")
        
        viewModelScope.launch {
            try {
                // Perform a database lookup for a matching username/password pair
                val user = userDao.login(username, password)
                
                if (user != null) {
                    Log.d(TAG, "Authentication successful for user: $username (ID: ${user.id})")
                    loginResult.postValue(user)
                } else {
                    Log.w(TAG, "Authentication failed: No matching credentials for '$username'")
                    errorMessage.postValue("Invalid username or password")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Critical error during login process for user: $username", e)
                errorMessage.postValue("Database error: Could not complete login")
            }
        }
    }

    /**
     * Creates a new user account in both the local and cloud databases.
     * 
     * Process:
     * 1. Check if the username is already taken.
     * 2. Insert the new user into the local Room database.
     * 3. Sync the new profile to Firebase for cross-device consistency.
     */
    fun register(username: String, password: String) {
        Log.i(TAG, "Initiating registration sequence for new user: $username")
        
        viewModelScope.launch {
            try {
                // Step 1: Uniqueness check
                val existingUser = userDao.getByUsername(username)
                if (existingUser != null) {
                    Log.w(TAG, "Registration aborted: Username '$username' is already in use")
                    errorMessage.postValue("Username already exists. Please choose another.")
                    return@launch
                }
                
                // Step 2: Local persistence
                val newUser = User(username = username, password = password)
                userDao.insert(newUser)
                Log.d(TAG, "New user account created locally for: $username")
                
                // Step 3: Cloud synchronization
                // We instantiate FirebaseManager to upload the profile to the realtime database
                FirebaseManager().uploadUser(newUser)
                
                Log.i(TAG, "Registration and cloud sync successful for user: $username")
                registerResult.postValue(true)
            } catch (e: Exception) {
                Log.e(TAG, "Critical error during registration for user: $username", e)
                errorMessage.postValue("An error occurred while creating your account")
            }
        }
    }
}
