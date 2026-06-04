package com.moneygoat.app.viewmodel
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.moneygoat.app.data.database.AppDatabase
import com.moneygoat.app.data.entity.User
import kotlinx.coroutines.launch

/**
 * ViewModel for handling user authentication (login and registration).
 * Uses Coroutines for asynchronous database operations.
 */
class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "MoneyGoat_Login"
    private val userDao = AppDatabase.getDatabase(application).userDao()
    
    // LiveData to observe login status
    val loginResult = MutableLiveData<User?>()
    
    // LiveData to observe registration status
    val registerResult = MutableLiveData<Boolean>()
    
    // LiveData to observe error messages
    val errorMessage = MutableLiveData<String>()

    /**
     * Attempts to log in a user with the provided credentials.
     * @param username The username input.
     * @param password The password input.
     */
    fun login(username: String, password: String) {
        Log.d(TAG, "Login attempt for user: $username")
        viewModelScope.launch {
            try {
                val user = userDao.login(username, password)
                if (user != null) {
                    Log.d(TAG, "Login successful for user: $username")
                    loginResult.postValue(user)
                } else {
                    Log.w(TAG, "Login failed for user: $username - Invalid credentials")
                    errorMessage.postValue("Invalid username or password")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during login for user: $username", e)
                errorMessage.postValue("An error occurred during login")
            }
        }
    }

    /**
     * Attempts to register a new user.
     * Checks if the username already exists before inserting.
     * Also syncs the user profile to Firebase.
     */
    fun register(username: String, password: String) {
        Log.d(TAG, "Registration attempt for user: $username")
        viewModelScope.launch {
            try {
                if (userDao.getByUsername(username) != null) {
                    Log.w(TAG, "Registration failed: Username $username already exists")
                    errorMessage.postValue("Username already exists")
                    return@launch
                }
                val user = User(username = username, password = password)
                userDao.insert(user)
                
                // Sync to online database
                com.moneygoat.app.data.FirebaseManager().uploadUser(user)
                
                Log.d(TAG, "Registration successful and synced for: $username")
                registerResult.postValue(true)
            } catch (e: Exception) {
                Log.e(TAG, "Error during registration for user: $username", e)
                errorMessage.postValue("An error occurred during registration")
            }
        }
    }
}
