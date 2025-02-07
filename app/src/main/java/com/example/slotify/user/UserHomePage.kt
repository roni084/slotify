package com.example.slotify.user

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.slotify.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserHomePage : AppCompatActivity() {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_home_page)

        // Initialize FirebaseFirestore & FirebaseAuth
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Retrieve the email from Intent
        val email = intent.getStringExtra("email") ?: return

        // Fetch admin details from Firestorm
        firestore.collection("Users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val adminDoc = documents.first()
                    val name = adminDoc.getString("name") ?: "N/A"
                    val phone = adminDoc.getString("phone") ?: "N/A"

                    // Set admin details to TextViews
                    findViewById<TextView>(R.id.tv_name).text = "Name: $name"
                    findViewById<TextView>(R.id.tv_email).text = "Email: $email"
                    findViewById<TextView>(R.id.tv_phone).text = "Phone: $phone"
                } else {
                    Toast.makeText(this, "Admin details not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }

        // Handle Sign Out button click
        val btnSignOut = findViewById<Button>(R.id.btn_sign_out)
        btnSignOut.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Signed Out Successfully", Toast.LENGTH_SHORT).show()

            // Navigate to SignInActivity
            val intent = Intent(this, UserSignInActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}