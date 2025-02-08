package com.example.slotify.user

import EmailSender
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.slotify.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import kotlin.random.Random

class UserSignUpActivity : AppCompatActivity() {
    private lateinit var tilName: TextInputLayout
    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilAddress: TextInputLayout
    private lateinit var tilPhone: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var tilConfirmPassword: TextInputLayout

    private lateinit var etName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etAddress: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var img: ImageView

    private lateinit var btnSignUp: Button
    private lateinit var tvSignIn: TextView

    private lateinit var auth: FirebaseAuth
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_sign_up)

        auth = FirebaseAuth.getInstance()

        // Initialize views
        tilName = findViewById(R.id.til_name)
        tilEmail = findViewById(R.id.til_email)
        tilAddress = findViewById(R.id.til_address)
        tilPhone = findViewById(R.id.til_phone)
        tilPassword = findViewById(R.id.til_pass)
        tilConfirmPassword = findViewById(R.id.til_confirmpass)

        etName = findViewById(R.id.et_name)
        etEmail = findViewById(R.id.et_email)
        etAddress = findViewById(R.id.et_address)
        etPhone = findViewById(R.id.et_phone)
        etPassword = findViewById(R.id.et_pass)
        etConfirmPassword = findViewById(R.id.et_confirmpass)

        btnSignUp = findViewById(R.id.btn_signup)
        tvSignIn = findViewById(R.id.tv_sign_in)
        img = findViewById(R.id.img)

        btnSignUp.setOnClickListener { registerUser() }
        tvSignIn.setOnClickListener {
            startActivity(Intent(this, UserSignInActivity::class.java))
        }

        // Keyboard visibility listener
        val rootView = findViewById<LinearLayout>(R.id.root_layout)
        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            val heightDiff = rootView.rootView.height - rootView.height
            val isKeyboardVisible = heightDiff > 200 // Threshold for detecting keyboard

            if (isKeyboardVisible) {
                img.visibility = ImageView.GONE
            } else {
                img.visibility = ImageView.VISIBLE
            }
        }
    }

    private fun registerUser() {
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val address = etAddress.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        // Validation checks
        if (name.isEmpty() || address.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Invalid email format"
            return
        }
        if (password != confirmPassword) {
            tilConfirmPassword.error = "Passwords do not match"
            return
        }
        if (password.length < 6) {
            tilPassword.error = "Password must be at least 6 characters"
            return
        }

        // Check if email already exists in Firestore
        firestore.collection("Users").document(email).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    Toast.makeText(this, "Email already registered", Toast.LENGTH_SHORT).show()
                } else {
                    saveUserToFirestore(name, email, address, phone, password)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error checking email", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveUserToFirestore(
        name: String,
        email: String,
        address: String,
        phone: String,
        password: String
    ) {
        val hashedPassword = hashPassword(password)

        val adminData = mapOf(
            "name" to name,
            "address" to address,
            "email" to email,
            "phone" to phone,
            "password" to hashedPassword
        )

        firestore.collection("Users").document(email).set(adminData)
            .addOnSuccessListener {
                sendVerificationCode(email, name)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error saving data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendVerificationCode(email: String, name: String) {
        val verificationCode = Random.nextInt(100000, 999999).toString()

        // Save verification code to Firestore or a secure temp location
        firestore.collection("VerificationCodes").document(email)
            .set(mapOf("code" to verificationCode))
            .addOnSuccessListener {
                // Send email in a background thread
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        EmailSender.sendEmail(
                            recipientEmail = email,
                            subject = "Your Verification Code",
                            messageBody = "Hello $name, thank you for joining Slotify! Your verification code is $verificationCode.\n-Slotify Team"
                        )
                        // Update UI on the main thread after success
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@UserSignUpActivity,
                                "Verification code sent to $email",
                                Toast.LENGTH_SHORT
                            ).show()
                            // Navigate to VerificationPage
                            val intent = Intent(this@UserSignUpActivity, UserVerificationPage::class.java)
                            intent.putExtra("email", email)
                            intent.putExtra("code", verificationCode)
                            startActivity(intent)
                            //finish()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@UserSignUpActivity,
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

    private fun hashPassword(password: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(password.toByteArray(Charsets.UTF_8))
            hashBytes.joinToString("") { "%02x".format(it) } // Convert bytes to hex
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Error hashing password", e)
        }
    }
}