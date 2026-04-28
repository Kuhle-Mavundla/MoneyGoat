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
 * Main Activity of the application.
 * Hosts the fragments for Home, Analytics, Add Expense, Categories, and Goals.
 * Manages the Bottom Navigation and the top menu.
 */
class MainActivity : AppCompatActivity() {
    private val TAG = "MoneyGoat_MainUI"
    var userId: Long = -1L
    var username: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "MainActivity created")
        setContentView(R.layout.activity_main)
        
        // Retrieve user information passed from LoginActivity
        userId = intent.getLongExtra("USER_ID", -1L)
        username = intent.getStringExtra("USERNAME") ?: ""
        Log.d(TAG, "Active user: $username (ID: $userId)")
        
        // Setup Action Bar
        supportActionBar?.title = "MoneyGoat"
        supportActionBar?.subtitle = "Welcome, $username"

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        
        // Load initial fragment if this is a fresh start
        if (savedInstanceState == null) {
            Log.d(TAG, "Initial fragment load: HomeFragment")
            loadFragment(HomeFragment())
        }

        // Handle bottom navigation item selection
        bottomNav.setOnItemSelectedListener { item ->
            Log.d(TAG, "Navigation item selected: ${item.title}")
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_analytics -> CategoryTotalsFragment()
                R.id.nav_add -> AddExpenseFragment()
                R.id.nav_categories -> CategoryManagerFragment()
                R.id.nav_goals -> GoalSettingsFragment()
                else -> HomeFragment()
            }
            loadFragment(fragment)
            true
        }
    }

    /**
     * Replaces the current fragment in the container with the provided fragment.
     */
    private fun loadFragment(fragment: Fragment) {
        Log.d(TAG, "Loading fragment: ${fragment.javaClass.simpleName}")
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean { 
        menuInflater.inflate(R.menu.main_menu, menu)
        return true 
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "Options item selected: ${item.title}")
        return when (item.itemId) {
            R.id.action_logout -> { 
                logout()
                true 
            }
            R.id.action_expense_list -> { 
                loadFragment(ExpenseListFragment())
                true 
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Clears the user session and returns to the LoginActivity.
     */
    private fun logout() {
        Log.d(TAG, "User logging out: $username")
        getSharedPreferences("moneygoat_prefs", Context.MODE_PRIVATE).edit().clear().apply()
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}
