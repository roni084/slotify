package com.example.slotify.admin

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.slotify.R
import com.google.firebase.firestore.FirebaseFirestore

class AdminVerificationPage2 : AppCompatActivity() {
    private lateinit var etCode1: EditText
    private lateinit var etCode2: EditText
    private lateinit var etCode3: EditText
    private lateinit var etCode4: EditText
    private lateinit var etCode5: EditText
    private lateinit var etCode6: EditText

    private lateinit var tvEmail: TextView
    private lateinit var tvTimer: TextView

    private lateinit var btnVerify: Button

    private lateinit var timer: CountDownTimer

    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var email: String
    private lateinit var correctCode: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_verification_page2)

        // Initialize views
        etCode1 = findViewById(R.id.et_code_11)
        etCode2 = findViewById(R.id.et_code_22)
        etCode3 = findViewById(R.id.et_code_33)
        etCode4 = findViewById(R.id.et_code_44)
        etCode5 = findViewById(R.id.et_code_55)
        etCode6 = findViewById(R.id.et_code_66)

        tvEmail = findViewById(R.id.tv_mail_2)
        tvTimer = findViewById(R.id.tv_timer_2)

        btnVerify = findViewById(R.id.btn_verify_2)

        email = intent.getStringExtra("email").toString()
        correctCode = intent.getStringExtra("code").toString()

        tvEmail.text = email

        // Setup EditTexts
        setupEditTexts()

        startTimer()

        // Handle "Verify" button click
        btnVerify.setOnClickListener { verifyCode() }

        // Handle "Resend code" logic
        tvTimer.setOnClickListener { resendCode() }
    }

    private fun setupEditTexts() {
        val editTexts = arrayOf(etCode1, etCode2, etCode3, etCode4, etCode5, etCode6)

        for (i in editTexts.indices) {
            val currentEditText = editTexts[i]
            val nextEditText = if (i < editTexts.size - 1) editTexts[i + 1] else null
            val previousEditText = if (i > 0) editTexts[i - 1] else null

            currentEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1) {
                        // Move to the next EditText when input is made
                        nextEditText?.requestFocus()
                    } else if (s?.length == 0) {
                        // Move to the previous EditText on backspace
                        previousEditText?.requestFocus()
                    }
                }
            })

            // Limit each EditText to only one character
            currentEditText.filters = arrayOf(InputFilter.LengthFilter(1))

            // Restrict input to digits only
            currentEditText.inputType = InputType.TYPE_CLASS_NUMBER
        }
    }

    private fun verifyCode() {
        val enteredCode = etCode1.text.toString() +
                etCode2.text.toString() +
                etCode3.text.toString() +
                etCode4.text.toString() +
                etCode5.text.toString() +
                etCode6.text.toString()

        firestore.collection("VerificationCodes").document(email).get().addOnSuccessListener { document ->
            val correctCode = document.getString("code")
            if (enteredCode == correctCode) {
                showResetPasswordDialog()
            } else {
                Toast.makeText(this, "Failed to verify. Try again later.", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to verify. Try again later.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showResetPasswordDialog() {
        // Inflate the reset password dialog
        val dialogView = layoutInflater.inflate(R.layout.dialog_reset_password, null)

        val etPassword = dialogView.findViewById<EditText>(R.id.et_reset_pass)
        val etConfirmPassword = dialogView.findViewById<EditText>(R.id.et_reset_Cpass)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btn_confirm)

        val dialog = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        btnConfirm.setOnClickListener {
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Password and Confirm Password are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Update the password in the Firestore Admin document
            firestore.collection("Admins").document(email).update("password", password)
                .addOnSuccessListener {
                    Toast.makeText(this, "Password reset successful!", Toast.LENGTH_SHORT).show()

                    // Redirect to sign-in page
                    val intent = Intent(this, AdminSignInActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }

        dialog.show()
    }

    private fun startTimer() {
        timer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tvTimer.text = "${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                btnVerify.text = "Resend code"
            }
        }
        timer.start()
    }

    private fun resendCode() {
        Toast.makeText(this, "Code resent successfully!", Toast.LENGTH_SHORT).show()
        startTimer()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
    }
}