package com.moneygoat.app.ui

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
 * RegisterActivity facilitates the creation of new user accounts.
 * It performs client-side validation of user input before interacting with the database.
 *
 * Flow:
 * 1. User enters username and password (with confirmation).
 * 2. Activity validates input constraints (non-empty, password match, minimum length).
 * 3. ViewModel attempts to create the user in the Room database and sync with Firebase.
 * 4. On success, the user is redirected back to the login screen.
 */
class RegisterActivity : AppCompatActivity() {
    private val TAG = "MoneyGoat_RegisterUI"
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "RegisterActivity launched")
        setContentView(R.layout.activity_register)

        // Use the same LoginViewModel for registration logic as it handles User entity operations
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        
        // UI Component Initialization
        val etUsername = findViewById<EditText>(R.id.etRegUsername)
        val etPassword = findViewById<EditText>(R.id.etRegPassword)
        val etConfirm = findViewById<EditText>(R.id.etRegConfirmPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvBack = findViewById<TextView>(R.id.tvBackToLogin)

        // Set up the registration submission logic
        btnRegister.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirm = etConfirm.text.toString().trim()
            
            Log.d(TAG, "Registration form submitted for username: $username")

            // --- VALIDATION CHECKS ---

            // 1. Check for blank fields
            if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                Log.w(TAG, "Validation failed: One or more fields are empty")
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // 2. Ensure both password entries match to prevent typos during account creation
            if (password != confirm) {
                Log.w(TAG, "Validation failed: Passwords do not match")
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // 3. Enforce a minimum security standard for password length
            if (password.length < 4) {
                Log.w(TAG, "Validation failed: Password length (${password.length}) is below requirement")
                Toast.makeText(this, "Password must be at least 4 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // If all checks pass, delegate to ViewModel
            Log.i(TAG, "Inputs validated. Calling ViewModel registration for: $username")
            viewModel.register(username, password)
        }
        
        // Simple navigation to return to the previous LoginActivity
        tvBack.setOnClickListener {
            Log.v(TAG, "User opted to cancel registration and return to Login")
            finish()
        }
        
        // --- VIEWMODEL OBSERVATION ---

        // Observe if the registration process was successful
        viewModel.registerResult.observe(this) { success ->
            if (success) {
                Log.i(TAG, "Registration confirmed by data layer. Returning to login screen.")
                Toast.makeText(this, "Registration successful! Please log in.", Toast.LENGTH_SHORT).show()
                finish() // Closes RegisterActivity and returns to LoginActivity
            }
        }
        
        // Observe errors (e.g., SQLite constraint failure if username is already taken)
        viewModel.errorMessage.observe(this) { errorMsg ->
            if (errorMsg != null) {
                Log.e(TAG, "Registration failed with error: $errorMsg")
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
