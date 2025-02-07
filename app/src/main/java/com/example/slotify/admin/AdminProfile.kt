package com.example.slotify.admin

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.slotify.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class AdminProfile : AppCompatActivity() {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    // UI Components
    private lateinit var BusinessName: TextView
    private lateinit var businessMoto: TextView
    private lateinit var profileLogo: CircleImageView

    private lateinit var profileImage: CircleImageView
    private lateinit var adminName: TextView
    private lateinit var adminEmail: TextView
    private lateinit var adminPhone: TextView
    private lateinit var adminAddress: TextView

    private lateinit var editProfile: ImageView
    private lateinit var signOut: ImageView

    private lateinit var homeButton: ImageButton
    private lateinit var profileButton: ImageButton
    private lateinit var addButton: ImageButton
    private lateinit var notificationButton: ImageButton
    private lateinit var dashboardButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_profile)

        // Initialize FirebaseFirestore & FirebaseAuth
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Retrieve the email from Intent
        val email = intent.getStringExtra("email") ?: return
        val businessType = intent.getStringExtra("businessType") ?: return
        val businessName = intent.getStringExtra("businessName") ?: return

        // Initialize ImageViews
        BusinessName = findViewById(R.id.business_name)
        businessMoto = findViewById(R.id.business_moto)
        profileLogo = findViewById(R.id.profile)
        profileImage = findViewById(R.id.iv_image)
        adminName = findViewById(R.id.tv_name)
        adminEmail = findViewById(R.id.tv_email)
        adminPhone = findViewById(R.id.tv_phone)
        adminAddress = findViewById(R.id.tv_address)

        editProfile = findViewById(R.id.iv_edit_profile)
        signOut = findViewById(R.id.iv_sign_out)

        homeButton = findViewById(R.id.home_button)
        profileButton = findViewById(R.id.profile_button)
        addButton = findViewById(R.id.add_button)
        notificationButton = findViewById(R.id.notification_button)
        dashboardButton = findViewById(R.id.dashboard_button)


        firestore.collection("Admins")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val adminDoc = documents.first()
                    val name = adminDoc.getString("name") ?: "N/A"
                    val phone = adminDoc.getString("phone") ?: "N/A"
                    val address = adminDoc.getString("address") ?: "N/A"
                    val profileImageUrl = adminDoc.getString("profileImageUrl") ?: ""

                    // Set admin details to TextViews
                    adminName.text = "Name: $name"
                    adminEmail.text = "Email: $email"
                    adminPhone.text = "Phone: $phone"
                    adminAddress.text = "Address: $address"

                    // Load profile image if available
                    if (profileImageUrl.isNotEmpty()) {
                        Picasso.get().load(profileImageUrl).into(profileImage)
                        Picasso.get().load(profileImageUrl).into(profileLogo)
                    } else {
                        profileImage.setImageResource(R.drawable.profile_logo)
                        profileLogo.setImageResource(R.drawable.profile_logo)
                    }
                } else {
                    Toast.makeText(this, "Admin profile not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }





        // Edit Profile Click Event
        editProfile.setOnClickListener {
            val intent = Intent(this, AdminEditProfile::class.java)
            intent.putExtra("email", email)
            intent.putExtra("businessType", businessType)
            intent.putExtra("businessName", businessName)
            startActivity(intent)
        }

        // Sign Out Click Event
        signOut.setOnClickListener {
            signOutUser()
        }

        homeButton.setOnClickListener {
            val intent = Intent(this, AdminHomePage::class.java)
            intent.putExtra("email", email)
            intent.putExtra("businessType", businessType)
            intent.putExtra("businessName", businessName)
            startActivity(intent)
        }

        profileButton.setOnClickListener {
            val intent = Intent(this, AdminProfile::class.java)
            intent.putExtra("email", email)
            intent.putExtra("businessType", businessType)
            intent.putExtra("businessName", businessName)
            startActivity(intent)
        }

        addButton.setOnClickListener {
            val intent = Intent(this, AddService::class.java)
            intent.putExtra("email", email)
            intent.putExtra("businessType", businessType)
            intent.putExtra("businessName", businessName)
            startActivity(intent)
        }

        notificationButton.setOnClickListener {
            val intent = Intent(this, AdminNotification::class.java)
            intent.putExtra("email", email)
            intent.putExtra("businessType", businessType)
            intent.putExtra("businessName", businessName)
            startActivity(intent)
        }

        dashboardButton.setOnClickListener {
            val intent = Intent(this, AdminHomePage::class.java)
            intent.putExtra("email", email)
            intent.putExtra("businessType", businessType)
            intent.putExtra("businessName", businessName)
            startActivity(intent)
        }
    }

    private fun signOutUser() {
        Toast.makeText(this, "Signing Out...", Toast.LENGTH_SHORT).show()
        // Example sign-out logic
        // FirebaseAuth.getInstance().signOut();

        // Redirect to login screen
        val intent = Intent(this, AdminSignInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
