package com.application.svcafe

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        auth = FirebaseAuth.getInstance()

        val fullNameEditText: EditText = findViewById(R.id.editTextFullName)
        val emailEditText: EditText = findViewById(R.id.editTextEmail)
        val passwordEditText: EditText = findViewById(R.id.editTextPassword)
        val confirmPasswordEditText: EditText = findViewById(R.id.editTextConfirmPassword)
        val signupButton: Button = findViewById(R.id.buttonSignup)
        val loginTextView: TextView = findViewById(R.id.login)

        signupButton.setOnClickListener {
            val fullName = fullNameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            if (fullName.isEmpty()) {
                showCustomToast("Please enter your full name")
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                showCustomToast("Please enter your email")
                return@setOnClickListener
            }

            if (!isEmailValid(email)) {
                showCustomToast("Please enter a valid email address")
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                showCustomToast("Please enter your password")
                return@setOnClickListener
            }

            if (!isPasswordValid(password)) {
                showCustomToast("Password must be at least 6 characters, contain at least one uppercase letter, one lowercase letter, one digit, and one special character")
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                showCustomToast("Passwords do not match")
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign up success
                        val user = auth.currentUser
                        val userId = user?.uid

                        if (userId != null) {
                            val database = FirebaseDatabase.getInstance()
                            val usersRef = database.getReference("users")

                            val userDetails = User(fullName, email)

                            usersRef.child(userId).setValue(userDetails)
                                .addOnCompleteListener { dbTask ->
                                    if (dbTask.isSuccessful) {
                                        showCustomToast("Signup successful!")
                                        val intent = Intent(this, MainActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        showCustomToast("Failed to save user details: ${dbTask.exception?.message}")
                                    }
                                }
                        }
                    } else {
                        // If sign up fails, display a message to the user.
                        showCustomToast("Signup failed: ${task.exception?.message}")
                    }
                }
        }

        loginTextView.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isPasswordValid(password: String): Boolean {
        val passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{6,}$"
        return password.matches(passwordPattern.toRegex())
    }

    private fun showCustomToast(message: String) {
        val inflater: LayoutInflater = layoutInflater
        val layout: View = inflater.inflate(R.layout.toast_layout, findViewById(R.id.toastText))

        val text: TextView = layout.findViewById(R.id.toastText)
        text.text = message

        with(Toast(applicationContext)) {
            duration = Toast.LENGTH_LONG
            view = layout
            show()
        }
    }
}
