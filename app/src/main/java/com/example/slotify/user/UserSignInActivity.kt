package com.example.slotify.user

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import EmailSender
import com.example.slotify.R
import com.example.slotify.admin.AdminHomePage
import com.example.slotify.admin.AdminSignUpActivity
import com.example.slotify.admin.AdminVerificationPage2
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class UserSignInActivity : AppCompatActivity() {
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText

    private lateinit var btnSignIn: Button
    private lateinit var tvSignUp: TextView
    private lateinit var tvForget: TextView
    private lateinit var checkRemember: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_sign_in)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)

        // Initialize views
        tilEmail = findViewById(R.id.til_email)
        tilPassword = findViewById(R.id.til_pass)
        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_pass)
        btnSignIn = findViewById(R.id.btn_login)
        tvSignUp = findViewById(R.id.tv_sign_up)
        checkRemember = findViewById(R.id.check_remember)
        tvForget = findViewById(R.id.tv_forgt_pass)

        // Check if "Remember Me" was enabled and auto-fill the email
        val savedEmail = sharedPreferences.getString("saved_email", null)
        if (sharedPreferences.getBoolean("is_remembered", false) && savedEmail != null) {
            etEmail.setText(savedEmail)
        }

        // Handle Sign In button click
        btnSignIn.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty()) {
                etEmail.error = "Email is required"
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                etPassword.error = "Password is required"
                return@setOnClickListener
            }

            // Save email if "Remember Me" is checked
            if (checkRemember.isChecked) {
                saveRememberMePreference(email)
            } else {
                clearRememberMePreference()
            }

            // Authenticate admin credentials
            authenticateUser(email, password)
        }

        // Handle Sign Up text click
        tvSignUp.setOnClickListener {
            val intent = Intent(this, UserSignUpActivity::class.java)
            startActivity(intent)
        }

        // Handle "Forgot password" link click
        tvForget.setOnClickListener { showEmailInputDialog() }
    }

    private fun authenticateUser(email: String, password: String) {
        firestore.collection("Users")           //
            .whereEqualTo("email", email)
            .whereEqualTo("password", password)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    // Pass only the email to HomePage
                    val intent = Intent(this, UserHomePage::class.java)
                    intent.putExtra("email", email)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveRememberMePreference(email: String) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("is_remembered", true)
        editor.putString("saved_email", email)
        editor.apply()
    }

    private fun clearRememberMePreference() {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }

    private fun showEmailInputDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_email_input, null)
        var til_dgEmail: TextInputLayout = dialogView.findViewById(R.id.til_frgt_email)
        var et_dgEmail: TextInputEditText = dialogView.findViewById(R.id.et_frgt_email)
        val btnContinue: Button = dialogView.findViewById(R.id.btn_continue)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        btnContinue.setOnClickListener {
            val dg_Email = et_dgEmail.text.toString().trim()

            if (dg_Email.isEmpty()) {
                et_dgEmail.error = "Email is required*"
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(dg_Email).matches()) {
                et_dgEmail.error = "Invalid email format"
                return@setOnClickListener
            }

            firestore.collection("Admins")
                .whereEqualTo("email", dg_Email)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        sendVerificationCode2(dg_Email)
                    } else {
                        Toast.makeText(this, "Invalid email, try again!", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { exception ->
                    Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun sendVerificationCode2(dgEmail: String) {
        val verificationCode = Random.nextInt(100000, 999999).toString()
        firestore.collection("VerificationCodes").document(dgEmail)
            .set(mapOf("code" to verificationCode))
            .addOnSuccessListener {
                // Send email in a background thread
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        EmailSender.sendEmail(
                            recipientEmail = dgEmail,
                            subject = "Your Verification Code",
                            messageBody = "Reset Password\nYour verification code is $verificationCode.\n-Slotify Team"
                        )
                        // Update UI on the main thread after success
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@UserSignInActivity,
                                "Verification code sent",
                                Toast.LENGTH_SHORT
                            ).show()
                            // Navigate to VerificationPage
                            val intent = Intent(this@UserSignInActivity, UserVerificationPage2::class.java)
                            intent.putExtra("email", dgEmail)
                            intent.putExtra("code", verificationCode)
                            startActivity(intent)
                            finish()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@UserSignInActivity,
                                "Failed to send email: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error saving verification code", Toast.LENGTH_SHORT).show()
            }
    }
}