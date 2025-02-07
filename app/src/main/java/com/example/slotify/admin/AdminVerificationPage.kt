package com.example.slotify.admin

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.slotify.R
import com.example.slotify.admin.AdminHomePage
import com.google.firebase.firestore.FirebaseFirestore

class AdminVerificationPage : AppCompatActivity() {

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
    private lateinit var businessType: String
    private lateinit var businessName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_verification_page)

        // Initialize views
        etCode1 = findViewById(R.id.et_code_1)
        etCode2 = findViewById(R.id.et_code_2)
        etCode3 = findViewById(R.id.et_code_3)
        etCode4 = findViewById(R.id.et_code_4)
        etCode5 = findViewById(R.id.et_code_5)
        etCode6 = findViewById(R.id.et_code_6)

        tvEmail = findViewById(R.id.tv_mail)
        tvTimer = findViewById(R.id.tv_timer)

        btnVerify = findViewById(R.id.btn_verify)

        email = intent.getStringExtra("email").toString()
        correctCode = intent.getStringExtra("code").toString()
        businessType = intent.getStringExtra("businessType").toString()
        businessName = intent.getStringExtra("businessName").toString()

        tvEmail.text = "$email"

        // Setup EditTexts
        setupEditTexts()

        startTimer()

        // Handle "Verify" button click
        btnVerify.setOnClickListener { verifyCode() }

        // Handle "Resend code" logic
        //tvTimer.setOnClickListener { resendCode() }
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

        // Inflate the dialog layout
        val dialogView = layoutInflater.inflate(R.layout.dialog_verification, null)

        // Initialize views from the inflated layout
        val tvMsg = dialogView.findViewById<TextView>(R.id.tv_msg)
        val tvNavigate = dialogView.findViewById<TextView>(R.id.tv_5s)
        val btnHome = dialogView.findViewById<Button>(R.id.btn_home)

        // Create a Dialog instance
        val dialog = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false) // Prevent the user from dismissing the dialog manually
            .create()

        firestore.collection("VerificationCodes").document(email).get().addOnSuccessListener { document ->
            val correctCode = document.getString("code")
            if (enteredCode == correctCode) {
                // If the code matches
                tvMsg.text = "Successful!"
                tvNavigate.text = "Redirecting to Home Page within 5 seconds"
                btnHome.text = "Home Page"

                dialog.show()
                btnHome.setOnClickListener { dialog.dismiss() }   //new add
                // Automatically navigate to Home Page after 5 seconds
                android.os.Handler().postDelayed({
                    dialog.dismiss()
                    val intent = Intent(this@AdminVerificationPage, AdminHomePage::class.java)
                    intent.putExtra("email", email)
                    intent.putExtra("businessType", businessType)
                    intent.putExtra("businessName", businessName)
                    startActivity(intent)
                    finish()
                }, 5000)
            } else {
                tvMsg.text = "Not Successful!"
                tvNavigate.text = ""
                btnHome.text = "Back"

                dialog.show()
                btnHome.setOnClickListener { dialog.dismiss() }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to verify. Try again later.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun startTimer() {
        timer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tvTimer.text = "${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                btnVerify.text = "Resend code"
                btnVerify.setOnClickListener { resendCode() }
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