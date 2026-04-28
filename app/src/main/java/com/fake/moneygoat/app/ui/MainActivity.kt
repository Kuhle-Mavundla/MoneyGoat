package com.moneygoat.app.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.moneygoat.app.R

class MainActivity : AppCompatActivity() {
    var userId: Long = -1L
    var username: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        userId = intent.getLongExtra("USER_ID", -1L)
        username = intent.getStringExtra("USERNAME") ?: ""
        supportActionBar?.title = "MoneyGoat"
        supportActionBar?.subtitle = "Welcome, $username"

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        if (savedInstanceState == null) loadFragment(HomeFragment())

        bottomNav.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_analytics -> CategoryTotalsFragment()
                R.id.nav_add -> AddExpenseFragment()
                R.id.nav_categories -> CategoryManagerFragment()
                R.id.nav_goals -> GoalSettingsFragment()
                else -> HomeFragment()
            }
            loadFragment(fragment); true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, fragment).commit()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean { menuInflater.inflate(R.menu.main_menu, menu); return true }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> { logout(); true }
            R.id.action_expense_list -> { loadFragment(ExpenseListFragment()); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logout() {
        getSharedPreferences("moneygoat_prefs", Context.MODE_PRIVATE).edit().clear().apply()
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}
