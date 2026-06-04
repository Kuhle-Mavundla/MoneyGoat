package com.moneygoat.app.data.dao
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.moneygoat.app.data.entity.User

/**
 * UserDao provides the data access methods for user account management.
 * 
 * It handles the core identity functions of the application, including 
 * account creation (registration) and credential verification (login).
 */
@Dao
interface UserDao {
    /**
     * Persists a new user record to the 'users' table.
     * 
     * @param user The User entity containing username and password.
     * @return The auto-generated row ID for the new user.
     */
    @Insert
    suspend fun insert(user: User): Long

    /**
     * Authenticates a user by checking for an exact match of username and password.
     * This is a "one-shot" query used during the login flow.
     * 
     * @param username The username provided in the login form.
     * @param password The password provided in the login form.
     * @return The matching User object if credentials are correct, null otherwise.
     */
    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    suspend fun login(username: String, password: String): User?

    /**
     * Checks if a specific username already exists in the database.
     * Used during registration to enforce the requirement that every user 
     * must have a unique identity.
     * 
     * @param username The username to check.
     * @return The existing User object if found, null if the username is available.
     */
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getByUsername(username: String): User?
}
