package com.moneygoat.app.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.moneygoat.app.R
import com.moneygoat.app.viewmodel.LoginViewModel

/**
 * LoginActivity is the initial entry point for users.
 * It provides the interface for user authentication and handles session persistence.
 *
 * Key Responsibilities:
 * 1. Auto-login check: Verifies if a valid session exists in SharedPreferences.
 * 2. Input Validation: Ensures username and password fields are not empty before submission.
 * 3. Authentication: Delegates the credential verification to LoginViewModel.
 * 4. Session Management: Persists the user ID and username upon successful login.
 */
class LoginActivity : AppCompatActivity() {
    private val TAG = "MoneyGoat_LoginUI"
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "LoginActivity created - Initializing authentication flow")
        
        // --- STEP 1: Session Check ---
        // We use SharedPreferences to remember the user and avoid forcing a login every time.
        val prefs = getSharedPreferences("moneygoat_prefs", Context.MODE_PRIVATE)
        val savedUserId = prefs.getLong("user_id", -1)

        if (savedUserId != -1L) {
            val savedUsername = prefs.getString("username", "") ?: ""
            Log.i(TAG, "Active session detected for: $savedUsername. Proceeding with auto-login.")
            navigateToMain(savedUserId, savedUsername)
            return // Stop further initialization of the login UI
        }
        
        // --- STEP 2: UI Setup ---
        setContentView(R.layout.activity_login)
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)

        // Handle login button click event
        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            Log.d(TAG, "Login button clicked for user: $username")

            if (username.isEmpty() || password.isEmpty()) {
                Log.w(TAG, "Authentication aborted: Empty credentials provided.")
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Request the ViewModel to verify the credentials
            viewModel.login(username, password)
        }
        
        // Navigate to the registration screen if the user doesn't have an account
        tvRegister.setOnClickListener {
            Log.d(TAG, "User requested navigation to RegistrationActivity")
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        
        // --- STEP 3: Observers ---

        // Observe successful login result from the ViewModel
        viewModel.loginResult.observe(this) { user ->
            if (user != null) {
                Log.i(TAG, "Login successful for user: ${user.username}. Saving session.")

                // Persist user details for future auto-login
                prefs.edit()
                    .putLong("user_id", user.id)
                    .putString("username", user.username)
                    .apply()

                Log.v(TAG, "Session persisted to SharedPreferences")
                navigateToMain(user.id, user.username)
            }
        }
        
        // Observe and display error messages (e.g., "User not found" or "Incorrect password")
        viewModel.errorMessage.observe(this) { message ->
            if (message != null) {
                Log.e(TAG, "Authentication failed: $message")
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Handles the transition to the main dashboard.
     * Clears the Activity stack so the user cannot navigate back to the login screen using the back button.
     *
     * @param userId Unique database ID of the authenticated user.
     * @param username The display name of the user.
     */
    private fun navigateToMain(userId: Long, username: String) {
        Log.d(TAG, "Navigating to MainActivity context for: $username")
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("USER_ID", userId)
            putExtra("USERNAME", username)
        }
        startActivity(intent)

        // finish() ensures this activity is removed from the task backstack
        finish()
    }
}
