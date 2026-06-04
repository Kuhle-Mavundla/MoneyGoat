package com.moneygoat.app.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.moneygoat.app.R

/**
 * MainActivity serves as the primary navigation hub for the MoneyGoat application.
 * It utilizes a Single Activity Architecture pattern where different features are 
 * hosted as Fragments within the `fragmentContainer`.
 *
 * This activity handles:
 * 1. User session context (userId and username).
 * 2. Bottom Navigation for core features (Home, Analytics, Add, Categories, Goals).
 * 3. Options Menu for global actions like Logout and viewing the full Expense List.
 */
class MainActivity : AppCompatActivity() {
    private val TAG = "MoneyGoat_MainUI"
    
    // Global user identifiers used across child fragments
    var userId: Long = -1L
    var username: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "Initializing MainActivity UI components")
        setContentView(R.layout.activity_main)
        
        // Retrieve user information passed from LoginActivity via Intent extras
        userId = intent.getLongExtra("USER_ID", -1L)
        username = intent.getStringExtra("USERNAME") ?: "Guest"
        
        if (userId == -1L) {
            Log.w(TAG, "MainActivity started with invalid User ID. Functionality may be limited.")
        } else {
            Log.d(TAG, "Active session verified for user: $username (ID: $userId)")
        }
        
        // Setup Action Bar appearance
        supportActionBar?.apply {
            title = "MoneyGoat"
            subtitle = "Logged in as $username"
            Log.v(TAG, "Action bar configured with user subtitle")
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        
        // Load initial fragment (Home) if this is a fresh start (not a configuration change)
        if (savedInstanceState == null) {
            Log.i(TAG, "Fresh start detected. Loading default HomeFragment.")
            loadFragment(HomeFragment())
        } else {
            Log.v(TAG, "Restoring UI from savedInstanceState")
        }

        // Handle bottom navigation item selection logic
        bottomNav.setOnItemSelectedListener { item ->
            Log.d(TAG, "User tapped navigation item: ${item.title} (ID: ${item.itemId})")
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_analytics -> CategoryTotalsFragment()
                R.id.nav_add -> AddExpenseFragment()
                R.id.nav_categories -> CategoryManagerFragment()
                R.id.nav_goals -> GoalSettingsFragment()
                else -> {
                    Log.w(TAG, "Unknown navigation ID: ${item.itemId}, defaulting to Home")
                    HomeFragment()
                }
            }
            loadFragment(fragment)
            true
        }
    }

    /**
     * Replaces the current fragment in the container.
     * Uses supportFragmentManager to handle transaction lifecycle safely.
     *
     * @param fragment The fragment instance to be displayed.
     */
    private fun loadFragment(fragment: Fragment) {
        val fragmentName = fragment.javaClass.simpleName
        Log.d(TAG, "Performing fragment transaction to: $fragmentName")
        try {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .setReorderingAllowed(true) // Optimization for fragment state
                .commit()
            Log.v(TAG, "Successfully committed transaction for $fragmentName")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load fragment $fragmentName: ${e.message}", e)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean { 
        Log.v(TAG, "Inflating options menu")
        menuInflater.inflate(R.menu.main_menu, menu)
        return true 
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "Action menu item clicked: ${item.title}")
        return when (item.itemId) {
            R.id.action_logout -> { 
                Log.i(TAG, "Logout requested by user")
                logout()
                true 
            }
            R.id.action_expense_list -> { 
                Log.i(TAG, "Navigating to full Expense List history")
                loadFragment(ExpenseListFragment())
                true 
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Terminates the current user session.
     * 1. Clears SharedPreferences to prevent auto-login on next start.
     * 2. Redirects to LoginActivity while clearing the activity backstack.
     */
    private fun logout() {
        Log.i(TAG, "Executing logout procedure for $username")
        
        // Clear session data from persistent storage
        val prefs = getSharedPreferences("moneygoat_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        Log.v(TAG, "Shared preferences cleared")

        // Redirect to Login and prevent going back to Main
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        Log.d(TAG, "Login activity started, finishing MainActivity")
        finish()
    }
}
