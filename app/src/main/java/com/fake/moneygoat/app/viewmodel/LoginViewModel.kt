package com.moneygoat.app.viewmodel
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.moneygoat.app.data.database.AppDatabase
import com.moneygoat.app.data.entity.User
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val userDao = AppDatabase.getDatabase(application).userDao()
    val loginResult = MutableLiveData<User?>()
    val registerResult = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()

    fun login(username: String, password: String) {
        viewModelScope.launch {
            val user = userDao.login(username, password)
            if (user != null) loginResult.postValue(user) else errorMessage.postValue("Invalid username or password")
        }
    }
    fun register(username: String, password: String) {
        viewModelScope.launch {
            if (userDao.getByUsername(username) != null) { errorMessage.postValue("Username already exists"); return@launch }
            userDao.insert(User(username = username, password = password))
            registerResult.postValue(true)
        }
    }
}
