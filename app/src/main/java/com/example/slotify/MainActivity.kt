package com.example.slotify

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.slotify.admin.AdminSignInActivity
import com.example.slotify.user.UserSignInActivity

class MainActivity : AppCompatActivity() {

    private lateinit var roleAdmin: LinearLayout
    private lateinit var roleUser: LinearLayout
    private lateinit var btnStart: Button
    private lateinit var tvTerms: TextView
    private lateinit var checkAgree: CheckBox

    private var selectedRole: String? = null // Variable to track selected role

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        roleAdmin = findViewById(R.id.role_admin)
        roleUser = findViewById(R.id.role_user)
        btnStart = findViewById(R.id.btn_start)
        tvTerms = findViewById(R.id.tv_terms)
        checkAgree = findViewById(R.id.checkbox_agree)

        // Handle role selection
        roleAdmin.setOnClickListener {
            selectedRole = "admin"
            //Toast.makeText(this, "Admin role selected", Toast.LENGTH_SHORT).show()
        }

        roleUser.setOnClickListener {
            selectedRole = "user"
            //Toast.makeText(this, "User role selected", Toast.LENGTH_SHORT).show()
        }

        // Handle terms and conditions click
        tvTerms.setOnClickListener {
            showTermsDialog()
        }

        // Handle Get Started button click
        btnStart.setOnClickListener {
            if (!checkAgree.isChecked) {
                // Show message to agree to terms and conditions
                Toast.makeText(this, "Agree to Terms and Conditions?", Toast.LENGTH_SHORT).show()
            } else if (selectedRole == null) {
                // Show message to choose a role
                Toast.makeText(this, "Please choose a role first!", Toast.LENGTH_SHORT).show()
            } else {
                // Navigate based on selected role
                when (selectedRole) {
                    "admin" -> {
                        val intent = Intent(this, AdminSignInActivity::class.java)
                        startActivity(intent)
                    }
                    "user" -> {
                        val intent = Intent(this, UserSignInActivity::class.java)
                        startActivity(intent)
                    }
                }
            }
        }
    }

    private fun showTermsDialog() {
        val dialog = Dialog(this)
        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.dialog_terms_conditions, null)

        dialog.setContentView(dialogView)

        val checkboxUnderstand = dialogView.findViewById<CheckBox>(R.id.checkbox_understand)
        val checkboxNotUnderstand = dialogView.findViewById<CheckBox>(R.id.checkbox_not_understand)
        val btnSubmit = dialogView.findViewById<Button>(R.id.btn_submit)

        // Initially, disable the submit button
        btnSubmit.isEnabled = false

        // Handle checkbox behavior
        val checkBoxListener = CompoundButton.OnCheckedChangeListener { _, _ ->
            // Enable the submit button only if one checkbox is checked
            btnSubmit.isEnabled = checkboxUnderstand.isChecked || checkboxNotUnderstand.isChecked
        }

        checkboxUnderstand.setOnCheckedChangeListener(checkBoxListener)
        checkboxNotUnderstand.setOnCheckedChangeListener(checkBoxListener)

        // Handle submit button click
        btnSubmit.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}