package com.example.slotify.admin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.slotify.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class AdminHomePage : AppCompatActivity() {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    // UI Components
    private lateinit var BusinessName: TextView
    private lateinit var businessMoto: TextView
    private lateinit var profileImage: CircleImageView

    private lateinit var button1: Button
    private lateinit var button2: Button
    private lateinit var button3: Button
    private lateinit var button4: Button

    private lateinit var homeButton: ImageButton
    private lateinit var profileButton: ImageButton
    private lateinit var addButton: ImageButton
    private lateinit var notificationButton: ImageButton
    private lateinit var dashboardButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_home_page)

        // Initialize FirebaseFirestore & FirebaseAuth
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Retrieve the email from Intent
        val email = intent.getStringExtra("email") ?: return
        val businessType = intent.getStringExtra("businessType") ?: return
        val businessName = intent.getStringExtra("businessName") ?: return

        // Initialize UI Components
        BusinessName = findViewById(R.id.business_name)
        businessMoto = findViewById(R.id.business_moto)
        profileImage = findViewById(R.id.profile)

        button1 = findViewById(R.id.button1)
        button2 = findViewById(R.id.button2)
        button3 = findViewById(R.id.button3)
        button4 = findViewById(R.id.button4)

        homeButton = findViewById(R.id.home_button)
        profileButton = findViewById(R.id.profile_button)
        addButton = findViewById(R.id.add_button)
        notificationButton = findViewById(R.id.notification_button)
        dashboardButton = findViewById(R.id.dashboard_button)

        //Optional
        //if(businessType!==null)
        BusinessName.text = businessType
        businessMoto.text = businessName

        firestore.collection("Admins")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val adminDoc = documents.first()
                    val profileImageUrl = adminDoc.getString("profileImageUrl") ?: ""

                    // Load profile image if available
                    if (profileImageUrl.isNotEmpty()) {
                        Picasso.get().load(profileImageUrl).into(profileImage)
                    } else {
                        profileImage.setImageResource(R.drawable.profile_logo)
                    }

                } else {
                    Toast.makeText(this, "Admin name not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }

        profileImage.setOnClickListener {
            val intent = Intent(this, AdminProfile::class.java)
            intent.putExtra("email", email)
            intent.putExtra("businessType", businessType)
            intent.putExtra("businessName", businessName)
            startActivity(intent)
        }

        // Example Click Listeners
        button1.setOnClickListener {
            val intent = Intent(this, AddService::class.java)
            intent.putExtra("email", email)
            intent.putExtra("businessType", businessType)
            intent.putExtra("businessName", businessName)
            startActivity(intent)
        }

        button2.setOnClickListener {
            val intent = Intent(this, EditServices::class.java)
            intent.putExtra("email", email)
            intent.putExtra("businessType", businessType)
            intent.putExtra("businessName", businessName)
            startActivity(intent)
        }

        button3.setOnClickListener {
            val intent = Intent(this, UpdateBusiness::class.java)
            intent.putExtra("email", email)
            intent.putExtra("businessType", businessType)
            intent.putExtra("businessName", businessName)
            startActivity(intent)
        }

        button4.setOnClickListener {
            val intent = Intent(this, AddService::class.java)
            intent.putExtra("email", email)
            intent.putExtra("businessType", businessType)
            intent.putExtra("businessName", businessName)
            startActivity(intent)
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
}
