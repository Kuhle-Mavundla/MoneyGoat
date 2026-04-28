package com.moneygoat.app.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.moneygoat.app.R
import com.moneygoat.app.viewmodel.LoginViewModel

class LoginActivity : AppCompatActivity() {
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("moneygoat_prefs", Context.MODE_PRIVATE)
        val savedUserId = prefs.getLong("user_id", -1)
        if (savedUserId != -1L) {
            navigateToMain(savedUserId, prefs.getString("username", "") ?: "")
            return
        }
        setContentView(R.layout.activity_login)
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.login(username, password)
        }
        tvRegister.setOnClickListener { startActivity(Intent(this, RegisterActivity::class.java)) }
        viewModel.loginResult.observe(this) { user ->
            if (user != null) {
                prefs.edit().putLong("user_id", user.id).putString("username", user.username).apply()
                navigateToMain(user.id, user.username)
            }
        }
        viewModel.errorMessage.observe(this) { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
    }

    private fun navigateToMain(userId: Long, username: String) {
        startActivity(Intent(this, MainActivity::class.java).apply {
            putExtra("USER_ID", userId); putExtra("USERNAME", username)
        })
        finish()
    }
}
